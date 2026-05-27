package com.cobling.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.model.ChapterCutscene
import com.cobling.app.model.ChapterCutsceneType
import com.cobling.app.model.ChapterDialogueStore
import com.cobling.app.ui.quest.blockintro.BlockIntroType
import com.cobling.app.ui.quest.blockintro.BlockIntroView
import com.cobling.app.ui.quest.cutscene.ChapterCutsceneScreen
import com.cobling.app.viewmodel.AuthViewModel

// ─────────────────────────────────────
// 튜토리얼 다시보기 항목 모델
// ─────────────────────────────────────

private sealed class TutorialPreviewItem {
    data class Cutscene(
        val label: String,
        val chapterId: String,
        val type: ChapterCutsceneType
    ) : TutorialPreviewItem()

    data class BlockIntro(
        val label: String,
        val type: BlockIntroType
    ) : TutorialPreviewItem()
}

// ─────────────────────────────────────
// 튜토리얼 다시보기 목록
// ─────────────────────────────────────

private val tutorialPreviewItems = listOf(
    TutorialPreviewItem.Cutscene(
        label = "Chapter 1 · 시작 컷신",
        chapterId = "ch1",
        type = ChapterCutsceneType.INTRO
    ),
    TutorialPreviewItem.Cutscene(
        label = "Chapter 1 · 클리어 컷신",
        chapterId = "ch1",
        type = ChapterCutsceneType.OUTRO
    ),

    TutorialPreviewItem.Cutscene(
        label = "Chapter 2 · 시작 컷신",
        chapterId = "ch2",
        type = ChapterCutsceneType.INTRO
    ),
    TutorialPreviewItem.Cutscene(
        label = "Chapter 2 · 클리어 컷신",
        chapterId = "ch2",
        type = ChapterCutsceneType.OUTRO
    ),
    TutorialPreviewItem.BlockIntro(
        label = "Chapter 2 · 블록 소개 - 왼쪽으로 돌기",
        type = BlockIntroType.TURN_LEFT
    ),
    TutorialPreviewItem.BlockIntro(
        label = "Chapter 2 · 블록 소개 - 오른쪽으로 돌기",
        type = BlockIntroType.TURN_RIGHT
    ),

    TutorialPreviewItem.Cutscene(
        label = "Chapter 3 · 시작 컷신",
        chapterId = "ch3",
        type = ChapterCutsceneType.INTRO
    ),
    TutorialPreviewItem.Cutscene(
        label = "Chapter 3 · 클리어 컷신",
        chapterId = "ch3",
        type = ChapterCutsceneType.OUTRO
    ),
    TutorialPreviewItem.BlockIntro(
        label = "Chapter 3 · 블록 소개 - 공격하기",
        type = BlockIntroType.ATTACK
    ),

    TutorialPreviewItem.Cutscene(
        label = "Chapter 4 · 시작 컷신",
        chapterId = "ch4",
        type = ChapterCutsceneType.INTRO
    ),
    TutorialPreviewItem.Cutscene(
        label = "Chapter 4 · 클리어 컷신",
        chapterId = "ch4",
        type = ChapterCutsceneType.OUTRO
    ),
    TutorialPreviewItem.BlockIntro(
        label = "Chapter 4 · 블록 소개 - 반복하기",
        type = BlockIntroType.REPEAT_LOOP
    ),

    TutorialPreviewItem.Cutscene(
        label = "Chapter 5 · 시작 컷신",
        chapterId = "ch5",
        type = ChapterCutsceneType.INTRO
    ),
    TutorialPreviewItem.Cutscene(
        label = "Chapter 5 · 클리어 컷신",
        chapterId = "ch5",
        type = ChapterCutsceneType.OUTRO
    ),
    TutorialPreviewItem.BlockIntro(
        label = "Chapter 5 · 블록 소개 - 조건문",
        type = BlockIntroType.CONDITION
    )
)

// ─────────────────────────────────────
// 메인 화면
// ─────────────────────────────────────

