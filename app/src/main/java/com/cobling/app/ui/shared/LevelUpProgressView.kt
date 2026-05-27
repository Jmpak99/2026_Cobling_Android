package com.cobling.app.ui.shared

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val EXP_TABLE = mapOf(
    1 to 100f, 2 to 120f, 3 to 160f, 4 to 200f, 5 to 240f,
    6 to 310f, 7 to 380f, 8 to 480f, 9 to 600f, 10 to 750f,
    11 to 930f, 12 to 1160f, 13 to 1460f, 14 to 1820f, 15 to 2270f,
    16 to 2840f, 17 to 3550f, 18 to 4440f, 19 to 5550f
)

fun maxExpForLevel(level: Int): Float = EXP_TABLE[level] ?: 100f

@Composable
fun LevelUpProgressView(
    startLevel: Int,
    startExp: Float,
    finalLevel: Int,
    finalExp: Float,
    subQuestGain: Float,
    chapterBonusGain: Float,
    enableTwoStage: Boolean,
    onDisplayedLevelChange: (Int) -> Unit,
    onDisplayedExpChange: (Float, Float) -> Unit,
    onSecondStageStart: () -> Unit,
    onAllStagesFinished: () -> Unit,
    onStartComputed: (Int, Float, Float) -> Unit
) {
    var animLevel by remember(startLevel, startExp, finalLevel, finalExp) {
        mutableStateOf(startLevel)
    }

    var animExp by remember(startLevel, startExp, finalLevel, finalExp) {
        mutableStateOf(startExp)
    }

    var animMaxExp by remember(startLevel) {
        mutableStateOf(maxExpForLevel(startLevel))
    }

    val progressRatio = if (animMaxExp > 0f) {
        (animExp / animMaxExp).coerceIn(0f, 1f)
    } else {
        0f
    }

    val percentText = if (animMaxExp > 0f) {
        ((animExp / animMaxExp) * 100).toInt()
    } else {
        0
    }

    LaunchedEffect(startLevel, startExp, finalLevel, finalExp, subQuestGain, chapterBonusGain) {
        animLevel = startLevel
        animExp = startExp
        animMaxExp = maxExpForLevel(startLevel)

        onDisplayedLevelChange(animLevel)
        onDisplayedExpChange(animExp, animMaxExp)
        onStartComputed(animLevel, animExp, animMaxExp)

        delay(100)

        animateGain(
            startLevel = animLevel,
            startExp = animExp,
            amount = subQuestGain,
            onLevelChange = { lvl, exp, maxExp ->
                animLevel = lvl
                animExp = exp
                animMaxExp = maxExp
                onDisplayedLevelChange(lvl)
                onDisplayedExpChange(exp, maxExp)
            }
        )

        if (enableTwoStage && chapterBonusGain > 0f) {
            delay(650)
            onSecondStageStart()
            delay(200)

            animateGain(
                startLevel = animLevel,
                startExp = animExp,
                amount = chapterBonusGain,
                onLevelChange = { lvl, exp, maxExp ->
                    animLevel = lvl
                    animExp = exp
                    animMaxExp = maxExp
                    onDisplayedLevelChange(lvl)
                    onDisplayedExpChange(exp, maxExp)
                }
            )
        }

        // 마지막 값 보정
        animLevel = finalLevel
        animExp = finalExp
        animMaxExp = maxExpForLevel(finalLevel)

        onDisplayedLevelChange(finalLevel)
        onDisplayedExpChange(finalExp, maxExpForLevel(finalLevel))

        onAllStagesFinished()
    }

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(14.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E5E5))
        )

        Row(modifier = Modifier.width(220.dp)) {
            Box(
                modifier = Modifier
                    .width((progressRatio * 220).dp.coerceAtLeast(12.dp))
                    .height(14.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD475))
            )
        }

        Text(
            text = "$percentText%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
    }
}

private suspend fun animateGain(
    startLevel: Int,
    startExp: Float,
    amount: Float,
    onLevelChange: (Int, Float, Float) -> Unit
) {
    var level = startLevel
    var exp = startExp
    var remaining = amount.coerceAtLeast(0f)

    while (remaining > 0f) {
        val maxExp = maxExpForLevel(level)
        val space = (maxExp - exp).coerceAtLeast(0f)

        if (remaining < space) {
            val target = exp + remaining
            remaining = 0f

            val steps = 20
            val stepSize = (target - exp) / steps

            repeat(steps) {
                exp += stepSize
                onLevelChange(level, exp.coerceAtMost(target), maxExp)
                delay(42)
            }

            exp = target
            onLevelChange(level, exp, maxExp)
        } else {
            remaining -= space

            val steps = 15
            val stepSize = if (steps > 0) space / steps else space

            repeat(steps) {
                exp += stepSize
                val maxE = maxExpForLevel(level)
                onLevelChange(level, exp.coerceAtMost(maxE), maxE)
                delay(43)
            }

            delay(120)

            level++
            exp = 0f

            val newMax = maxExpForLevel(level)
            onLevelChange(level, 0f, newMax)

            delay(50)
        }
    }
}