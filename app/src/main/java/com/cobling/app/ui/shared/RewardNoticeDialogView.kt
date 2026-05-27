package com.cobling.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class RewardNoticeType {
    BADGE,
    MEMORY_FRAGMENT,
    HIDDEN_CHAPTER_OPEN
}

@Composable
fun RewardNoticeDialogView(
    type: RewardNoticeType,
    onConfirm: () -> Unit
) {
    val icon = when (type) {
        RewardNoticeType.BADGE -> "🏅"
        RewardNoticeType.MEMORY_FRAGMENT -> "💎"
        RewardNoticeType.HIDDEN_CHAPTER_OPEN -> "✨"
    }

    val title = when (type) {
        RewardNoticeType.BADGE -> "배지를 얻었어요!"
        RewardNoticeType.MEMORY_FRAGMENT -> "기억의 조각을 얻었어요!"
        RewardNoticeType.HIDDEN_CHAPTER_OPEN -> "숨겨진 챕터가 열렸어요!"
    }

    val message = when (type) {
        RewardNoticeType.BADGE -> "기록장에서 확인해보세요!"
        RewardNoticeType.MEMORY_FRAGMENT -> "4개를 모으면 숨겨진 챕터가 열려요!"
        RewardNoticeType.HIDDEN_CHAPTER_OPEN -> "기억의 조각이 모두 모였어요!"
    }

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFFF7F7))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE8CFCF),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE6B8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 38.sp
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3A2A2A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = message,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7A6666),
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )

                Spacer(modifier = Modifier.height(26.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF4A3535)),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onConfirm
                    ) {
                        Text(
                            text = "확인",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        containerColor = Color.Transparent
    )
}