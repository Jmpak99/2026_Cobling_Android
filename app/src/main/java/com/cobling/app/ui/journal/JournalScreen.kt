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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cobling.app.R
import com.cobling.app.model.JournalBadgeItem
import com.cobling.app.model.JournalMemoryFragment
import com.cobling.app.viewmodel.JournalViewModel

@Composable
fun JournalScreen(
    onMemoryMoreClick: () -> Unit = {},
    viewModel: JournalViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val memoryFragments = uiState.memoryFragments
    val badges = uiState.badges

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 56.dp, bottom = 120.dp)
    ) {
        Text(
            text = "기록장",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3A2A2A)
        )

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "기록장을 불러오는 중이에요...",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF7A6666)
            )

            return@Column
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = uiState.errorMessage,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFB00020)
            )

            return@Column
        }

        Spacer(modifier = Modifier.height(30.dp))

        SectionHeader(
            title = "기억의 조각",
            showMore = true,
            onMoreClick = onMemoryMoreClick
        )

        Spacer(modifier = Modifier.height(18.dp))

        MemoryPreviewGrid(
            memoryFragments = memoryFragments
        )

        Spacer(modifier = Modifier.height(42.dp))

        SectionHeader(
            title = "배지",
            showMore = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        BadgePreviewGrid(
            badges = badges
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    showMore: Boolean,
    onMoreClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3A2A2A)
        )

        if (showMore) {
            Text(
                modifier = Modifier.clickable {
                    onMoreClick()
                },
                text = "모두보기",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4A3535)
            )
        }
    }
}

@Composable
private fun MemoryPreviewGrid(
    memoryFragments: List<JournalMemoryFragment>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        memoryFragments.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { item ->
                    MemoryFragmentCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun MemoryFragmentCard(
    item: JournalMemoryFragment
) {
    Box(
        modifier = Modifier
            .size(82.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (item.isCollected) {
                    Color(0xFFFFF7F7)
                } else {
                    Color(0xFFFFEFEF)
                }
            )
            .border(
                width = 1.dp,
                color = Color(0xFFE3CACA),
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (item.isCollected) {
            Image(
                painter = painterResource(id = R.drawable.item_memory_fragment),
                contentDescription = item.title,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = "?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFBCAAAA)
            )
        }
    }
}

@Composable
private fun BadgePreviewGrid(
    badges: List<JournalBadgeItem>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        badges.chunked(3).forEach { rowBadges ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowBadges.forEach { badge ->
                    BadgePreviewCard(
                        modifier = Modifier.weight(1f),
                        badge = badge
                    )
                }

                repeat(3 - rowBadges.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BadgePreviewCard(
    modifier: Modifier = Modifier,
    badge: JournalBadgeItem
) {
    Column(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF7F7))
            .border(
                width = 1.dp,
                color = Color(0xFFE3CACA),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    if (badge.isUnlocked) {
                        Color(0xFFFFDFA3)
                    } else {
                        Color(0xFFE8DADA)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (badge.isUnlocked) "🏅" else "🔒",
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (badge.isUnlocked) badge.title else "???",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3A2A2A),
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (badge.isUnlocked) {
                badge.description
            } else {
                "아직 획득하지 못했어요"
            },
            fontSize = 10.sp,
            color = Color(0xFF7A6666),
            textAlign = TextAlign.Center,
            lineHeight = 13.sp,
            maxLines = 2
        )
    }
}