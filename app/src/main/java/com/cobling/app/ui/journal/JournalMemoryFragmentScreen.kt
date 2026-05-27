package com.cobling.app.ui.journal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cobling.app.R
import com.cobling.app.model.JournalMemoryFragment
import com.cobling.app.viewmodel.JournalViewModel

private val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

@Composable
fun JournalMemoryFragmentScreen(
    onBackClick: () -> Unit,
    onHiddenQuestClick: () -> Unit,
    viewModel: JournalViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val memoryFragments = uiState.memoryFragments

    val collectedCount = memoryFragments.count { it.isCollected }
    val isHiddenQuestUnlocked = collectedCount >= 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 30.dp)
            .padding(top = 56.dp, bottom = 34.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable {
                        onBackClick()
                    },
                text = "‹",
                fontSize = 46.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = Pretendard,
                color = Color(0xFF3A2A2A)
            )

            Text(
                text = "기억의 조각",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Pretendard,
                color = Color(0xFF3A2A2A)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "기억의 조각",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Pretendard,
            color = Color(0xFF3A2A2A)
        )

        Spacer(modifier = Modifier.height(76.dp))

        when {
            uiState.isLoading -> {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "기억의 조각을 불러오는 중이에요...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Pretendard,
                    color = Color(0xFF7A6666),
                    textAlign = TextAlign.Center
                )
            }

            uiState.errorMessage != null -> {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = uiState.errorMessage,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Pretendard,
                    color = Color(0xFFB00020),
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                MemoryFragmentLargeGrid(
                    memoryFragments = memoryFragments
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))

        MemoryHiddenQuestNoticeBox(
            isUnlocked = isHiddenQuestUnlocked,
            onClick = onHiddenQuestClick
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun MemoryFragmentLargeGrid(
    memoryFragments: List<JournalMemoryFragment>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        memoryFragments.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(38.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { item ->
                    MemoryFragmentLargeCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun MemoryFragmentLargeCard(
    item: JournalMemoryFragment
) {
    Box(
        modifier = Modifier
            .size(width = 120.dp, height = 120.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFD4C6C6),
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (item.isCollected) {
            Image(
                painter = painterResource(id = R.drawable.item_memory_fragment),
                contentDescription = item.title,
                modifier = Modifier
                    .size(108.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = "?",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Pretendard,
                color = Color(0xFFBCAAAA)
            )
        }
    }
}

@Composable
private fun MemoryHiddenQuestNoticeBox(
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isUnlocked) {
                    Color(0xFFFFEEF1)
                } else {
                    Color(0xFFF8F8F6)
                }
            )
            .clickable(
                enabled = isUnlocked
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isUnlocked) {
                "히든 챕터 입장하기"
            } else {
                "기억의 조각을 모두 모으면\n숨겨진 퀘스트가 열려요 !"
            },
            fontSize = if (isUnlocked) 21.sp else 19.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Pretendard,
            color = Color(0xFF4A3535),
            textAlign = TextAlign.Center,
            lineHeight = 25.sp
        )
    }
}