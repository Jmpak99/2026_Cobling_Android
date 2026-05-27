package com.cobling.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.R
import com.cobling.app.model.BlockType

// ─────────────────────────────────
// SpeechBubbleView
// ─────────────────────────────────
@Composable
fun SpeechBubbleView(message: String) {
    Column(
        modifier = Modifier
            .widthIn(max = 250.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.spirit_forest),
                contentDescription = "정령",
                modifier = Modifier.size(40.dp),
                tint = Color.Unspecified
            )
            Text(
                text = "정령의 속삭임",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

// ─────────────────────────────────
// ReviewPromptView
// ─────────────────────────────────
@Composable
fun ReviewPromptView(
    milestone: Int,
    onNegative: () -> Unit,
    onPositive: () -> Unit
) {
    val headerEmoji = when (milestone) { 5 -> "🌱"; 15 -> "🎉"; 30 -> "🏆"; else -> "✨" }
    val titleText = when (milestone) {
        5  -> "코블링과 첫 5개의 서브퀘스트를\n완료했어요!"
        15 -> "코블링과 15개의 서브퀘스트를\n완료했어요!"
        30 -> "코블링과 30개의 서브퀘스트를\n완료했어요!"
        else -> "코블링을 재미있게 사용하고 계신가요?"
    }
    val subtitleText = when (milestone) {
        5  -> "써보니 어떠셨나요?"
        15 -> "여기까지의 경험이 궁금해요."
        30 -> "코블링이 마음에 드셨다면 응원 부탁드려요!"
        else -> "앱 사용 경험을 들려주세요."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(270.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF161C2E))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(headerEmoji, fontSize = 18.sp)
            Text(titleText, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitleText, fontSize = 12.sp, color = Color.White.copy(alpha = 0.78f))
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(
                    onClick = onNegative,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                ) { Text("별로에요", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White) }
                TextButton(
                    onClick = onPositive,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                ) { Text("좋았어요", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White) }
            }
        }
    }
}

// ─────────────────────────────────
// GhostBlockView (드래그 고스트)
// ─────────────────────────────────
@Composable
fun GhostBlockView(
    type: BlockType,
    positionX: Float,
    positionY: Float
) {
    val (wDp, hDp) = when (type) {
        BlockType.REPEAT_COUNT -> Pair(165, 36)
        BlockType.START        -> Pair(160, 50)
        else                   -> Pair(120, 30)
    }
    Box(
        modifier = Modifier
            .offset(
                x = (positionX - wDp / 2).dp,
                y = (positionY - hDp / 2).dp
            )
            .size(wDp.dp, hDp.dp)
    ) {
        Icon(
            painter = painterResource(id = blockDrawableId(type)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            tint = Color.Unspecified.copy(alpha = 0.6f)
        )
    }
}

private fun blockDrawableId(type: BlockType): Int = when (type) {
    BlockType.START          -> R.drawable.block_start
    BlockType.MOVE_FORWARD   -> R.drawable.block_move
    BlockType.TURN_LEFT      -> R.drawable.block_turn_left
    BlockType.TURN_RIGHT     -> R.drawable.block_turn_right
    BlockType.ATTACK         -> R.drawable.block_attack
    BlockType.REPEAT_COUNT   -> R.drawable.block_repeat_count
    BlockType.REPEAT_FOREVER -> R.drawable.block_repeat_forever
    BlockType.IF             -> R.drawable.block_if
    BlockType.IF_ELSE        -> R.drawable.block_if_else
    else                     -> R.drawable.block_move
}
