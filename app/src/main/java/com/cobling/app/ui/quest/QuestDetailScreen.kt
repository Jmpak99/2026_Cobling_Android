package com.cobling.app.ui.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.R
import com.cobling.app.model.*
import com.cobling.app.ui.theme.*
import com.cobling.app.util.LocalStorageManager
import com.cobling.app.util.QuestTheme
import com.cobling.app.viewmodel.AuthViewModel
import com.cobling.app.viewmodel.TabBarViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun QuestDetailScreen(
    chapterId: String,
    tabBarViewModel: TabBarViewModel,
    authViewModel: AuthViewModel,
    onNavigateToGame: (subQuestId: String) -> Unit,
    onBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    var subQuests by remember { mutableStateOf<List<SubQuest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var chapterTitle by remember { mutableStateOf("") }
    var showLockedAlert by remember { mutableStateOf(false) }
    var showCutscene by remember { mutableStateOf(false) }
    var pendingSubQuestId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(chapterId) {
        tabBarViewModel.isTabBarVisible = true

        try {
            val doc = db.collection("quests")
                .document(chapterId)
                .get()
                .await()

            chapterTitle = doc.getString("title") ?: ""
        } catch (_: Exception) {
        }

        loadSubQuests(
            db = db,
            chapterId = chapterId,
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            onLoaded = {
                subQuests = it
                isLoading = false
            },
            onError = {
                errorMessage = it
                isLoading = false
            }
        )
    }

    // 컷씬
    if (showCutscene) {
        val cutscene = ChapterCutsceneProvider.introCutscene(chapterId)

        com.cobling.app.ui.quest.cutscene.ChapterCutsceneScreen(
            chapterId = chapterId,
            cutscene = cutscene,
            authViewModel = authViewModel,
            onClose = {
                LocalStorageManager.setCutsceneShown(
                    chapterId,
                    ChapterCutsceneType.INTRO
                )

                val target = pendingSubQuestId
                pendingSubQuestId = null
                showCutscene = false

                if (target != null) {
                    tabBarViewModel.isTabBarVisible = false
                    onNavigateToGame(target)
                }
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ─────────────────────────────────
        // 상단 영역: 뒤로가기 버튼 → 타이틀
        // ─────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2F2F2))
                    .clickable {
                        tabBarViewModel.isTabBarVisible = true
                        onBack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "‹",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = chapterTitle,
                fontSize = 34.sp,
                fontFamily = GmarketFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "코블링의 퀘스트",
                fontFamily = PretendardFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "코블링과 함께 문제를 해결해 보세요!",
                fontFamily = PretendardFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Text(
                    text = "에러: $errorMessage",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> {
                val chapterNum = chapterId
                    .filter { it.isDigit() }
                    .toIntOrNull() ?: 1

                val bgColor = QuestTheme.backgroundColor(chapterNum)

                LazyColumn(
                    contentPadding = PaddingValues(
                        bottom = 80.dp,
                        start = 24.dp,
                        end = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(subQuests) { subQuest ->
                        SubQuestCard(
                            subQuest = subQuest,
                            backgroundColor = bgColor,
                            onTap = {
                                when {
                                    subQuest.state == SubQuestState.LOCKED -> {
                                        showLockedAlert = true
                                    }

                                    subQuest.order == 1 &&
                                            !LocalStorageManager.isCutsceneShown(
                                                chapterId,
                                                ChapterCutsceneType.INTRO
                                            ) -> {
                                        pendingSubQuestId = subQuest.id
                                        showCutscene = true
                                    }

                                    else -> {
                                        tabBarViewModel.isTabBarVisible = false
                                        onNavigateToGame(subQuest.id)
                                    }
                                }
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
}

@Composable
private fun SubQuestCard(
    subQuest: SubQuest,
    backgroundColor: Color,
    onTap: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable {
                onTap()
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(Modifier.height(80.dp))

            // ─────────────────────────────────
            // 하단 흰색 박스
            // 좌우 padding 제거 → 바깥 카드 좌우와 딱 맞춤
            // ─────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
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
                            text = subQuest.title,
                            fontFamily = GmarketFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )

                        Text(
                            text = subQuest.description,
                            fontFamily = PretendardFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Icon(
                        painter = painterResource(
                            subQuestIconRes(
                                state = subQuest.state,
                                perfect = subQuest.perfectClear
                            )
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(70.dp, 30.dp),
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

private fun subQuestIconRes(
    state: SubQuestState,
    perfect: Boolean
): Int = when {
    state == SubQuestState.COMPLETED && perfect -> {
        R.drawable.icon_perfect_clear
    }

    state == SubQuestState.COMPLETED -> {
        R.drawable.icon_completed
    }

    state == SubQuestState.IN_PROGRESS -> {
        R.drawable.icon_in_progress
    }

    else -> {
        R.drawable.icon_lock
    }
}

private suspend fun loadSubQuests(
    db: FirebaseFirestore,
    chapterId: String,
    userId: String,
    onLoaded: (List<SubQuest>) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val baseSnap = db.collection("quests")
            .document(chapterId)
            .collection("subQuests")
            .orderBy("order")
            .get()
            .await()

        val base = baseSnap.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null

            SubQuest(
                id = doc.id,
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                state = SubQuestState.LOCKED,
                perfectClear = false,
                order = (data["order"] as? Long)?.toInt() ?: 0
            )
        }

        val progressSnap = db.collection("users")
            .document(userId)
            .collection("progress")
            .document(chapterId)
            .collection("subQuests")
            .get()
            .await()

        val stateMap = progressSnap.documents.associate { doc ->
            doc.id to ((doc.data?.get("state") as? String) ?: "locked").trim()
        }

        val perfectMap = progressSnap.documents.associate { doc ->
            doc.id to (doc.data?.get("perfectClear") as? Boolean ?: false)
        }

        val merged = base.map { sq ->
            val state = when (stateMap[sq.id]) {
                "completed" -> SubQuestState.COMPLETED
                "inProgress" -> SubQuestState.IN_PROGRESS
                else -> SubQuestState.LOCKED
            }

            sq.copy(
                state = state,
                perfectClear = perfectMap[sq.id] ?: false
            )
        }

        onLoaded(merged)
    } catch (e: Exception) {
        onError(e.message ?: "알 수 없는 오류")
    }
}