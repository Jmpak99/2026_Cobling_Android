package com.cobling.app.ui.quest.blockintro

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.R

enum class BlockIntroType { ATTACK, REPEAT_LOOP, CONDITION, TURN_LEFT, TURN_RIGHT }

data class BlockIntroContent(
    val title: String, val subtitle: String, val description: String,
    val exampleTitle: String, val exampleCaption: String,
    val imageName: String, val exampleImageName: String?, val buttonTitle: String
)

fun BlockIntroType.content(): BlockIntroContent = when (this) {
    BlockIntroType.ATTACK -> BlockIntroContent(
        "새로운 블록을 배웠어요!", "공격 블록",
        "캐릭터가 바라보는 방향의\n몬스터를 공격해요. \n적 앞에서 사용해야 효과가 있어요.",
        "사용 예시", "몬스터 앞까지 이동한 후\n공격하기 블록을 놓으면\n몬스터를 물리칠 수 있어요.",
        "block_attack", null, "시작하기"
    )
    BlockIntroType.REPEAT_LOOP -> BlockIntroContent(
        "새로운 블록을 배웠어요!", "반복 블록",
        "블록 묶음을 N번 반복해서 실행해요.\n같은 동작을 여러 번 써야 할 때\n훨씬 간단하게 만들 수 있어요!",
        "사용 예시", "앞으로 가기를 3번 쓰는 대신\n반복 블록 안에 앞으로 가기를 넣고\n3번 반복하면 똑같이 동작해요.",
        "block_repeat_count", "example_repeat_count", "시작하기"
    )
    BlockIntroType.CONDITION -> BlockIntroContent(
        "새로운 블록을 배웠어요!", "조건 블록",
        "상황에 따라 다른 행동을 할 수 있어요.\n앞이 막혀 있는지, 적이 있는지 확인하고 알맞게 움직여보세요.",
        "사용 예시", "앞이 막혀있으면 공격하도록 만들 수 있어요.",
        "block_if", "example_if", "시작하기"
    )
    BlockIntroType.TURN_LEFT -> BlockIntroContent(
        "새로운 블록을 배웠어요!", "왼쪽으로 돌기 블록",
        "캐릭터가 제자리에서 왼쪽을 바라보도록 \n 방향을 바꿔요.\n이동하지 않고, \n 제자리에서 방향만 바꾸는 블록이에요.",
        "사용 예시", "앞으로 가기 전에 왼쪽으로 돌기 블록을 사용하면, 캐릭터가 제자리에서 왼쪽 방향을 바라보게 돼요.",
        "block_turn_left", null, "시작하기"
    )
    BlockIntroType.TURN_RIGHT -> BlockIntroContent(
        "새로운 블록을 배웠어요!", "오른쪽으로 돌기 블록",
        "캐릭터가 제자리에서 오른쪽을 바라보도록 방향을 바꿔요.\n이동하지 않고, 제자리에서 방향만 바꾸는 블록이에요.",
        "사용 예시", "앞으로 가기 전에 오른쪽으로 돌기 블록을 사용하면, 캐릭터가 제자리에서 오른쪽 방향을 바라보게 돼요.",
        "block_turn_right", null, "시작하기"
    )
}

@Composable
fun BlockIntroView(type: BlockIntroType, onStart: () -> Unit) {
    val content = type.content()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(28.dp))
                .padding(vertical = 28.dp, horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = content.title,
                fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2E3A2D), textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))

            // 블록 이미지
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFF6F8EF)),
                contentAlignment = Alignment.Center
            ) {
                val imgId = blockIntroImageRes(content.imageName)
                if (imgId != 0) {
                    Icon(
                        painter = painterResource(id = imgId),
                        contentDescription = content.subtitle,
                        modifier = Modifier.size(110.dp),
                        tint = Color.Unspecified
                    )
                }
            }
            Spacer(modifier = Modifier.height(22.dp))

            Text(content.subtitle, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E3A2D), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = content.description,
                fontSize = 16.sp, fontWeight = FontWeight.Medium,
                color = Color(0xFF4C5B48), textAlign = TextAlign.Center, lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(22.dp))

            // 예시 박스
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFFF8E9))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(content.exampleTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A6E52))
                content.exampleImageName?.let { exImg ->
                    val exId = blockIntroImageRes(exImg)
                    if (exId != 0) {
                        Icon(
                            painter = painterResource(id = exId),
                            contentDescription = null,
                            modifier = Modifier.height(70.dp).fillMaxWidth(),
                            tint = Color.Unspecified
                        )
                    }
                }
                Text(content.exampleCaption, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5F6B5A), lineHeight = 18.sp)
            }
            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B8F5D))
            ) {
                Text(content.buttonTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

private fun blockIntroImageRes(name: String): Int = when (name) {
    "block_attack"        -> R.drawable.block_attack
    "block_repeat_count"  -> R.drawable.block_repeat_count
    "block_if"            -> R.drawable.block_if
    "block_turn_left"     -> R.drawable.block_turn_left
    "block_turn_right"    -> R.drawable.block_turn_right
    "example_repeat_count"-> R.drawable.example_repeat_count
    "example_if"          -> R.drawable.example_if
    else -> 0
}
