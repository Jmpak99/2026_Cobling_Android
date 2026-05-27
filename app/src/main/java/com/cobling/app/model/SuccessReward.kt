package com.cobling.app.model

data class SuccessReward(
    // 서버 기준 최종 결과
    val level: Int,
    val currentExp: Float,
    val maxExp: Float,

    // 이번 퀘스트 보상 (연출용)
    val gainedExp: Int,
    // 연출 분기용
    val isPerfectClear: Boolean,

    val previousLevel: Int = level,
    val previousExp: Float = currentExp,

    // 챕터 보너스
    val chapterBonusExp: Int,
    val isChapterCleared: Boolean,

    // 이번 클리어로 방금 달성했는지
    val didJustCompleteDailyMission: Boolean,
    val didJustCompleteMonthlyMission: Boolean,

    // 현재 완료 상태
    val isDailyMissionCompleted: Boolean,
    val isMonthlyMissionCompleted: Boolean,

    // 이번 클리어에서 실제 지급된 미션 보상 EXP
    val dailyMissionRewardExp: Int,
    val monthlyMissionRewardExp: Int
)