@Composable
fun CutscenePreviewScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    var playingItem by remember {
        mutableStateOf<TutorialPreviewItem?>(null)
    }

    when (val current = playingItem) {
        null -> {
            TutorialPreviewListScreen(
                onBack = onBack,
                onSelect = { selectedItem ->
                    playingItem = selectedItem
                }
            )
        }

        is TutorialPreviewItem.Cutscene -> {
            val lines = ChapterDialogueStore.lines(
                chapterId = current.chapterId,
                type = current.type
            )

            val cutscene = ChapterCutscene(
                chapterId = current.chapterId,
                type = current.type,
                lines = lines
            )

            ChapterCutsceneScreen(
                chapterId = current.chapterId,
                cutscene = cutscene,
                authViewModel = authViewModel,
                onClose = {
                    playingItem = null
                }
            )
        }

        is TutorialPreviewItem.BlockIntro -> {
            BlockIntroView(
                type = current.type,
                onStart = {
                    playingItem = null
                }
            )
        }
    }
}

// ─────────────────────────────────────
// 목록 화면
// ─────────────────────────────────────

@Composable
private fun TutorialPreviewListScreen(
    onBack: () -> Unit,
    onSelect: (TutorialPreviewItem) -> Unit
) {
    val groupedItems = tutorialPreviewItems.groupBy { item ->
        when (item) {
            is TutorialPreviewItem.Cutscene -> item.chapterId
            is TutorialPreviewItem.BlockIntro -> chapterIdByBlockIntroType(item.type)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color(0xFF1A1A1A)
                    )
                }

                Spacer(Modifier.width(4.dp))

                Text(
                    text = "튜토리얼 다시보기",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }

            Text(
                text = "이전에 봤던 컷신과 블록 소개를 다시 볼 수 있어요.",
                fontSize = 13.sp,
                color = Color(0xFF888888),
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        groupedItems.forEach { (chapterId, items) ->
            item {
                Text(
                    text = chapterTitle(chapterId),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7F77DD),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(items) { item ->
                TutorialPreviewCard(
                    item = item,
                    onClick = {
                        onSelect(item)
                    }
                )
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────
// 카드
// ─────────────────────────────────────

@Composable
private fun TutorialPreviewCard(
    item: TutorialPreviewItem,
    onClick: () -> Unit
) {
    val cardData = when (item) {
        is TutorialPreviewItem.Cutscene -> {
            val isIntro = item.type == ChapterCutsceneType.INTRO

            PreviewCardData(
                emoji = if (isIntro) "🌅" else "🏆",
                label = item.label,
                tagText = if (isIntro) "시작" else "클리어",
                tagColor = if (isIntro) Color(0xFF6BAE6E) else Color(0xFFE07B54)
            )
        }

        is TutorialPreviewItem.BlockIntro -> {
            PreviewCardData(
                emoji = "📦",
                label = item.label,
                tagText = "블록소개",
                tagColor = Color(0xFF7F77DD)
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable {
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = cardData.emoji,
            fontSize = 24.sp
        )

        Text(
            text = cardData.label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(cardData.tagColor.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = cardData.tagText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = cardData.tagColor
            )
        }

        Text(
            text = "›",
            fontSize = 18.sp,
            color = Color(0xFFCCCCCC)
        )
    }
}

// ─────────────────────────────────────
// 내부 유틸
// ─────────────────────────────────────

private fun chapterTitle(chapterId: String): String {
    return when (chapterId) {
        "ch1" -> "Chapter 1"
        "ch2" -> "Chapter 2"
        "ch3" -> "Chapter 3"
        "ch4" -> "Chapter 4"
        "ch5" -> "Chapter 5"
        else -> chapterId.uppercase()
    }
}

private fun chapterIdByBlockIntroType(type: BlockIntroType): String {
    return when (type) {
        BlockIntroType.TURN_LEFT -> "ch1"
        BlockIntroType.TURN_RIGHT -> "ch1"
        BlockIntroType.ATTACK -> "ch2"
        BlockIntroType.REPEAT_LOOP -> "ch3"
        BlockIntroType.CONDITION -> "ch4"
    }
}

private data class PreviewCardData(
    val emoji: String,
    val label: String,
    val tagText: String,
    val tagColor: Color
)