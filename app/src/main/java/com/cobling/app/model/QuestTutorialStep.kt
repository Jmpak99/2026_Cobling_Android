package com.cobling.app.model

// ──────────────────────────────────────
// QuestTutorialStep
// ──────────────────────────────────────
enum class QuestTutorialStep(val value: Int) {
    STORY_INTRO(0),
    EXPLAIN_STORY_BUTTON(1),
    EXPLAIN_BLOCK_PALETTE(2),
    EXPLAIN_PLACE_BLOCK(3),
    EXPLAIN_REMOVE_BLOCK(4),
    EXPLAIN_PLAY_BUTTON(5),
    EXPLAIN_STOP_BUTTON(6),
    EXPLAIN_REACH_FLAG(7),
    READY_TO_START(8),
    COMPLETED(9);

    val title: String get() = when (this) {
        STORY_INTRO           -> "스토리 소개"
        EXPLAIN_STORY_BUTTON  -> "힌트 버튼"
        EXPLAIN_BLOCK_PALETTE -> "블록 사용하기"
        EXPLAIN_PLACE_BLOCK   -> "블록 배치하기"
        EXPLAIN_REMOVE_BLOCK  -> "블록 제거하기"
        EXPLAIN_PLAY_BUTTON   -> "시작 버튼"
        EXPLAIN_STOP_BUTTON   -> "멈춤 버튼"
        EXPLAIN_REACH_FLAG    -> "성공 조건"
        READY_TO_START        -> "직접 시작하기"
        COMPLETED             -> "튜토리얼 종료"
    }

    val message: String get() = when (this) {
        STORY_INTRO           -> "코블링이 깨어나기 위해서는 도움이 필요해요. \n먼저 게임 화면에서 어떤 기능을 사용할지 같이 알아볼까요?"
        EXPLAIN_STORY_BUTTON  -> "힌트 버튼을 누르면 현재 퀘스트의 힌트를 확인할 수 있어요."
        EXPLAIN_BLOCK_PALETTE -> "왼쪽에는 코블링을 움직일 수 있는 블록들이 있어요. 필요한 블록을 골라 사용할 수 있어요."
        EXPLAIN_PLACE_BLOCK   -> "블록을 드래그해서 시작 블록 아래에 순서대로 배치하면 코블링의 행동이 만들어져요."
        EXPLAIN_REMOVE_BLOCK  -> "배치한 블록을 다시 왼쪽 팔레트 영역으로 드래그하면 블록을 제거할 수 있어요."
        EXPLAIN_PLAY_BUTTON   -> "블록 배치가 끝나면 시작 버튼을 눌러 코블링을 움직일 수 있어요."
        EXPLAIN_STOP_BUTTON   -> "실행 중에는 멈춤 버튼을 눌러 언제든지 중지할 수 있어요."
        EXPLAIN_REACH_FLAG    -> "코블링이 깃발에 도착하면 퀘스트 성공이에요!"
        READY_TO_START        -> "이제 게임 방법을 모두 확인했어요. \n직접 블록을 배치해서 코블링을 움직여볼까요?"
        COMPLETED             -> "튜토리얼이 종료되었어요."
    }

    val focusTarget: QuestTutorialFocusTarget? get() = when (this) {
        STORY_INTRO           -> null
        EXPLAIN_STORY_BUTTON  -> QuestTutorialFocusTarget.STORY_BUTTON
        EXPLAIN_BLOCK_PALETTE -> QuestTutorialFocusTarget.BLOCK_PALETTE
        EXPLAIN_PLACE_BLOCK   -> QuestTutorialFocusTarget.BLOCK_CANVAS
        EXPLAIN_REMOVE_BLOCK  -> QuestTutorialFocusTarget.BLOCK_PALETTE
        EXPLAIN_PLAY_BUTTON   -> QuestTutorialFocusTarget.PLAY_BUTTON
        EXPLAIN_STOP_BUTTON   -> QuestTutorialFocusTarget.STOP_BUTTON
        EXPLAIN_REACH_FLAG    -> QuestTutorialFocusTarget.FLAG
        READY_TO_START        -> null
        COMPLETED             -> null
    }

    val primaryButtonTitle: String get() = when (this) {
        READY_TO_START -> "시작하기"
        COMPLETED      -> "확인"
        else           -> "다음"
    }

    val showsSkipButton: Boolean get() = (this != COMPLETED)

    val isReadyToStartStep: Boolean get() = (this == READY_TO_START)

    val isCompletedStep: Boolean get() = (this == COMPLETED)

    val visibleStepNumber: Int? get() = when (this) {
        STORY_INTRO           -> 1
        EXPLAIN_STORY_BUTTON  -> 2
        EXPLAIN_BLOCK_PALETTE -> 3
        EXPLAIN_PLACE_BLOCK   -> 4
        EXPLAIN_REMOVE_BLOCK  -> 5
        EXPLAIN_PLAY_BUTTON   -> 6
        EXPLAIN_STOP_BUTTON   -> 7
        EXPLAIN_REACH_FLAG    -> 8
        READY_TO_START        -> 9
        COMPLETED             -> null
    }

    val totalVisibleSteps: Int get() = 9

    val nextStep: QuestTutorialStep? get() = when (this) {
        STORY_INTRO           -> EXPLAIN_STORY_BUTTON
        EXPLAIN_STORY_BUTTON  -> EXPLAIN_BLOCK_PALETTE
        EXPLAIN_BLOCK_PALETTE -> EXPLAIN_PLACE_BLOCK
        EXPLAIN_PLACE_BLOCK   -> EXPLAIN_REMOVE_BLOCK
        EXPLAIN_REMOVE_BLOCK  -> EXPLAIN_PLAY_BUTTON
        EXPLAIN_PLAY_BUTTON   -> EXPLAIN_STOP_BUTTON
        EXPLAIN_STOP_BUTTON   -> EXPLAIN_REACH_FLAG
        EXPLAIN_REACH_FLAG    -> READY_TO_START
        READY_TO_START        -> COMPLETED
        COMPLETED             -> null
    }

    val previousStep: QuestTutorialStep? get() = when (this) {
        STORY_INTRO           -> null
        EXPLAIN_STORY_BUTTON  -> STORY_INTRO
        EXPLAIN_BLOCK_PALETTE -> EXPLAIN_STORY_BUTTON
        EXPLAIN_PLACE_BLOCK   -> EXPLAIN_BLOCK_PALETTE
        EXPLAIN_REMOVE_BLOCK  -> EXPLAIN_PLACE_BLOCK
        EXPLAIN_PLAY_BUTTON   -> EXPLAIN_REMOVE_BLOCK
        EXPLAIN_STOP_BUTTON   -> EXPLAIN_PLAY_BUTTON
        EXPLAIN_REACH_FLAG    -> EXPLAIN_STOP_BUTTON
        READY_TO_START        -> EXPLAIN_REACH_FLAG
        COMPLETED             -> READY_TO_START
    }
}

// ──────────────────────────────────────
// QuestTutorialFocusTarget
// ──────────────────────────────────────
enum class QuestTutorialFocusTarget {
    STORY_BUTTON,
    BLOCK_PALETTE,
    BLOCK_CANVAS,
    PLAY_BUTTON,
    STOP_BUTTON,
    FLAG
}
