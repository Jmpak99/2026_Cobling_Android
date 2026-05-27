package com.cobling.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobling.app.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

// 캐릭터 방향
enum class Direction(val rawValue: String) {
    UP("up"), DOWN("down"), LEFT("left"), RIGHT("right");

    fun turnedLeft(): Direction = when (this) {
        UP -> LEFT
        LEFT -> DOWN
        DOWN -> RIGHT
        RIGHT -> UP
    }

    fun turnedRight(): Direction = when (this) {
        UP -> RIGHT
        RIGHT -> DOWN
        DOWN -> LEFT
        LEFT -> UP
    }

    companion object {
        fun fromRawValue(raw: String): Direction =
            entries.firstOrNull { it.rawValue == raw.lowercase() } ?: RIGHT
    }
}

// 다음 퀘스트 이동 액션
sealed class NextQuestAction {
    data class GoToQuest(val id: String) : NextQuestAction()
    object Locked : NextQuestAction()
    object Waiting : NextQuestAction()
    object GoToList : NextQuestAction()
}

// 성공 이후 추가 보상 데이터
data class QuestExtraRewardData(
    val earnedBadgeIds: List<String> = emptyList(),
    val memoryFragmentGranted: Boolean = false,
    val memoryFragmentId: String? = null,
    val memoryFragmentTotalCount: Int = 0,
    val hiddenChapterUnlockedNow: Boolean = false
)

