package com.cobling.app.ui.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cobling.app.ui.quest.block.GhostBlockOverlay
import com.cobling.app.ui.quest.blockintro.BlockIntroType
import com.cobling.app.ui.quest.blockintro.BlockIntroView
import com.cobling.app.ui.quest.cutscene.ChapterCutsceneScreen
import com.cobling.app.ui.quest.tutorial.QuestTutorialOverlayView
import com.cobling.app.ui.shared.FailureDialogView
import com.cobling.app.ui.shared.ReviewPromptView
import com.cobling.app.ui.shared.SuccessDialogView
import com.cobling.app.ui.shared.evolution.EvolutionView
import com.cobling.app.viewmodel.AuthViewModel
import com.cobling.app.viewmodel.DragManager
import com.cobling.app.viewmodel.NextQuestAction
import com.cobling.app.viewmodel.QuestTutorialViewModel
import com.cobling.app.viewmodel.QuestViewModel
import com.cobling.app.viewmodel.ReviewManagerViewModel
import com.cobling.app.viewmodel.TabBarViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cobling.app.ui.shared.RewardNoticeDialogView
import com.cobling.app.ui.shared.RewardNoticeType

private enum class QuestRewardPopupType {
    BADGE,
    MEMORY_FRAGMENT,
    HIDDEN_CHAPTER_OPEN
}

