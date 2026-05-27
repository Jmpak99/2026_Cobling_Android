package com.cobling.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobling.app.model.Block
import com.cobling.app.model.BlockType
import com.cobling.app.model.experience.ExperienceStage
import com.cobling.app.model.experience.ExperienceStageData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ExperienceQuestViewModel @Inject constructor() : ViewModel() {
    val stage: ExperienceStage = ExperienceStageData.stage1
    private val scope = viewModelScope
    var characterRow by mutableStateOf(0)
    var characterCol by mutableStateOf(0)
    var characterDirection by mutableStateOf(Direction.RIGHT)
    var mapData by mutableStateOf<List<List<Int>>>(emptyList())
    var showFailureDialog by mutableStateOf(false)
    var showSuccessDialog by mutableStateOf(false)
    var startBlock by mutableStateOf(Block(type = BlockType.START))
    var currentExecutingBlockID by mutableStateOf<UUID?>(null)
    var isExecuting by mutableStateOf(false)
    var didFailExecution by mutableStateOf(false)
    var didStopExecution by mutableStateOf(false)
    var allowedBlocks by mutableStateOf<List<BlockType>>(listOf(BlockType.MOVE_FORWARD))

    private var startRow = 0; private var startCol = 0
    var goalRow = 0; var goalCol = 0
    private var startDirection = Direction.RIGHT
    private var executionToken: UUID = UUID.randomUUID()

    init { configureStage(stage) }

    fun configureStage(stage: ExperienceStage) {
        mapData = ExperienceStageData.grid
        startRow = stage.startY; startCol = stage.startX
        goalRow = stage.goalY; goalCol = stage.goalX
        characterRow = startRow; characterCol = startCol
        characterDirection = stage.startDirection; startDirection = stage.startDirection
        allowedBlocks = stage.allowedBlocks
        showFailureDialog = false; showSuccessDialog = false
        isExecuting = false; didFailExecution = false; didStopExecution = false
        currentExecutingBlockID = null; startBlock = Block(type = BlockType.START)
    }

    fun resetForNewExperience() {
        executionToken = UUID.randomUUID(); didStopExecution = false
        startBlock = Block(type = BlockType.START); isExecuting = false
        didFailExecution = false; currentExecutingBlockID = null
        characterRow = startRow; characterCol = startCol; characterDirection = startDirection
        showFailureDialog = false; showSuccessDialog = false
    }

    fun stopExecution() {
        didStopExecution = true; executionToken = UUID.randomUUID()
        isExecuting = false; didFailExecution = false; currentExecutingBlockID = null
        characterRow = startRow; characterCol = startCol; characterDirection = startDirection
        showFailureDialog = false; showSuccessDialog = false
    }

    private fun isTokenValid(token: UUID) = token == executionToken && !didStopExecution

    fun startExecution() {
        if (isExecuting) return
        didStopExecution = false; executionToken = UUID.randomUUID()
        val token = executionToken; didFailExecution = false; isExecuting = true
        executeBlocks(startBlock.children.toList(), isTopLevel = true, token = token) {}
    }

    private fun executeBlocks(blocks: List<Block>, index: Int = 0, isTopLevel: Boolean = false, token: UUID, completion: () -> Unit) {
        if (!isTokenValid(token)) return
        if (didFailExecution) return
        if (index >= blocks.size) {
            if (!isTokenValid(token)) return
            if (!isTopLevel) { completion(); return }
            if (didFailExecution) return
            if (characterRow != goalRow || characterCol != goalCol) { resetToStart(); return }
            isExecuting = false; showSuccessDialog = true; completion(); return
        }
        val current = blocks[index]; currentExecutingBlockID = current.id
        when (current.type) {
            BlockType.MOVE_FORWARD -> {
                moveForward {
                    scope.launch { delay(500); if (isTokenValid(token)) executeBlocks(blocks, index+1, isTopLevel, token, completion) }
                }
            }
            BlockType.TURN_LEFT -> {
                characterDirection = characterDirection.turnedLeft()
                scope.launch { delay(300); if (isTokenValid(token)) executeBlocks(blocks, index+1, isTopLevel, token, completion) }
            }
            BlockType.TURN_RIGHT -> {
                characterDirection = characterDirection.turnedRight()
                scope.launch { delay(300); if (isTokenValid(token)) executeBlocks(blocks, index+1, isTopLevel, token, completion) }
            }
            BlockType.REPEAT_COUNT -> {
                val count = current.value?.toIntOrNull() ?: 1
                fun runRepeat(remaining: Int) {
                    if (!isTokenValid(token)) return
                    if (remaining <= 0) { executeBlocks(blocks, index+1, isTopLevel, token, completion); return }
                    currentExecutingBlockID = current.id
                    scope.launch { delay(200); if (!isTokenValid(token)) return@launch; executeBlocks(current.children.toList(), token = token) { runRepeat(remaining - 1) } }
                }
                runRepeat(count)
            }
            else -> scope.launch { delay(300); if (isTokenValid(token)) executeBlocks(blocks, index+1, isTopLevel, token, completion) }
        }
    }

    private fun moveForward(completion: () -> Unit) {
        var nr = characterRow; var nc = characterCol
        when (characterDirection) {
            Direction.UP -> nr -= 1; Direction.DOWN -> nr += 1
            Direction.LEFT -> nc -= 1; Direction.RIGHT -> nc += 1
        }
        if (nr < 0 || nr >= mapData.size || nc < 0 || nc >= mapData[0].size) { resetToStart(); return }
        if (mapData[nr][nc] == 0) { resetToStart(); return }
        characterRow = nr; characterCol = nc; completion()
    }

    fun resetToStart() {
        executionToken = UUID.randomUUID(); didStopExecution = false
        didFailExecution = true; isExecuting = false; currentExecutingBlockID = null
        characterRow = startRow; characterCol = startCol; characterDirection = startDirection
        showFailureDialog = true
    }

    fun resetExecution() {
        executionToken = UUID.randomUUID(); didStopExecution = false
        didFailExecution = false; isExecuting = false; currentExecutingBlockID = null
        characterRow = startRow; characterCol = startCol; characterDirection = startDirection
    }

    fun findParentContainer(target: Block): Block? {
        fun search(container: Block): Block? {
            if (container.children.any { it.id == target.id }) return container
            for (child in container.children) { if (child.type.isContainer) search(child)?.let { return it } }
            return null
        }
        return search(startBlock)
    }

    fun isDescendant(target: Block, ancestor: Block): Boolean {
        fun dfs(node: Block): Boolean {
            for (child in node.children) { if (child.id == target.id) return true; if (child.type.isContainer && dfs(child)) return true }
            return false
        }
        return dfs(ancestor)
    }

    // 편의 래퍼
    val characterPosition get() = com.cobling.app.model.Position(row = characterRow, col = characterCol)
    val goalPosition get() = com.cobling.app.model.Position(row = goalRow, col = goalCol)

    fun appendForwardBlock() {
        startBlock.children.add(Block(type = BlockType.MOVE_FORWARD))
    }

    fun removeBlock(index: Int) {
        if (index in startBlock.children.indices) {
            startBlock.children.removeAt(index)
        }
    }
}
