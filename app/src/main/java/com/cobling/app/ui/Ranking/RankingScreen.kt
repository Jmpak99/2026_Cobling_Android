package com.cobling.app.ui.Ranking

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.model.RankUser
import com.cobling.app.model.SortType
import com.cobling.app.viewmodel.RankingViewModel

private val Rank1Color = Color(0xFF85B7EB)
private val Rank2Color = Color(0xFFD4537E)
private val Rank3Color = Color(0xFFF4C0D1)
private val AvatarBg = Color(0xFFCECBF6)
private val SortActive = Color(0xFF7F77DD)

private fun tierColors(tier: String): Pair<Color, Color> = when (tier) {
    "S" -> Color(0xFFEEEDFE) to Color(0xFF3C3489)
    "A" -> Color(0xFFE6F1FB) to Color(0xFF0C447C)
    "B" -> Color(0xFFEAF3DE) to Color(0xFF27500A)
    else -> Color(0xFFFAEEDA) to Color(0xFF633806)
}

private fun rankRingColor(rank: Int): Color = when (rank) {
    1 -> Rank1Color
    2 -> Rank2Color
    else -> Rank3Color
}

private fun rankPedestalColor(rank: Int): Color = when (rank) {
    1 -> Rank1Color
    2 -> Rank2Color
    else -> Rank3Color
}

@Composable
fun RankingScreen(
    viewModel: RankingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val users = uiState.users
    val sortType = uiState.sortType
    val top3 = users.take(3)
    val rest = users.drop(3)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(Modifier.height(20.dp))

            Text(
                text = "랭킹",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(Modifier.height(14.dp))
        }

        item {
            SortBar(
                current = sortType,
                onSelect = { selected ->
                    viewModel.changeSortType(selected)
                }
            )

            Spacer(Modifier.height(14.dp))
        }

        when {
            uiState.isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            uiState.errorMessage != null -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "오류가 발생했습니다.",
                            fontSize = 14.sp,
                            color = Color(0xFFD4537E)
                        )
                    }
                }
            }

            users.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "아직 랭킹 데이터가 없습니다.",
                            fontSize = 14.sp,
                            color = Color(0xFF888780)
                        )
                    }
                }
            }

            else -> {
                item {
                    PodiumSection(
                        top3 = top3,
                        sortType = sortType
                    )

                    Spacer(Modifier.height(10.dp))
                }

                itemsIndexed(
                    items = rest,
                    key = { _, user -> user.id }
                ) { index, user ->
                    val rank = index + 4

                    RankRow(
                        user = user,
                        rank = rank,
                        sortType = sortType
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun SortBar(
    current: SortType,
    onSelect: (SortType) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SortType.entries.forEach { type ->
            val isActive = current == type

            Surface(
                onClick = { onSelect(type) },
                shape = RoundedCornerShape(20.dp),
                color = if (isActive) SortActive else Color.White,
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Text(
                    text = type.label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    fontSize = 13.sp,
                    color = if (isActive) Color.White else Color(0xFF888780)
                )
            }
        }
    }
}

@Composable
fun PodiumSection(
    top3: List<RankUser>,
    sortType: SortType
) {
    if (top3.size < 3) {
        return
    }

    val podiumOrder = listOf(
        top3[1] to 2,
        top3[0] to 1,
        top3[2] to 3
    )

    val avatarSizes = listOf(56.dp, 68.dp, 50.dp)
    val pedestalHeights = listOf(40.dp, 56.dp, 32.dp)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFEFEFEF)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            podiumOrder.forEachIndexed { index, pair ->
                val user = pair.first
                val rank = pair.second

                PodiumItem(
                    user = user,
                    rank = rank,
                    avatarSize = avatarSizes[index],
                    pedestalHeight = pedestalHeights[index],
                    sortType = sortType
                )
            }
        }
    }
}

@Composable
fun PodiumItem(
    user: RankUser,
    rank: Int,
    avatarSize: Dp,
    pedestalHeight: Dp,
    sortType: SortType
) {
    val ringColor = rankRingColor(rank)
    val pedestalColor = rankPedestalColor(rank)

    val scoreText = when (sortType) {
        SortType.LEVEL -> "Lv ${user.level}"
        SortType.XP -> "${user.xp} XP"
        SortType.QUESTS -> "${user.quests}개"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(avatarSize + 6.dp)
                .clip(CircleShape)
                .background(ringColor),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(AvatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.take(1),
                    fontSize = (avatarSize.value * 0.35f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3C3489)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = user.name,
            fontSize = 12.sp,
            color = Color(0xFF444441),
            maxLines = 1
        )

        Text(
            text = scoreText,
            fontSize = 10.sp,
            color = Color(0xFF888780)
        )

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(64.dp)
                .height(pedestalHeight)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(pedestalColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun RankRow(
    user: RankUser,
    rank: Int,
    sortType: SortType
) {
    val tierColor = tierColors(user.tier)
    val tierBg = tierColor.first
    val tierText = tierColor.second

    val scoreValue = when (sortType) {
        SortType.LEVEL -> "Lv ${user.level}"
        SortType.XP -> "${user.xp} XP"
        SortType.QUESTS -> "${user.quests}"
    }

    val scoreLabel = when (sortType) {
        SortType.LEVEL -> "레벨"
        SortType.XP -> "XP"
        SortType.QUESTS -> "퀘스트"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 0.dp,
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank",
                modifier = Modifier.width(24.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF888780)
            )

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AvatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.take(1),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3C3489)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )

                    Spacer(Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(tierBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = user.tier,
                            fontSize = 10.sp,
                            color = tierText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "Lv ${user.level}  ·  퀘스트 ${user.quests}개",
                    fontSize = 12.sp,
                    color = Color(0xFF888780)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = scoreValue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )

                Text(
                    text = scoreLabel,
                    fontSize = 11.sp,
                    color = Color(0xFFAAAAAA)
                )
            }
        }
    }
}