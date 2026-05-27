package com.cobling.app.ui.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.cobling.app.R
import com.cobling.app.model.QuestDocument
import com.cobling.app.model.QuestStatus
import com.cobling.app.navigation.Screen
import com.cobling.app.ui.theme.GmarketFamily
import com.cobling.app.ui.theme.PretendardFamily
import com.cobling.app.util.QuestTheme
import com.cobling.app.viewmodel.AppState
import com.cobling.app.viewmodel.QuestListViewModel
import com.cobling.app.viewmodel.TabBarViewModel

@Composable
fun QuestListScreen(
    appState: AppState,
    tabBarViewModel: TabBarViewModel,
    navController: NavHostController,
    vm: QuestListViewModel = hiltViewModel()
) {
    var showLockedAlert by remember { mutableStateOf(false) }
    var showComingSoonAlert by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tabBarViewModel.isTabBarVisible = true
        appState.isInGame = false
        vm.fetchQuests()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Text(
            text = "퀘스트",
            fontFamily = PretendardFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            color = Color.Black,
            modifier = Modifier.padding(
                start = 24.dp,
                top = 36.dp,
                bottom = 8.dp
            )
        )

        when {
            vm.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            vm.errorMessage != null -> {
                Text(
                    text = "오류: ${vm.errorMessage}",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        bottom = 80.dp,
                        start = 24.dp,
                        end = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(vm.quests) { (quest, status, isPerfect) ->
                        QuestCardWrapper(
                            quest = quest,
                            status = status,
                            isPerfectChapter = isPerfect,
                            onLockedTap = {
                                showLockedAlert = true
                            },
                            onTap = {
                                navController.navigate(
                                    Screen.QuestDetail.createRoute(quest.id)
                                )
                            }
                        )
                    }

                    item {
                        ComingSoonQuestCard(
                            chapterNumber = 6,
                            title = "새로운 모험이 곧 시작돼요",
                            subtitle = "업데이트를 기다려 주세요",
                            onTap = {
                                showComingSoonAlert = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showLockedAlert) {
        AlertDialog(
            onDismissRequest = {
                showLockedAlert = false
            },
            title = {
                Text("잠긴 퀘스트입니다")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLockedAlert = false
                    }
                ) {
                    Text("확인")
                }
            }
        )
    }

    if (showComingSoonAlert) {
        AlertDialog(
            onDismissRequest = {
                showComingSoonAlert = false
            },
            title = {
                Text("Coming Soon")
            },
            text = {
                Text("새로운 챕터가 곧 업데이트될 예정입니다 🚀")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showComingSoonAlert = false
                    }
                ) {
                    Text("확인")
                }
            }
        )
    }
}

@Composable
private fun QuestCardWrapper(
    quest: QuestDocument,
    status: QuestStatus,
    isPerfectChapter: Boolean,
    onLockedTap: () -> Unit,
    onTap: () -> Unit
) {
    val bgColor = QuestTheme.backgroundColor(quest.order)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable {
                if (status == QuestStatus.LOCKED) {
                    onLockedTap()
                } else {
                    onTap()
                }
            }
    ) {
        QuestCardContent(
            title = quest.title,
            subtitle = quest.subtitle,
            status = status,
            isPerfectChapter = isPerfectChapter
        )
    }
}

@Composable
private fun QuestCardContent(
    title: String,
    subtitle: String,
    status: QuestStatus,
    isPerfectChapter: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(125.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(95.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp
                    )
                )
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontFamily = GmarketFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )

                    Text(
                        text = subtitle,
                        fontFamily = PretendardFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                val iconId = statusIconRes(
                    status = status,
                    isPerfect = isPerfectChapter
                )

                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    modifier = Modifier.size(
                        width = when {
                            status == QuestStatus.COMPLETED && isPerfectChapter -> 110.dp
                            status == QuestStatus.IN_PROGRESS -> 110.dp
                            else -> 92.dp
                        },
                        height = 42.dp
                    ),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

@Composable
private fun ComingSoonQuestCard(
    chapterNumber: Int,
    title: String,
    subtitle: String,
    onTap: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Gray.copy(alpha = 0.18f))
            .clickable {
                onTap()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White.copy(alpha = 0.7f))
                        .padding(horizontal = 26.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Coming Soon",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(125.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 20.dp,
                            bottomEnd = 20.dp
                        )
                    )
                    .background(Color.White.copy(alpha = 0.95f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "챕터 $chapterNumber",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    Text(
                        text = title,
                        fontSize = 14.sp,
                        color = Color.Black
                    )

                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

private fun statusIconRes(
    status: QuestStatus,
    isPerfect: Boolean
): Int = when {
    status == QuestStatus.COMPLETED && isPerfect -> {
        R.drawable.icon_perfect_clear
    }

    status == QuestStatus.COMPLETED -> {
        R.drawable.icon_completed
    }

    status == QuestStatus.IN_PROGRESS -> {
        R.drawable.icon_in_progress
    }

    else -> {
        R.drawable.icon_lock
    }
}