@HiltViewModel
class QuestViewModel @Inject constructor(
    private val localStorage: com.cobling.app.util.LocalStorageManager
) : ViewModel() {

    // 게임 상태
    var characterRow by mutableStateOf(0)
    var characterCol by mutableStateOf(0)
    var characterDirection by mutableStateOf(Direction.RIGHT)

    private var startDirection = Direction.RIGHT

    var mapData by mutableStateOf<List<List<Int>>>(emptyList())
    var showFailureDialog by mutableStateOf(false)
    var showSuccessDialog by mutableStateOf(false)
    var startBlock by mutableStateOf(Block(type = BlockType.START))
    var currentExecutingBlockID by mutableStateOf<UUID?>(null)
    var isExecuting by mutableStateOf(false)
    var didFailExecution by mutableStateOf(false)
    var didStopExecution by mutableStateOf(false)

    private var executionToken: UUID = UUID.randomUUID()

    // Success Reward
    var successReward by mutableStateOf<SuccessReward?>(null)
    var extraRewardData by mutableStateOf(QuestExtraRewardData())
    var isShowingCutscene by mutableStateOf(false)
    var currentCutscene by mutableStateOf<ChapterCutscene?>(null)
    var isRewardLoading by mutableStateOf(false)
    var showRewardDelayAlert by mutableStateOf(false)

    // 적
    private var initialEnemies: List<Enemy> = emptyList()
    var enemies by mutableStateOf<List<Enemy>>(emptyList())

    // Firestore
    var subQuest by mutableStateOf<SubQuestDocument?>(null)
    private var startRow = 0
    private var startCol = 0
    var goalRow = 0
    var goalCol = 0

    // 편의 래퍼 (UI에서 사용)
    val characterPosition get() = com.cobling.app.model.Position(row = characterRow, col = characterCol)
    val goalPosition get() = com.cobling.app.model.Position(row = goalRow, col = goalCol)
    var allowedBlocks by mutableStateOf<List<BlockType>>(emptyList())
    var currentAllowedIfConditions by mutableStateOf<List<IfCondition>>(IfCondition.entries)
    var currentDefaultIfCondition by mutableStateOf(IfCondition.FRONT_IS_CLEAR)

    private val db = FirebaseFirestore.getInstance()
    var currentChapterId = ""
    private var currentSubQuestId = ""

    private var unlockListener: ListenerRegistration? = null
    private var userUpdateListener: ListenerRegistration? = null
    private var chapterBonusListener: ListenerRegistration? = null
    private var rewardLoadingStartedAt: Long? = null
    private val minRewardOverlayDurationMs = 450L

    override fun onCleared() {
        super.onCleared()
        unlockListener?.remove()
        userUpdateListener?.remove()
        chapterBonusListener?.remove()
    }

    // MARK: - 리셋
    fun resetForNewSubQuest() {
        executionToken = UUID.randomUUID()
        didStopExecution = false
        startBlock = Block(type = BlockType.START)
        isExecuting = false
        didFailExecution = false
        currentExecutingBlockID = null
        characterRow = startRow
        characterCol = startCol
        characterDirection = startDirection
        enemies = initialEnemies.toList()
        showFailureDialog = false
        showSuccessDialog = false
        successReward = null
        extraRewardData = QuestExtraRewardData()
        isRewardLoading = false
        rewardLoadingStartedAt = null
    }

    fun stopExecution() {
        didStopExecution = true
        executionToken = UUID.randomUUID()
        isExecuting = false
        didFailExecution = false
        currentExecutingBlockID = null
        characterRow = startRow
        characterCol = startCol
        characterDirection = startDirection
        enemies = initialEnemies.toList()
        showFailureDialog = false
        showSuccessDialog = false
    }

    private fun isTokenValid(token: UUID): Boolean = token == executionToken && !didStopExecution

    // 컷씬 관련
    fun wasCutsceneShown(chapterId: String, type: ChapterCutsceneType): Boolean {
        return localStorage.isCutsceneShown(chapterId, type)
    }

    fun wasOutroShown(chapterId: String): Boolean = wasCutsceneShown(chapterId, ChapterCutsceneType.OUTRO)

    fun presentIntroIfNeeded(chapterId: String) {
        if (localStorage.isCutsceneShown(chapterId, ChapterCutsceneType.INTRO)) return
        val lines = ChapterDialogueStore.lines(chapterId, ChapterCutsceneType.INTRO)
        if (lines.isEmpty()) return

        val cutscene = ChapterCutscene(
            chapterId = chapterId,
            type = ChapterCutsceneType.INTRO,
            lines = lines
        )

        currentCutscene = cutscene
        isShowingCutscene = true
    }

    fun presentOutroAfterChapterReward(chapterId: String) {
        if (localStorage.isCutsceneShown(chapterId, ChapterCutsceneType.OUTRO)) return
        val lines = ChapterDialogueStore.lines(chapterId, ChapterCutsceneType.OUTRO)
        if (lines.isEmpty()) return

        val cutscene = ChapterCutscene(
            chapterId = chapterId,
            type = ChapterCutsceneType.OUTRO,
            lines = lines
        )

        currentCutscene = cutscene
        isShowingCutscene = true
    }

    fun dismissCutsceneAndMarkShown() {
        val c = currentCutscene ?: run {
            isShowingCutscene = false
            return
        }

        localStorage.setCutsceneShown(c.chapterId, c.type)
        isShowingCutscene = false
        currentCutscene = null
    }

    private fun beginRewardLoading() {
        rewardLoadingStartedAt = System.currentTimeMillis()
        isRewardLoading = true
    }

    private fun endRewardLoadingAndShowSuccess(showSuccess: () -> Unit) {
        val started = rewardLoadingStartedAt ?: System.currentTimeMillis()
        val elapsed = System.currentTimeMillis() - started
        val remaining = maxOf(0L, minRewardOverlayDurationMs - elapsed)

        viewModelScope.launch {
            delay(remaining)
            isRewardLoading = false
            rewardLoadingStartedAt = null
            showSuccess()
        }
    }

    private fun applyIfRules(subQuest: SubQuestDocument) {
        val allowedRaw = subQuest.rules.allowedIfConditions ?: emptyList()
        val allowed = allowedRaw.mapNotNull { IfCondition.fromRawValue(it) }
        currentAllowedIfConditions = if (allowed.isEmpty()) {
            IfCondition.entries
        } else {
            allowed
        }

        val raw = subQuest.rules.defaultIfCondition
        currentDefaultIfCondition = if (raw != null) {
            IfCondition.fromRawValue(raw) ?: IfCondition.FRONT_IS_CLEAR
        } else {
            IfCondition.FRONT_IS_CLEAR
        }
    }

    // MARK: - Firestore 서브퀘스트 로드
    fun fetchSubQuest(chapterId: String, subQuestId: String) {
        currentChapterId = chapterId
        currentSubQuestId = subQuestId

        db.collection("quests").document(chapterId)
            .collection("subQuests").document(subQuestId)
            .get()
            .addOnSuccessListener { snapshot ->
                try {
                    val sq = snapshot.toObject(SubQuestDocument::class.java)
                        ?: return@addOnSuccessListener

                    val loadedSq = sq.copy(id = snapshot.id)
                    subQuest = loadedSq
                    mapData = loadedSq.map.parsedGrid

                    startRow = loadedSq.map.start.row
                    startCol = loadedSq.map.start.col
                    goalRow = loadedSq.map.goal.row
                    goalCol = loadedSq.map.goal.col

                    val loadedEnemies = (loadedSq.map.enemies ?: emptyList())
                        .filter { it.id.trim().isNotEmpty() }

                    initialEnemies = loadedEnemies
                    enemies = loadedEnemies.toList()

                    characterRow = startRow
                    characterCol = startCol

                    val dir = Direction.fromRawValue(loadedSq.map.startDirection)
                    startDirection = dir
                    characterDirection = dir

                    allowedBlocks = loadedSq.rules.allowBlocks.mapNotNull {
                        BlockType.fromRawValue(it)
                    }

                    applyIfRules(loadedSq)
                } catch (e: Exception) {
                    println("❌ 디코딩 실패: ${e.message}")
                }
            }
            .addOnFailureListener {
                println("Firestore 불러오기 실패: $it")
            }
    }

    // MARK: - IF 조건 판정
    private fun evaluateIfCondition(cond: IfCondition): Boolean = when (cond) {
        IfCondition.FRONT_IS_CLEAR -> isFrontClear()
        IfCondition.FRONT_IS_BLOCKED -> !isFrontClear()
        IfCondition.ENEMY_IN_FRONT -> isEnemyInFront()
        else -> false
    }

    private fun isFrontClear(): Boolean {
        if (mapData.isEmpty() || mapData[0].isEmpty()) return false

        var nr = characterRow
        var nc = characterCol

        when (characterDirection) {
            Direction.UP -> nr -= 1
            Direction.DOWN -> nr += 1
            Direction.LEFT -> nc -= 1
            Direction.RIGHT -> nc += 1
        }

        if (nr < 0 || nr >= mapData.size || nc < 0 || nc >= mapData[0].size) {
            return false
        }

        return mapData[nr][nc] != 0
    }

    private fun isEnemyInFront(): Boolean {
        var nr = characterRow
        var nc = characterCol

        when (characterDirection) {
            Direction.UP -> nr -= 1
            Direction.DOWN -> nr += 1
            Direction.LEFT -> nc -= 1
            Direction.RIGHT -> nc += 1
        }

        return enemies.any { it.row == nr && it.col == nc }
    }

    // MARK: - 블록 실행 시작
    fun startExecution() {
        if (isExecuting) return

        didStopExecution = false
        executionToken = UUID.randomUUID()

        val token = executionToken

        didFailExecution = false
        isExecuting = true

        executeBlocks(
            blocks = startBlock.children.toList(),
            isTopLevel = true,
            token = token
        ) {}
    }

    private fun executeBlocks(
        blocks: List<Block>,
        index: Int = 0,
        isTopLevel: Boolean = false,
        token: UUID,
        completion: () -> Unit
    ) {
        if (!isTokenValid(token)) return
        if (didFailExecution) return

        if (index >= blocks.size) {
            if (!isTokenValid(token)) return

            if (!isTopLevel) {
                completion()
                return
            }

            if (didFailExecution) return

            if (characterRow != goalRow || characterCol != goalCol) {
                resetToStart()
                return
            }

            if (enemies.isNotEmpty()) {
                resetToStart()
                return
            }

            isExecuting = false

            val sq = subQuest
            if (sq != null) {
                handleQuestClear(sq, countUsedBlocks())
            }

            completion()
            return
        }

        val current = blocks[index]
        currentExecutingBlockID = current.id

        when (current.type) {
            BlockType.MOVE_FORWARD -> {
                moveForward {
                    viewModelScope.launch {
                        delay(500)
                        if (isTokenValid(token)) {
                            executeBlocks(blocks, index + 1, isTopLevel, token, completion)
                        }
                    }
                }
            }

            BlockType.TURN_LEFT -> {
                characterDirection = characterDirection.turnedLeft()

                viewModelScope.launch {
                    delay(300)
                    if (isTokenValid(token)) {
                        executeBlocks(blocks, index + 1, isTopLevel, token, completion)
                    }
                }
            }

            BlockType.TURN_RIGHT -> {
                characterDirection = characterDirection.turnedRight()

                viewModelScope.launch {
                    delay(300)
                    if (isTokenValid(token)) {
                        executeBlocks(blocks, index + 1, isTopLevel, token, completion)
                    }
                }
            }

            BlockType.ATTACK -> {
                attack {
                    viewModelScope.launch {
                        delay(350)
                        if (isTokenValid(token)) {
                            executeBlocks(blocks, index + 1, isTopLevel, token, completion)
                        }
                    }
                }
            }

            BlockType.REPEAT_COUNT -> {
                val repeatCount = current.value?.toIntOrNull() ?: 1

                fun runRepeat(remaining: Int) {
                    if (!isTokenValid(token)) return

                    if (remaining <= 0) {
                        executeBlocks(blocks, index + 1, isTopLevel, token, completion)
                        return
                    }

                    currentExecutingBlockID = current.id

                    viewModelScope.launch {
                        delay(200)

                        if (!isTokenValid(token)) return@launch

                        executeBlocks(current.children.toList(), token = token) {
                            runRepeat(remaining - 1)
                        }
                    }
                }

                runRepeat(repeatCount)
            }

            BlockType.IF -> {
                val cond = current.condition
                val shouldRun = evaluateIfCondition(cond)

                currentExecutingBlockID = current.id

                viewModelScope.launch {
                    delay(200)

                    if (!isTokenValid(token)) return@launch

                    if (shouldRun) {
                        executeBlocks(current.children.toList(), token = token) {
                            executeBlocks(blocks, index + 1, isTopLevel, token, completion)
                        }
                    } else {
                        executeBlocks(blocks, index + 1, isTopLevel, token, completion)
                    }
                }
            }

            else -> {
                viewModelScope.launch {
                    delay(300)
                    if (isTokenValid(token)) {
                        executeBlocks(blocks, index + 1, isTopLevel, token, completion)
                    }
                }
            }
        }
    }

    fun removeBlockFromCanvas(
        block: Block,
        parentContainer: Block? = null
    ) {
        if (block.type == BlockType.START) return

        if (parentContainer != null) {
            parentContainer.children.removeAll { it.id == block.id }
        } else {
            startBlock.children.removeAll { it.id == block.id }
        }

        refreshStartBlock()
    }

    fun addBlockFromPalette(type: BlockType, insertIndex: Int? = null) {
        if (type == BlockType.START) return

        val newBlock = Block(type = type)

        val safeIndex = insertIndex
            ?.coerceIn(0, startBlock.children.size)
            ?: startBlock.children.size

        startBlock.children.add(safeIndex, newBlock)

        refreshStartBlock()
    }

    fun addBlockToContainerFromPalette(
        containerBlock: Block,
        type: BlockType,
        insertIndex: Int
    ) {
        if (type == BlockType.START) return
        if (!containerBlock.type.isContainer) return

        val newBlock = Block(type = type)

        val safeIndex = insertIndex.coerceIn(0, containerBlock.children.size)
        containerBlock.children.add(safeIndex, newBlock)

        refreshStartBlock()
    }

    fun moveBlockToRoot(
        movingBlock: Block,
        oldParent: Block?,
        insertIndex: Int?
    ) {
        if (movingBlock.type == BlockType.START) return

        val targetIndex = insertIndex ?: startBlock.children.size

        if (oldParent == null) {
            val oldIndex = startBlock.children.indexOfFirst { it.id == movingBlock.id }
            if (oldIndex == -1) return

            startBlock.children.removeAt(oldIndex)

            val adjustedIndex = if (oldIndex < targetIndex) {
                targetIndex - 1
            } else {
                targetIndex
            }

            val safeIndex = adjustedIndex.coerceIn(0, startBlock.children.size)
            startBlock.children.add(safeIndex, movingBlock)
        } else {
            oldParent.children.removeAll { it.id == movingBlock.id }

            val safeIndex = targetIndex.coerceIn(0, startBlock.children.size)
            startBlock.children.add(safeIndex, movingBlock)
        }

        refreshStartBlock()
    }

    fun moveBlockToContainer(
        movingBlock: Block,
        oldParent: Block?,
        newParent: Block,
        insertIndex: Int
    ) {
        if (movingBlock.type == BlockType.START) return
        if (!newParent.type.isContainer) return
        if (movingBlock.id == newParent.id) return

        if (
            isDescendant(
                target = newParent,
                ancestor = movingBlock
            )
        ) {
            return
        }

        if (oldParent?.id == newParent.id) {
            val oldIndex = newParent.children.indexOfFirst { it.id == movingBlock.id }
            if (oldIndex == -1) return

            newParent.children.removeAt(oldIndex)

            val adjustedIndex = if (oldIndex < insertIndex) {
                insertIndex - 1
            } else {
                insertIndex
            }

            val safeIndex = adjustedIndex.coerceIn(0, newParent.children.size)
            newParent.children.add(safeIndex, movingBlock)
        } else {
            if (oldParent == null) {
                startBlock.children.removeAll { it.id == movingBlock.id }
            } else {
                oldParent.children.removeAll { it.id == movingBlock.id }
            }

            val safeIndex = insertIndex.coerceIn(0, newParent.children.size)
            newParent.children.add(safeIndex, movingBlock)
        }

        refreshStartBlock()
    }

    private fun refreshStartBlock() {
        val copiedChildren = startBlock.children.toMutableList()

        startBlock = Block(type = BlockType.START).apply {
            children.addAll(copiedChildren)
        }
    }

    fun findParentContainer(target: Block): Block? {
        fun search(container: Block): Block? {
            if (container.children.any { it.id == target.id }) return container

            for (child in container.children) {
                if (child.type.isContainer) {
                    search(child)?.let { return it }
                }
            }

            return null
        }

        return search(startBlock)
    }

    fun isDescendant(target: Block, ancestor: Block): Boolean {
        fun dfs(node: Block): Boolean {
            for (child in node.children) {
                if (child.id == target.id) return true

                if (child.type.isContainer && dfs(child)) {
                    return true
                }
            }

            return false
        }

        return dfs(ancestor)
    }

    fun maxExpForLevel(level: Int): Double {
        val table = mapOf(
            1 to 100.0,
            2 to 120.0,
            3 to 160.0,
            4 to 200.0,
            5 to 240.0,
            6 to 310.0,
            7 to 380.0,
            8 to 480.0,
            9 to 600.0,
            10 to 750.0,
            11 to 930.0,
            12 to 1160.0,
            13 to 1460.0,
            14 to 1820.0,
            15 to 2270.0,
            16 to 2840.0,
            17 to 3550.0,
            18 to 4440.0,
            19 to 5550.0
        )

        return table[level] ?: 100.0
    }

    private fun applyGainLocally(level: Int, exp: Double, gain: Int): Pair<Int, Double> {
        var lv = level
        var e = exp + maxOf(0, gain).toDouble()

        while (e >= maxExpForLevel(lv)) {
            e -= maxExpForLevel(lv)
            lv++
        }

        return Pair(lv, e)
    }

    // MARK: - 이동
    private fun moveForward(completion: () -> Unit) {
        var newRow = characterRow
        var newCol = characterCol

        when (characterDirection) {
            Direction.UP -> newRow -= 1
            Direction.DOWN -> newRow += 1
            Direction.LEFT -> newCol -= 1
            Direction.RIGHT -> newCol += 1
        }

        if (newRow < 0 || newRow >= mapData.size || newCol < 0 || newCol >= mapData[0].size) {
            resetToStart()
            return
        }

        if (mapData[newRow][newCol] == 0) {
            resetToStart()
            return
        }

        if (enemies.any { it.row == newRow && it.col == newCol }) {
            resetToStart()
            return
        }

        characterRow = newRow
        characterCol = newCol

        completion()
    }

    private fun attack(completion: () -> Unit) {
        val target = enemyInAttackRange() ?: run {
            completion()
            return
        }

        enemies = enemies.filter { it.id != target.id }

        completion()
    }

    fun enemyInAttackRange(): Enemy? {
        val sq = subQuest ?: return null
        val range = maxOf(0, sq.rules.attackRange)

        if (range == 0) return null

        for (step in 1..range) {
            var tr = characterRow
            var tc = characterCol

            when (characterDirection) {
                Direction.UP -> tr -= step
                Direction.DOWN -> tr += step
                Direction.LEFT -> tc -= step
                Direction.RIGHT -> tc += step
            }

            enemies.firstOrNull { it.row == tr && it.col == tc }?.let {
                return it
            }
        }

        return null
    }

    fun resetToStart() {
        executionToken = UUID.randomUUID()
        didStopExecution = false
        didFailExecution = true
        isExecuting = false
        currentExecutingBlockID = null
        characterRow = startRow
        characterCol = startCol
        characterDirection = startDirection
        enemies = initialEnemies.toList()
        showFailureDialog = true
    }

    fun resetExecution() {
        executionToken = UUID.randomUUID()
        didStopExecution = false
        didFailExecution = false
        isExecuting = false
        currentExecutingBlockID = null
        characterRow = startRow
        characterCol = startCol
        characterDirection = startDirection
        enemies = initialEnemies.toList()
    }

    private fun countUsedBlocks(): Int {
        fun dfs(blocks: List<Block>): Int {
            var total = 0

            for (b in blocks) {
                total += 1

                if (b.type.isContainer) {
                    total += dfs(b.children)
                }
            }

            return total
        }

        return dfs(startBlock.children)
    }

    // MARK: - 퀘스트 클리어 처리 (서버 연동)
    private fun handleQuestClear(sq: SubQuestDocument, usedBlocks: Int) {
        beginRewardLoading()

        val baseExp = sq.rewards.baseExp
        val bonusExp = sq.rewards.perfectBonusExp
        val maxSteps = sq.rules.maxSteps
        val isPerfect = usedBlocks <= maxSteps
        val earned = if (isPerfect) baseExp + bonusExp else baseExp

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val subId = currentSubQuestId.takeIf { it.isNotEmpty() } ?: return

        val progressRef = db.collection("users").document(userId)
            .collection("progress").document(currentChapterId)
            .collection("subQuests").document(subId)

        val userRef = db.collection("users").document(userId)

        viewModelScope.launch {
            try {
                val progressSnap = progressRef.get(Source.SERVER).await()
                val prevState = progressSnap.getString("state") ?: "locked"

                if (prevState == "completed") {
                    progressRef.update(
                        mapOf(
                            "attempts" to FieldValue.increment(1),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()

                    val userSnap = userRef.get(Source.SERVER).await()
                    val level = userSnap.getLong("level")?.toInt() ?: 1
                    val exp = userSnap.getDouble("exp") ?: 0.0
                    val maxExp = maxExpForLevel(level)

                    fetchChapterBonusAndMissionInfo(userId) { isCleared, chapterBonus, missionData ->
                        successReward = SuccessReward(
                            level = level,
                            currentExp = exp.toFloat(),
                            maxExp = maxExp.toFloat(),
                            gainedExp = 0,
                            isPerfectClear = false,
                            chapterBonusExp = chapterBonus,
                            isChapterCleared = isCleared,
                            didJustCompleteDailyMission = missionData.first.didJustDaily,
                            didJustCompleteMonthlyMission = missionData.first.didJustMonthly,
                            isDailyMissionCompleted = missionData.first.isDailyCompleted,
                            isMonthlyMissionCompleted = missionData.first.isMonthlyCompleted,
                            dailyMissionRewardExp = missionData.second.dailyRewardExp,
                            monthlyMissionRewardExp = missionData.second.monthlyRewardExp
                        )

                        endRewardLoadingAndShowSuccess {
                            showSuccessDialog = true
                        }
                    }

                    return@launch
                }

                val userSnap = userRef.get().await()
                val prevLevel = userSnap.getLong("level")?.toInt() ?: 1
                val prevExp = userSnap.getDouble("exp") ?: 0.0

                progressRef.update(
                    mapOf(
                        "earnedExp" to earned,
                        "perfectClear" to isPerfect,
                        "state" to "completed",
                        "attempts" to FieldValue.increment(1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()

                // users 업데이트 대기
                waitForUserUpdate(userRef, prevLevel, prevExp, 6000L) { level, exp ->
                    viewModelScope.launch {
                        waitForChapterBonusWrite(userId, 5000L) { isCleared, chapterBonus ->
                            fetchChapterBonusAndMissionInfo(userId) { _, _, missionData ->
                                val extraRewardExp =
                                    chapterBonus +
                                            missionData.second.dailyRewardExp +
                                            missionData.second.monthlyRewardExp

                                val hasExtraReward = extraRewardExp > 0

                                if (!hasExtraReward) {
                                    val maxExp = maxExpForLevel(level)

                                    successReward = buildReward(
                                        level = level,
                                        currentExp = exp.toFloat(),
                                        maxExp = maxExp.toFloat(),
                                        gainedExp = earned,
                                        isPerfect = isPerfect,
                                        chapterBonus = 0,
                                        isChapterCleared = false,
                                        missionData = missionData,
                                        previousLevel = prevLevel,
                                        previousExp = prevExp.toFloat()
                                    )

                                    endRewardLoadingAndShowSuccess {
                                        showSuccessDialog = true
                                    }
                                } else {
                                    waitForUserUpdate(userRef, level, exp, 5000L) { finalLevel, finalExp ->
                                        fetchChapterBonusAndMissionInfo(userId) { finalIsCleared, finalChapterBonus, finalMission ->
                                            val maxExp = maxExpForLevel(finalLevel)

                                            successReward = buildReward(
                                                level = finalLevel,
                                                currentExp = finalExp.toFloat(),
                                                maxExp = maxExp.toFloat(),
                                                gainedExp = earned,
                                                isPerfect = isPerfect,
                                                chapterBonus = finalChapterBonus,
                                                isChapterCleared = finalIsCleared,
                                                missionData = finalMission,
                                                previousLevel = prevLevel,
                                                previousExp = prevExp.toFloat()
                                            )

                                            endRewardLoadingAndShowSuccess {
                                                showSuccessDialog = true
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("❌ 퀘스트 클리어 처리 실패: ${e.message}")
            }
        }
    }

    private data class MissionData(
        val didJustDaily: Boolean,
        val didJustMonthly: Boolean,
        val isDailyCompleted: Boolean,
        val isMonthlyCompleted: Boolean
    )

    private data class MissionReward(
        val dailyRewardExp: Int,
        val monthlyRewardExp: Int
    )

    private fun buildReward(
        level: Int,
        currentExp: Float,
        maxExp: Float,
        gainedExp: Int,
        isPerfect: Boolean,
        chapterBonus: Int,
        isChapterCleared: Boolean,
        missionData: Pair<MissionData, MissionReward>,
        previousLevel: Int,
        previousExp: Float
    ) = SuccessReward(
        level = level,
        currentExp = currentExp,
        maxExp = maxExp,

        previousLevel = previousLevel,
        previousExp = previousExp,

        gainedExp = gainedExp,
        isPerfectClear = isPerfect,
        chapterBonusExp = chapterBonus,
        isChapterCleared = isChapterCleared,
        didJustCompleteDailyMission = missionData.first.didJustDaily,
        didJustCompleteMonthlyMission = missionData.first.didJustMonthly,
        isDailyMissionCompleted = missionData.first.isDailyCompleted,
        isMonthlyMissionCompleted = missionData.first.isMonthlyCompleted,
        dailyMissionRewardExp = missionData.second.dailyRewardExp,
        monthlyMissionRewardExp = missionData.second.monthlyRewardExp
    )

    private fun fetchChapterBonusAndMissionInfo(
        userId: String,
        callback: (
            isCleared: Boolean,
            chapterBonus: Int,
            missionData: Pair<MissionData, MissionReward>
        ) -> Unit
    ) {
        val ref = db.collection("users").document(userId)
            .collection("progress").document(currentChapterId)
            .collection("subQuests").document(currentSubQuestId)

        viewModelScope.launch {
            try {
                val snap = ref.get(Source.SERVER).await()
                val data = snap.data ?: emptyMap()

                val cleared = data["chapterClearGranted"] as? Boolean ?: false
                val bonus = (data["chapterBonusExpGranted"] as? Long)?.toInt() ?: 0

                val md = MissionData(
                    didJustDaily = data["didJustCompleteDailyMission"] as? Boolean ?: false,
                    didJustMonthly = data["didJustCompleteMonthlyMission"] as? Boolean ?: false,
                    isDailyCompleted = data["isDailyMissionCompleted"] as? Boolean ?: false,
                    isMonthlyCompleted = data["isMonthlyMissionCompleted"] as? Boolean ?: false
                )

                val mr = MissionReward(
                    dailyRewardExp = (data["dailyMissionRewardExpGranted"] as? Long)?.toInt() ?: 0,
                    monthlyRewardExp = (data["monthlyMissionRewardExpGranted"] as? Long)?.toInt() ?: 0
                )

                val earnedBadgeIds = (data["earnedBadgeIds"] as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?: emptyList()

                extraRewardData = QuestExtraRewardData(
                    earnedBadgeIds = earnedBadgeIds,
                    memoryFragmentGranted = data["memoryFragmentGranted"] as? Boolean ?: false,
                    memoryFragmentId = data["memoryFragmentId"] as? String,
                    memoryFragmentTotalCount = (data["memoryFragmentTotalCount"] as? Long)?.toInt() ?: 0,
                    hiddenChapterUnlockedNow = data["hiddenChapterUnlockedNow"] as? Boolean ?: false
                )

                println("🎁 extraRewardData loaded: $extraRewardData")

                callback(cleared, bonus, Pair(md, mr))
            } catch (e: Exception) {
                extraRewardData = QuestExtraRewardData()

                callback(
                    false,
                    0,
                    Pair(
                        MissionData(
                            didJustDaily = false,
                            didJustMonthly = false,
                            isDailyCompleted = false,
                            isMonthlyCompleted = false
                        ),
                        MissionReward(
                            dailyRewardExp = 0,
                            monthlyRewardExp = 0
                        )
                    )
                )
            }
        }
    }

    private fun waitForUserUpdate(
        userRef: com.google.firebase.firestore.DocumentReference,
        previousLevel: Int,
        previousExp: Double,
        timeoutMs: Long,
        completion: (level: Int, exp: Double) -> Unit
    ) {
        var done = false

        userUpdateListener?.remove()
        userUpdateListener = null

        viewModelScope.launch {
            delay(timeoutMs)

            if (done) return@launch

            done = true
            userUpdateListener?.remove()
            userUpdateListener = null

            userRef.get(Source.SERVER).addOnSuccessListener { snap ->
                val level = snap.getLong("level")?.toInt() ?: 1
                val exp = snap.getDouble("exp") ?: 0.0

                completion(level, exp)
            }
        }

        userUpdateListener = userRef.addSnapshotListener { snap, _ ->
            if (done) return@addSnapshotListener

            val data = snap?.data ?: return@addSnapshotListener
            val level = data["level"].toIntValue(1)
            val exp = data["exp"].toDoubleValue(0.0)

            if (level != previousLevel || exp != previousExp) {
                done = true
                userUpdateListener?.remove()
                userUpdateListener = null

                completion(level, exp)
            }
        }
    }

    private fun waitForChapterBonusWrite(
        userId: String,
        timeoutMs: Long,
        completion: (isCleared: Boolean, bonusExp: Int) -> Unit
    ) {
        val ref = db.collection("users").document(userId)
            .collection("progress").document(currentChapterId)
            .collection("subQuests").document(currentSubQuestId)

        var done = false

        chapterBonusListener?.remove()
        chapterBonusListener = null

        viewModelScope.launch {
            delay(timeoutMs)

            if (done) return@launch

            done = true
            chapterBonusListener?.remove()
            chapterBonusListener = null

            try {
                val snap = ref.get(Source.SERVER).await()
                val d = snap.data ?: emptyMap<String, Any>()

                val cleared = d["chapterClearGranted"] as? Boolean ?: false
                val bonus = (d["chapterBonusExpGranted"] as? Long)?.toInt() ?: 0

                completion(cleared, bonus)
            } catch (e: Exception) {
                completion(false, 0)
            }
        }

        chapterBonusListener = ref.addSnapshotListener { snap, _ ->
            if (done) return@addSnapshotListener

            val d = snap?.data ?: emptyMap<String, Any>()

            val rewardSettled = d["rewardSettled"] as? Boolean ?: false
            val cleared = d["chapterClearGranted"] as? Boolean ?: false
            val bonus = (d["chapterBonusExpGranted"] as? Long)?.toInt() ?: 0

            if (rewardSettled) {
                done = true
                chapterBonusListener?.remove()
                chapterBonusListener = null

                completion(cleared, bonus)
            }
        }
    }

    // MARK: - 다음 퀘스트
    fun goToNextSubQuest(completion: (NextQuestAction) -> Unit) {
        val sq = subQuest ?: run {
            completion(NextQuestAction.GoToList)
            return
        }

        val nextOrder = sq.order + 1

        db.collection("quests").document(currentChapterId)
            .collection("subQuests")
            .whereEqualTo("order", nextOrder)
            .get()
            .addOnSuccessListener { snapshot ->
                val doc = snapshot.documents.firstOrNull() ?: run {
                    completion(NextQuestAction.GoToList)
                    return@addOnSuccessListener
                }

                val nextId = doc.id

                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                    completion(NextQuestAction.Locked)
                    return@addOnSuccessListener
                }

                val progressRef = db.collection("users").document(userId)
                    .collection("progress").document(currentChapterId)
                    .collection("subQuests").document(nextId)

                progressRef.get(Source.SERVER)
                    .addOnSuccessListener { snap ->
                        val state = snap.getString("state") ?: "locked"

                        handleNextState(
                            state = state,
                            progressRef = progressRef,
                            nextId = nextId,
                            completion = completion
                        )
                    }
                    .addOnFailureListener {
                        progressRef.get()
                            .addOnSuccessListener { snap ->
                                val state = snap.getString("state") ?: "locked"

                                handleNextState(
                                    state = state,
                                    progressRef = progressRef,
                                    nextId = nextId,
                                    completion = completion
                                )
                            }
                    }
            }
            .addOnFailureListener {
                completion(NextQuestAction.GoToList)
            }
    }

    private fun handleNextState(
        state: String,
        progressRef: com.google.firebase.firestore.DocumentReference,
        nextId: String,
        completion: (NextQuestAction) -> Unit
    ) {
        when (state) {
            "inProgress", "completed" -> {
                completion(NextQuestAction.GoToQuest(nextId))
            }

            "locked" -> {
                waitUntilUnlocked(
                    progressRef = progressRef,
                    timeoutMs = 4000L,
                    onUnlocked = {
                        completion(NextQuestAction.GoToQuest(nextId))
                    },
                    onTimeout = {
                        completion(NextQuestAction.Waiting)
                    }
                )
            }

            else -> {
                completion(NextQuestAction.Locked)
            }
        }
    }

    private fun waitUntilUnlocked(
        progressRef: com.google.firebase.firestore.DocumentReference,
        timeoutMs: Long,
        onUnlocked: () -> Unit,
        onTimeout: () -> Unit
    ) {
        unlockListener?.remove()

        var done = false

        viewModelScope.launch {
            delay(timeoutMs)

            if (done) return@launch

            done = true
            unlockListener?.remove()
            unlockListener = null

            onTimeout()
        }

        unlockListener = progressRef.addSnapshotListener { snap, _ ->
            if (done) return@addSnapshotListener

            val state = snap?.getString("state") ?: "locked"

            if (state == "inProgress" || state == "completed") {
                done = true
                unlockListener?.remove()
                unlockListener = null

                onUnlocked()
            }
        }
    }

    val storyMessage: String?
        get() = subQuest?.story?.takeIf { it.isActive }?.message

    val hintMessage: String?
        get() = subQuest?.hint?.takeIf { it.isActive }?.message
}

private fun Any?.toDoubleValue(default: Double = 0.0): Double {
    return when (this) {
        is Double -> this
        is Long -> this.toDouble()
        is Int -> this.toDouble()
        is Float -> this.toDouble()
        is Number -> this.toDouble()
        else -> default
    }
}

private fun Any?.toIntValue(default: Int = 0): Int {
    return when (this) {
        is Int -> this
        is Long -> this.toInt()
        is Double -> this.toInt()
        is Float -> this.toInt()
        is Number -> this.toInt()
        else -> default
    }
}

private data class EvolutionResult(
    val shouldEvolve: Boolean,
    val fromStage: String,
    val toStage: String
)

private fun getStageForLevel(level: Int): String {
    return when {
        level >= 20 -> "legend"
        level >= 10 -> "cobling"
        level >= 5 -> "kid"
        else -> "egg"
    }
}

private fun calculateEvolution(
    previousLevel: Int,
    currentLevel: Int,
    currentStage: String
): EvolutionResult {
    val beforeStage = getStageForLevel(previousLevel)
    val afterStage = getStageForLevel(currentLevel)

    val shouldEvolve =
        previousLevel < currentLevel &&
                beforeStage != afterStage &&
                currentStage != afterStage

    return EvolutionResult(
        shouldEvolve = shouldEvolve,
        fromStage = beforeStage,
        toStage = afterStage
    )
}