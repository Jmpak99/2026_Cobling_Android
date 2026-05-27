package com.cobling.app.ui.experience

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.cobling.app.R
import com.cobling.app.model.BlockType
import com.cobling.app.navigation.Screen
import com.cobling.app.ui.quest.block.blockImageRes
import com.cobling.app.viewmodel.AppState
import com.cobling.app.viewmodel.Direction
import com.cobling.app.viewmodel.ExperienceQuestViewModel

private const val TILE = 36

@Composable
fun ExperienceQuestScreen(
    appState: AppState,
    navController: NavHostController,
    vm: ExperienceQuestViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { appState.isInGame = true }
    DisposableEffect(Unit) { onDispose { vm.stopExecution(); appState.isInGame = false } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF2DC))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(42.dp))

            // 헤더
            Text(
                text = vm.stage.title,
                fontSize = 34.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFF3A3A3A),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // 컨트롤 버튼
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton(onClick = { vm.startExecution() }) {
                        Icon(Icons.Filled.PlayArrow, "실행", tint = Color(0xFF58ED98), modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = { vm.stopExecution() }) {
                        Icon(Icons.Filled.Stop, "정지", tint = Color(0xFFE85A5A), modifier = Modifier.size(28.dp))
                    }
                }
                IconButton(onClick = { appState.isInGame = false; navController.popBackStack() }) {
                    Icon(painterResource(R.drawable.gp_out), "나가기", modifier = Modifier.size(32.dp), tint = Color.Unspecified)
                }
            }

            // 설명
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(vm.stage.description, fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text("앞으로 가기 블록 2개를 연결해 깃발까지 이동해보세요.", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                ExperienceMapGrid(vm = vm)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 블록 섹션
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 팔레트
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("블록", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE9E8DD))
                            .clickable { vm.appendForwardBlock() }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("앞으로 가기 추가", fontSize = 16.sp)
                    }
                }

                // 캔버스
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("조립한 블록", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("${vm.startBlock.children.size}개", fontSize = 14.sp, color = Color.Gray)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 시작 블록
                        Icon(
                            painter = painterResource(blockImageRes(BlockType.START)),
                            contentDescription = "시작",
                            modifier = Modifier.size(160.dp, 50.dp),
                            tint = Color.Unspecified
                        )
                        if (vm.startBlock.children.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF5F5F5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("블록을 추가해주세요", color = Color.Gray)
                            }
                        } else {
                            vm.startBlock.children.forEachIndexed { idx, block ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(blockImageRes(block.type)),
                                        contentDescription = block.type.name,
                                        modifier = Modifier.size(120.dp, 30.dp),
                                        tint = Color.Unspecified
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    TextButton(onClick = { vm.removeBlock(idx) }) {
                                        Text("✕", color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    // 실행 / 초기화 버튼
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { vm.startExecution() },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF58ED98))
                        ) { Text("실행하기", fontWeight = FontWeight.Bold, color = Color.Black) }
                        OutlinedButton(
                            onClick = { vm.resetForNewExperience() },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("초기화") }
                    }
                }
            }
        }

        // 성공 Alert
        if (vm.showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("체험 성공!") },
                text = { Text("코블링 체험을 완료했어요.\n계속하려면 회원가입이 필요해요.") },
                confirmButton = {
                    TextButton(onClick = {
                        vm.resetForNewExperience()
                        navController.navigate(Screen.Signup.route)
                    }) { Text("회원가입하기") }
                },
                dismissButton = {
                    TextButton(onClick = { vm.resetForNewExperience() }) { Text("다시하기") }
                }
            )
        }

        // 실패 Alert
        if (vm.showFailureDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("실패했어요") },
                text = { Text("길을 벗어났어요. 블록을 다시 확인해보세요.") },
                confirmButton = {
                    TextButton(onClick = { vm.resetExecution() }) { Text("다시하기") }
                }
            )
        }
    }
}

@Composable
private fun ExperienceMapGrid(vm: ExperienceQuestViewModel) {
    val tileDp = TILE.dp
    Box {
        Column {
            vm.mapData.forEachIndexed { row, rowData ->
                Row {
                    rowData.forEachIndexed { col, cell ->
                        Box(modifier = Modifier.size(tileDp)) {
                            if (cell == 1 || cell == 2) {
                                Icon(painterResource(R.drawable.iv_game_way_1), null,
                                    modifier = Modifier.fillMaxSize(), tint = Color.Unspecified)
                            }
                            if (vm.goalPosition.row == row && vm.goalPosition.col == col) {
                                Icon(painterResource(R.drawable.gp_flag), null,
                                    modifier = Modifier.size(36.dp).offset(y = (-15).dp).align(Alignment.Center),
                                    tint = Color.Unspecified)
                            }
                        }
                    }
                }
            }
        }
        // 캐릭터
        val cx = vm.characterPosition.col * TILE + TILE / 2
        val cy = vm.characterPosition.row * TILE + TILE / 2
        Icon(
            painterResource(R.drawable.cobling_stage_egg_front), null,
            modifier = Modifier.size((TILE * 1.4).dp).offset(x = (cx - TILE * 0.7).dp, y = (cy - TILE * 0.7 - 15).dp),
            tint = Color.Unspecified
        )
    }
}
