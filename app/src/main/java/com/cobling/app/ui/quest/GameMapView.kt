package com.cobling.app.ui.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cobling.app.R
import com.cobling.app.ui.shared.SpeechBubbleView
import com.cobling.app.ui.theme.GmarketFamily
import com.cobling.app.viewmodel.AuthViewModel
import com.cobling.app.viewmodel.Direction
import com.cobling.app.viewmodel.QuestViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val TILE_DP = 36

@Composable
fun GameMapView(
    viewModel: QuestViewModel,
    questTitle: String,
    subQuestId: String,
    authViewModel: AuthViewModel,
    onStoryButtonPositioned: (Rect) -> Unit = {},
    onPlayButtonPositioned: (Rect) -> Unit = {},
    onStopButtonPositioned: (Rect) -> Unit = {},
    onFlagPositioned: (Rect) -> Unit = {},
    onExit: () -> Unit
) {
    var isStoryOn by remember { mutableStateOf(false) }

    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()

    var liveCharacterStage by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(subQuestId) {
        isStoryOn = false
    }

    // ─────────────────────────────────────
    // 캐릭터 stage 실시간 감시
    // Firestore users/{uid}/character.stage 변경 시 즉시 반영
    // ─────────────────────────────────────
    DisposableEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            onDispose { }
        } else {
            val listener = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .addSnapshotListener { snapshot, _ ->
                    val stageFromDotPath = snapshot?.getString("character.stage")

                    val stageFromMap = (snapshot?.get("character") as? Map<*, *>)
                        ?.get("stage") as? String

                    liveCharacterStage = stageFromDotPath ?: stageFromMap
                }

            onDispose {
                listener.remove()
            }
        }
    }

    val profileStage = userProfile
        ?.character
        ?.stage
        ?.trim()
        ?.lowercase()

    val firestoreStage = liveCharacterStage
        ?.trim()
        ?.lowercase()

    val stage = firestoreStage
        ?: profileStage
        ?: "egg"

    val safeStage = if (stage in setOf("egg", "kid", "cobling", "legend")) {
        stage
    } else {
        "egg"
    }

    val tileDp = TILE_DP.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(470.dp)
            .background(Color(0xFFFFF2DC))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(42.dp))

            Text(
                text = questTitle,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = GmarketFamily,
                color = Color(0xFF3A3A3A),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-6).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.startExecution()
                        },
                        modifier = Modifier
                            .size(58.dp)
                            .onGloballyPositioned { coords ->
                                val p = coords.positionInRoot()
                                val s = coords.size

                                onPlayButtonPositioned(
                                    Rect(
                                        p.x,
                                        p.y,
                                        p.x + s.width,
                                        p.y + s.height
                                    )
                                )
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "실행",
                            tint = Color(0xFF58ED98),
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.stopExecution()
                        },
                        modifier = Modifier
                            .size(58.dp)
                            .onGloballyPositioned { coords ->
                                val p = coords.positionInRoot()
                                val s = coords.size

                                onStopButtonPositioned(
                                    Rect(
                                        p.x,
                                        p.y,
                                        p.x + s.width,
                                        p.y + s.height
                                    )
                                )
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stop,
                            contentDescription = "정지",
                            tint = Color(0xFFE85A5A),
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onExit,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.gp_out),
                        contentDescription = "나가기",
                        modifier = Modifier.size(34.dp),
                        tint = Color.Unspecified
                    )
                }
            }

            val mapWidth = (viewModel.mapData.firstOrNull()?.size ?: 0) * TILE_DP
            val mapHeight = viewModel.mapData.size * TILE_DP

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .width(mapWidth.dp)
                        .height(mapHeight.dp)
                ) {
                    Column {
                        viewModel.mapData.forEachIndexed { row, rowData ->
                            Row {
                                rowData.forEachIndexed { col, cell ->
                                    Box(
                                        modifier = Modifier.size(tileDp)
                                    ) {
                                        if (cell == 1 || cell == 2) {
                                            Icon(
                                                painter = painterResource(R.drawable.iv_game_way_1),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                tint = Color.Unspecified
                                            )
                                        }

                                        if (
                                            viewModel.enemies.any {
                                                it.row == row && it.col == col
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(
                                                    R.drawable.cobling_character_enemies
                                                ),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(tileDp * 1.4f)
                                                    .offset(y = (-8).dp)
                                                    .align(Alignment.Center),
                                                tint = Color.Unspecified
                                            )
                                        }

                                        val isGoal =
                                            viewModel.goalPosition.row == row &&
                                                    viewModel.goalPosition.col == col

                                        if (isGoal) {
                                            Icon(
                                                painter = painterResource(R.drawable.gp_flag),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .offset(y = (-15).dp)
                                                    .align(Alignment.Center)
                                                    .onGloballyPositioned { coords ->
                                                        val p = coords.positionInRoot()
                                                        val s = coords.size

                                                        onFlagPositioned(
                                                            Rect(
                                                                p.x,
                                                                p.y,
                                                                p.x + s.width,
                                                                p.y + s.height
                                                            )
                                                        )
                                                    },
                                                tint = Color.Unspecified
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val charX =
                        viewModel.characterPosition.col * TILE_DP + TILE_DP / 2

                    val charY =
                        viewModel.characterPosition.row * TILE_DP + TILE_DP / 2

                    Icon(
                        painter = painterResource(
                            id = charDrawableId(
                                stage = safeStage,
                                direction = viewModel.characterDirection
                            )
                        ),
                        contentDescription = "캐릭터",
                        modifier = Modifier
                            .size((TILE_DP * 1.4f).dp)
                            .offset(
                                x = (charX - TILE_DP * 0.7f).dp,
                                y = (charY - TILE_DP * 0.7f - 15).dp
                            ),
                        tint = Color.Unspecified
                    )
                }
            }
        }

        // ─────────────────────────────────────
        // 스토리 버튼 + 말풍선 오버레이
        // Column 안에 넣으면 팔레트/캔버스가 밀리므로 Box 위에 띄움
        // ─────────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            if (isStoryOn) {
                SpeechBubbleView(
                    message = viewModel.hintMessage
                        ?: "힌트가 아직 없어요."
                )

                Spacer(modifier = Modifier.width(8.dp))
            }

            IconButton(
                onClick = {
                    isStoryOn = !isStoryOn
                },
                modifier = Modifier
                    .onGloballyPositioned { coords ->
                        val p = coords.positionInRoot()
                        val s = coords.size

                        onStoryButtonPositioned(
                            Rect(
                                p.x,
                                p.y,
                                p.x + s.width,
                                p.y + s.height
                            )
                        )
                    }
            ) {
                Icon(
                    painter = painterResource(
                        if (isStoryOn) {
                            R.drawable.gp_story_btn_on
                        } else {
                            R.drawable.gp_stroy_btn_off
                        }
                    ),
                    contentDescription = "스토리",
                    modifier = Modifier.size(40.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

private fun charDrawableId(
    stage: String,
    direction: Direction
): Int {
    return when (stage) {
        "egg" -> when (direction) {
            Direction.UP -> R.drawable.cobling_stage_egg_back
            Direction.DOWN -> R.drawable.cobling_stage_egg_front
            Direction.LEFT -> R.drawable.cobling_stage_egg_left
            Direction.RIGHT -> R.drawable.cobling_stage_egg_right
        }

        "kid" -> when (direction) {
            Direction.UP -> R.drawable.cobling_stage_kid_back
            Direction.DOWN -> R.drawable.cobling_stage_kid_front
            Direction.LEFT -> R.drawable.cobling_stage_kid_left
            Direction.RIGHT -> R.drawable.cobling_stage_kid_right
        }

        "cobling" -> when (direction) {
            Direction.UP -> R.drawable.cobling_stage_cobling_back
            Direction.DOWN -> R.drawable.cobling_stage_cobling_front
            Direction.LEFT -> R.drawable.cobling_stage_cobling_left
            Direction.RIGHT -> R.drawable.cobling_stage_cobling_right
        }

        "legend" -> when (direction) {
            Direction.UP -> R.drawable.cobling_stage_legend_back
            Direction.DOWN -> R.drawable.cobling_stage_legend_front
            Direction.LEFT -> R.drawable.cobling_stage_legend_left
            Direction.RIGHT -> R.drawable.cobling_stage_legend_right
        }

        else -> when (direction) {
            Direction.UP -> R.drawable.cobling_stage_egg_back
            Direction.DOWN -> R.drawable.cobling_stage_egg_front
            Direction.LEFT -> R.drawable.cobling_stage_egg_left
            Direction.RIGHT -> R.drawable.cobling_stage_egg_right
        }
    }
}