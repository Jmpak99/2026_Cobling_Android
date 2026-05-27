package com.cobling.app.ui.quest.tutorial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.viewmodel.QuestTutorialViewModel
import com.cobling.app.model.QuestTutorialFocusTarget

@Composable
fun QuestTutorialOverlayView(
    viewModel: QuestTutorialViewModel,
    storyButtonFrame: Rect? = null,
    blockPaletteFrame: Rect? = null,
    blockCanvasFrame: Rect? = null,
    playButtonFrame: Rect? = null,
    stopButtonFrame: Rect? = null,
    flagFrame: Rect? = null
) {
    val currentFrame = when (viewModel.focusTarget) {
        QuestTutorialFocusTarget.STORY_BUTTON -> storyButtonFrame
        QuestTutorialFocusTarget.BLOCK_PALETTE -> blockPaletteFrame
        QuestTutorialFocusTarget.BLOCK_CANVAS -> blockCanvasFrame
        QuestTutorialFocusTarget.PLAY_BUTTON -> playButtonFrame
        QuestTutorialFocusTarget.STOP_BUTTON -> stopButtonFrame
        QuestTutorialFocusTarget.FLAG -> flagFrame
        null -> null
    }

    val expandedFrame = currentFrame?.let { frame ->
        val inset = if (viewModel.focusTarget == QuestTutorialFocusTarget.FLAG) -16f else -8f
        Rect(
            left = frame.left + inset,
            top = frame.top + inset,
            right = frame.right - inset,
            bottom = frame.bottom - inset
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 딤 배경 + 컷아웃
        if (expandedFrame != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawDimmedWithCutout(expandedFrame, cornerRadius = 18f)
            }
            // 하이라이트 테두리
            Box(
                modifier = Modifier
                    .offset(
                        x = expandedFrame.left.dp,
                        y = expandedFrame.top.dp
                    )
                    .size(
                        width = expandedFrame.width.dp,
                        height = expandedFrame.height.dp
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.14f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )
        }

        // 버블
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp, start = 16.dp, end = 16.dp)
        ) {
            QuestTutorialBubbleView(viewModel = viewModel)
        }
    }
}

private fun DrawScope.drawDimmedWithCutout(cutout: Rect, cornerRadius: Float) {
    val fullRect = Rect(Offset.Zero, size)
    val path = Path().apply {
        addRect(fullRect)
        addRoundRect(
            RoundRect(
                rect = cutout,
                cornerRadius = CornerRadius(cornerRadius)
            )
        )
    }
    drawPath(path = path, color = Color.Black.copy(alpha = 0.45f), style = Fill)
}

// ─────────────────────────────────
// QuestTutorialBubbleView
// ─────────────────────────────────
@Composable
fun QuestTutorialBubbleView(viewModel: QuestTutorialViewModel) {
    val canGoBack = viewModel.currentStep.previousStep != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(top = 18.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(viewModel.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
                viewModel.visibleStepNumber?.let { step ->
                    Text("$step/${viewModel.totalVisibleSteps}", fontSize = 12.sp, color = Color.Gray)
                }
            }
            LinearProgressIndicator(
                progress = { viewModel.progressValue },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF6B8F5D)
            )
        }

        HorizontalDivider()

        // 본문
        Text(
            text = viewModel.message,
            fontSize = 16.sp,
            color = Color(0xFF3A3A3A),
            lineHeight = 22.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        )

        HorizontalDivider()

        // 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(top = 14.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(
                onClick = { viewModel.goToPreviousStep() },
                enabled = canGoBack,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.12f))
            ) {
                Text("이전", color = if (canGoBack) Color.Black else Color.Gray, fontWeight = FontWeight.SemiBold)
            }

            if (viewModel.showsSkipButton) {
                TextButton(
                    onClick = { viewModel.skipTutorial() },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF2DC))
                ) {
                    Text("건너뛰기", color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
            }

            Button(
                onClick = { viewModel.handlePrimaryButtonTap() },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B8F5D))
            ) {
                Text(viewModel.primaryButtonTitle, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