@Composable
fun QuestBlockScreen(
    chapterId: String,
    subQuestId: String,
    tabBarViewModel: TabBarViewModel,
    authViewModel: AuthViewModel,
    onGoNextSubQuest: (String) -> Unit,
    onExitToList: () -> Unit,
    dragManager: DragManager = remember { DragManager() },
    viewModel: QuestViewModel = hiltViewModel(),
    tutorialVM: QuestTutorialViewModel = hiltViewModel(),
    reviewManagerVM: ReviewManagerViewModel = hiltViewModel()
) {
    var storyButtonFrame by remember { mutableStateOf<Rect?>(null) }
    var blockPaletteFrame by remember { mutableStateOf<Rect?>(null) }
    var blockCanvasFrame by remember { mutableStateOf<Rect?>(null) }
    var playButtonFrame by remember { mutableStateOf<Rect?>(null) }
    var stopButtonFrame by remember { mutableStateOf<Rect?>(null) }
    var flagFrame by remember { mutableStateOf<Rect?>(null) }

    var isWaitingOverlay by remember { mutableStateOf(false) }
    var waitingRetryCount by remember { mutableStateOf(0) }
    var showWaitingAlert by remember { mutableStateOf(false) }
    var showLockedAlert by remember { mutableStateOf(false) }
    var shouldGoNextAfterCutscene by remember { mutableStateOf(false) }
    var showEvolution by remember { mutableStateOf(false) }
    var evolutionLevel by remember { mutableStateOf(0) }
    var currentBlockIntroType by remember { mutableStateOf<BlockIntroType?>(null) }
    var hasPresentedInitialBlockIntro by remember { mutableStateOf(false) }
    var hasPresentedInitialTutorial by remember { mutableStateOf(false) }

    var rewardPopupQueue by remember { mutableStateOf<List<QuestRewardPopupType>>(emptyList()) }
    var currentRewardPopup by remember { mutableStateOf<QuestRewardPopupType?>(null) }

    val isTutorialTarget = chapterId.lowercase() == "ch1" && subQuestId.lowercase() == "sq1"

    val blockIntroTypeForQuest: BlockIntroType? = when {
        chapterId.lowercase() == "ch1" && subQuestId.lowercase() == "sq3" -> BlockIntroType.TURN_LEFT
        chapterId.lowercase() == "ch1" && subQuestId.lowercase() == "sq4" -> BlockIntroType.TURN_RIGHT
        chapterId.lowercase() == "ch2" && subQuestId.lowercase() == "sq1" -> BlockIntroType.ATTACK
        chapterId.lowercase() == "ch3" && subQuestId.lowercase() == "sq1" -> BlockIntroType.REPEAT_LOOP
        chapterId.lowercase() == "ch4" && subQuestId.lowercase() == "sq1" -> BlockIntroType.CONDITION
        else -> null
    }

    val tutorialKey = "tutorial.quest.${chapterId.lowercase()}.${subQuestId.lowercase()}"

    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()

    val pendingEvolutionLevel: Int? = run {
        val pending = userProfile?.character?.evolutionPending ?: false
        if (!pending) return@run null

        val lv = userProfile?.character?.evolutionLevel ?: 0
        if (listOf(5, 10, 15).contains(lv)) {
            lv
        } else {
            null
        }
    }

    fun moveToNextOrOutro() {
        val reward = viewModel.successReward ?: return

        if (reward.isChapterCleared && !viewModel.wasOutroShown(viewModel.currentChapterId)) {
            shouldGoNextAfterCutscene = true
            viewModel.presentOutroAfterChapterReward(viewModel.currentChapterId)
        } else {
            isWaitingOverlay = true
            waitingRetryCount = 0

            tryGoNext(
                viewModel = viewModel,
                onGoNextSubQuest = onGoNextSubQuest,
                onExitToList = onExitToList,
                tabBarViewModel = tabBarViewModel,
                setWaiting = { isWaitingOverlay = it },
                setRetryCount = { waitingRetryCount = it },
                onShowWaitingAlert = { showWaitingAlert = true },
                onShowLockedAlert = { showLockedAlert = true }
            )
        }
    }

    fun showNextRewardPopupOrContinue() {
        if (rewardPopupQueue.isNotEmpty()) {
            currentRewardPopup = rewardPopupQueue.first()
            rewardPopupQueue = rewardPopupQueue.drop(1)
        } else {
            currentRewardPopup = null
            moveToNextOrOutro()
        }
    }

    fun startPostSuccessFlow() {
        val extra = viewModel.extraRewardData

        println("🎁 startPostSuccessFlow extra = $extra")

        rewardPopupQueue = buildList {
            if (extra.earnedBadgeIds.isNotEmpty()) {
                add(QuestRewardPopupType.BADGE)
            }

            if (extra.memoryFragmentGranted) {
                add(QuestRewardPopupType.MEMORY_FRAGMENT)
            }

            if (extra.hiddenChapterUnlockedNow) {
                add(QuestRewardPopupType.HIDDEN_CHAPTER_OPEN)
            }
        }

        println("🎁 rewardPopupQueue = $rewardPopupQueue")

        showNextRewardPopupOrContinue()
    }

    LaunchedEffect(subQuestId) {
        tabBarViewModel.isTabBarVisible = false

        hasPresentedInitialTutorial = false
        hasPresentedInitialBlockIntro = false
        currentBlockIntroType = null

        rewardPopupQueue = emptyList()
        currentRewardPopup = null
        showEvolution = false
        shouldGoNextAfterCutscene = false

        viewModel.fetchSubQuest(chapterId, subQuestId)
    }

    // subQuest 로드 후 튜토리얼/블록인트로 트리거
    LaunchedEffect(viewModel.subQuest?.id) {
        if (viewModel.subQuest == null) return@LaunchedEffect

        delay(200)

        if (isTutorialTarget && !viewModel.isShowingCutscene && !hasPresentedInitialTutorial) {
            hasPresentedInitialTutorial = true
            tutorialVM.startTutorial(tutorialKey)
        } else if (blockIntroTypeForQuest != null && !viewModel.isShowingCutscene && !hasPresentedInitialBlockIntro) {
            hasPresentedInitialBlockIntro = true
            currentBlockIntroType = blockIntroTypeForQuest
        }
    }

    // 컷씬 닫힌 후 트리거
    LaunchedEffect(viewModel.isShowingCutscene) {
        if (!viewModel.isShowingCutscene) {
            if (shouldGoNextAfterCutscene) {
                shouldGoNextAfterCutscene = false
                isWaitingOverlay = true
                waitingRetryCount = 0

                tryGoNext(
                    viewModel = viewModel,
                    onGoNextSubQuest = onGoNextSubQuest,
                    onExitToList = onExitToList,
                    tabBarViewModel = tabBarViewModel,
                    setWaiting = { isWaitingOverlay = it },
                    setRetryCount = { waitingRetryCount = it },
                    onShowWaitingAlert = { showWaitingAlert = true },
                    onShowLockedAlert = { showLockedAlert = true }
                )

                return@LaunchedEffect
            }

            if (isTutorialTarget && !hasPresentedInitialTutorial) {
                hasPresentedInitialTutorial = true
                delay(150)
                tutorialVM.startTutorial(tutorialKey)
            } else if (blockIntroTypeForQuest != null && !hasPresentedInitialBlockIntro) {
                hasPresentedInitialBlockIntro = true
                delay(150)
                currentBlockIntroType = blockIntroTypeForQuest
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 게임 맵
            if (viewModel.subQuest != null) {
                GameMapView(
                    viewModel = viewModel,
                    questTitle = viewModel.subQuest?.title ?: "",
                    subQuestId = subQuestId,
                    authViewModel = authViewModel,
                    onStoryButtonPositioned = { storyButtonFrame = it },
                    onPlayButtonPositioned = { playButtonFrame = it },
                    onStopButtonPositioned = { stopButtonFrame = it },
                    onFlagPositioned = { flagFrame = it },
                    onExit = {
                        viewModel.stopExecution()
                        onExitToList()
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .height(450.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // 블록 영역
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                BlockPaletteView(
                    viewModel = viewModel,
                    dragManager = dragManager
                )

                BlockCanvasView(
                    viewModel = viewModel,
                    dragManager = dragManager
                )
            }
        }

        // 드래그 중 따라다니는 고스트 블럭
        GhostBlockOverlay(
            dragManager = dragManager
        )

        // Waiting Overlay
        if (isWaitingOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(color = Color.White)

                    Text(
                        text = "다음 퀘스트 여는 중입니다…",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }

        // 보상 로딩 Overlay
        if (viewModel.isRewardLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(color = Color.White)

                    Text(
                        text = "보상 정산 중입니다…",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }

        // 실패 다이얼로그
        if (viewModel.showFailureDialog) {
            FailureDialogView {
                viewModel.showFailureDialog = false
                viewModel.resetExecution()
            }
        }

        // 성공 다이얼로그
        if (viewModel.showSuccessDialog && viewModel.successReward != null) {
            SuccessDialogView(
                reward = viewModel.successReward!!,
                characterStage = userProfile?.character?.stage ?: "egg",
                onRetry = {
                    viewModel.showSuccessDialog = false
                    rewardPopupQueue = emptyList()
                    currentRewardPopup = null
                    viewModel.resetExecution()
                },
                onNext = {
                    viewModel.showSuccessDialog = false

                    val evoLv = pendingEvolutionLevel

                    if (evoLv != null) {
                        evolutionLevel = evoLv
                        showEvolution = true
                    } else {
                        startPostSuccessFlow()
                    }
                }
            )
        }

        // 진화 화면
        if (showEvolution) {
            EvolutionView(
                reachedLevel = evolutionLevel,
                onCompleted = {
                    showEvolution = false
                    startPostSuccessFlow()
                }
            )
        }

        // 보상 획득 팝업
        currentRewardPopup?.let { popupType ->
            RewardNoticeDialogView(
                type = when (popupType) {
                    QuestRewardPopupType.BADGE -> RewardNoticeType.BADGE
                    QuestRewardPopupType.MEMORY_FRAGMENT -> RewardNoticeType.MEMORY_FRAGMENT
                    QuestRewardPopupType.HIDDEN_CHAPTER_OPEN -> RewardNoticeType.HIDDEN_CHAPTER_OPEN
                },
                onConfirm = {
                    currentRewardPopup = null
                    showNextRewardPopupOrContinue()
                }
            )
        }

        // 컷씬
        if (viewModel.isShowingCutscene && viewModel.currentCutscene != null) {
            ChapterCutsceneScreen(
                chapterId = chapterId,
                cutscene = viewModel.currentCutscene!!,
                authViewModel = authViewModel,
                onClose = {
                    viewModel.dismissCutsceneAndMarkShown()
                }
            )
        }

        // 튜토리얼 오버레이
        if (tutorialVM.isActive) {
            QuestTutorialOverlayView(
                viewModel = tutorialVM,
                storyButtonFrame = storyButtonFrame,
                blockPaletteFrame = blockPaletteFrame,
                blockCanvasFrame = blockCanvasFrame,
                playButtonFrame = playButtonFrame,
                stopButtonFrame = stopButtonFrame,
                flagFrame = flagFrame
            )
        }

        // 블록 인트로
        currentBlockIntroType?.let { introType ->
            BlockIntroView(
                type = introType,
                onStart = {
                    currentBlockIntroType = null
                    reviewManagerVM.consumePendingReviewIfNeeded()
                }
            )
        }

        // 리뷰 팝업
        if (reviewManagerVM.shouldShowReviewPopup && reviewManagerVM.currentMilestone != null) {
            ReviewPromptView(
                milestone = reviewManagerVM.currentMilestone!!,
                onNegative = {
                    reviewManagerVM.handleNegativeFeedback()
                },
                onPositive = {
                    reviewManagerVM.handlePositiveFeedback()
                }
            )
        }
    }

    // 알럿
    if (showWaitingAlert) {
        AlertDialog(
            onDismissRequest = {
                showWaitingAlert = false
            },
            title = {
                Text("⏳ 챕터를 여는 중이에요")
            },
            text = {
                Text("서버 반영이 지연되고 있어요.\n잠시 후 다시 시도해 주세요.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWaitingAlert = false
                    }
                ) {
                    Text("확인")
                }
            }
        )
    }

    if (showLockedAlert) {
        AlertDialog(
            onDismissRequest = {
                showLockedAlert = false
            },
            title = {
                Text("🔒 잠긴 퀘스트입니다")
            },
            text = {
                Text("선행 퀘스트를 먼저 완료해 주세요.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLockedAlert = false
                    }
                ) {
                    Text("확인")
                }
            }
        )
    }
}

private fun tryGoNext(
    viewModel: QuestViewModel,
    onGoNextSubQuest: (String) -> Unit,
    onExitToList: () -> Unit,
    tabBarViewModel: TabBarViewModel,
    setWaiting: (Boolean) -> Unit,
    setRetryCount: (Int) -> Unit,
    onShowWaitingAlert: () -> Unit,
    onShowLockedAlert: () -> Unit,
    retryCount: Int = 0
) {
    viewModel.goToNextSubQuest { action ->
        when (action) {
            is NextQuestAction.GoToQuest -> {
                setWaiting(false)
                onGoNextSubQuest(action.id)
            }

            is NextQuestAction.GoToList -> {
                setWaiting(false)
                tabBarViewModel.isTabBarVisible = true
                onExitToList()
            }

            is NextQuestAction.Waiting -> {
                val next = retryCount + 1
                setRetryCount(next)

                if (next <= 6) {
                    kotlinx.coroutines.MainScope().launch {
                        kotlinx.coroutines.delay(600)

                        tryGoNext(
                            viewModel = viewModel,
                            onGoNextSubQuest = onGoNextSubQuest,
                            onExitToList = onExitToList,
                            tabBarViewModel = tabBarViewModel,
                            setWaiting = setWaiting,
                            setRetryCount = setRetryCount,
                            onShowWaitingAlert = onShowWaitingAlert,
                            onShowLockedAlert = onShowLockedAlert,
                            retryCount = next
                        )
                    }
                } else {
                    setWaiting(false)
                    onShowWaitingAlert()
                }
            }

            is NextQuestAction.Locked -> {
                setWaiting(false)
                onShowLockedAlert()
            }
        }
    }
}

