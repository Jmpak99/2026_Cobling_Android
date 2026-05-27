package com.cobling.app.ui.shared

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cobling.app.R
import com.cobling.app.model.SuccessReward

@Composable
fun SuccessDialogView(
    reward: SuccessReward,
    characterStage: String,
    onRetry: () -> Unit,
    onNext: () -> Unit
) {
    var displayedLevel by remember(reward.previousLevel) {
        mutableStateOf(reward.previousLevel)
    }
    var startLevel by remember(reward.previousLevel) {
        mutableStateOf(reward.previousLevel)
    }
    var displayedExp by remember(reward.previousExp) {
        mutableStateOf(reward.previousExp)
    }
    var displayedMaxExp by remember { mutableStateOf(100f) }
    var showExtraRewardStage by remember { mutableStateOf(false) }
    var isAnimatingTwoStage by remember { mutableStateOf(false) }

    val dailyMissionGain = reward.dailyMissionRewardExp
    val monthlyMissionGain = reward.monthlyMissionRewardExp
    val totalExtraRewardGain = reward.chapterBonusExp + dailyMissionGain + monthlyMissionGain
    val shouldShowExtraRewardLine = totalExtraRewardGain > 0

    /**
     * 중요:
     * startLevel은 LevelUpProgressView 내부 계산 결과라서,
     * 제목/상태 판단의 기준으로 바로 쓰면 비정상적으로 "레벨업!"이 뜰 수 있음.
     *
     * 따라서 레벨업 문구는 "시작 레벨이 최종 레벨보다 작고",
     * "애니메이션 계산이 끝난 뒤에도 실제 표시 레벨이 reward.level 이상일 때"만 보여줌.
     */
    val didLevelUpForDisplay = reward.level > reward.previousLevel

    val nextButtonTitle = when {
        isAnimatingTwoStage -> "정산 중..."
        else -> "다음"
    }

    val safeStage = characterStage
        .trim()
        .lowercase()
        .takeIf { it in setOf("egg", "kid", "cobling", "legend") }
        ?: "egg"

    LaunchedEffect(reward) {
        displayedLevel = reward.level
        displayedExp = reward.currentExp.toFloat()
        isAnimatingTwoStage = shouldShowExtraRewardLine
        showExtraRewardStage = false
        startLevel = reward.previousLevel
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.58f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .padding(horizontal = 28.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (didLevelUpForDisplay) "🎉 레벨업!" else "🎉 성공!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = if (didLevelUpForDisplay) {
                        "코블링이 한 단계 진화했어!"
                    } else {
                        "코블링이 한 단계 성장했어!"
                    },
                    fontSize = 14.sp,
                    color = Color.Black
                )

                Icon(
                    painter = painterResource(id = characterAssetResId(safeStage)),
                    contentDescription = "코블링 캐릭터",
                    modifier = Modifier.size(130.dp),
                    tint = Color.Unspecified
                )

                if (didLevelUpForDisplay) {
                    Text(
                        text = "Lv.$startLevel → Lv.${reward.level}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                } else {
                    Text(
                        text = "Lv. ${reward.level}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                LevelUpProgressView(
                    startLevel = reward.previousLevel,
                    startExp = reward.previousExp,
                    finalLevel = reward.level,
                    finalExp = reward.currentExp,
                    subQuestGain = reward.gainedExp.toFloat(),
                    chapterBonusGain = totalExtraRewardGain.toFloat(),
                    enableTwoStage = shouldShowExtraRewardLine,
                    onDisplayedLevelChange = { level ->
                        displayedLevel = level
                    },
                    onDisplayedExpChange = { exp, maxExp ->
                        displayedExp = exp
                        displayedMaxExp = maxExp
                    },
                    onSecondStageStart = {
                        showExtraRewardStage = true
                    },
                    onAllStagesFinished = {
                        isAnimatingTwoStage = false
                    },
                    onStartComputed = { computedStartLevel, computedStartExp, computedStartMax ->
                        startLevel = computedStartLevel
                        displayedExp = computedStartExp
                        displayedMaxExp = computedStartMax
                    }
                )

                Text(
                    text = "${displayedExp.toInt()} / ${displayedMaxExp.toInt()} EXP",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "+${reward.gainedExp} EXP",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    if (shouldShowExtraRewardLine) {
                        Text(
                            text = if (showExtraRewardStage) {
                                "+$totalExtraRewardGain EXP (추가 보상)"
                            } else {
                                " "
                            },
                            fontSize = 12.sp,
                            color = Color(0xFF7A5A00)
                        )
                    }

                    if (showExtraRewardStage && reward.chapterBonusExp > 0) {
                        Text(
                            text = "챕터 보너스 +${reward.chapterBonusExp} EXP",
                            fontSize = 12.sp,
                            color = Color(0xFF7A5A00)
                        )
                    }

                    if (showExtraRewardStage && dailyMissionGain > 0) {
                        Text(
                            text = "일일 미션 +$dailyMissionGain EXP",
                            fontSize = 12.sp,
                            color = Color(0xFF1B5E20)
                        )
                    }

                    if (showExtraRewardStage && monthlyMissionGain > 0) {
                        Text(
                            text = "월간 미션 +$monthlyMissionGain EXP",
                            fontSize = 12.sp,
                            color = Color(0xFF4A148C)
                        )
                    }
                }

                if (reward.isChapterCleared) {
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFFFF2CC), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD475),
                            modifier = Modifier.size(13.dp)
                        )

                        Text(
                            text = "챕터 클리어!",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }

                if (reward.isPerfectClear) {
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFFFF3CD), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB703),
                            modifier = Modifier.size(11.dp)
                        )

                        Text(
                            text = "완벽한 해결",
                            fontSize = 12.sp,
                            color = Color(0xFF7A5A00)
                        )
                    }
                }

                if (reward.didJustCompleteDailyMission) {
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "일일 미션 달성!",
                            fontSize = 12.sp,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }

                if (reward.didJustCompleteMonthlyMission) {
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFF3E5F5), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "월간 미션 달성!",
                            fontSize = 12.sp,
                            color = Color(0xFF4A148C)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEDEBE5)
                        )
                    ) {
                        Text(
                            text = "다시하기",
                            color = Color.Black
                        )
                    }

                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        enabled = !isAnimatingTwoStage,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD475),
                            disabledContainerColor = Color(0xFFFFD475).copy(alpha = 0.55f)
                        )
                    ) {
                        Text(
                            text = nextButtonTitle,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

private fun characterAssetResId(stage: String): Int {
    return when (stage) {
        "egg" -> R.drawable.cobling_stage_egg_front
        "kid" -> R.drawable.cobling_stage_kid_front
        "cobling" -> R.drawable.cobling_stage_cobling_front
        "legend" -> R.drawable.cobling_stage_legend_front
        else -> R.drawable.cobling_stage_egg_front
    }
}