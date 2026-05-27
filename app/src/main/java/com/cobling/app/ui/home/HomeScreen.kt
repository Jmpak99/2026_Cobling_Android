package com.cobling.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cobling.app.ui.theme.colorFromHex
import com.cobling.app.viewmodel.AppState
import com.cobling.app.viewmodel.AuthViewModel
import com.cobling.app.viewmodel.HomeViewModel
import com.cobling.app.viewmodel.TabItem
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.cobling.app.R
import com.cobling.app.ui.theme.*

// ──────────────────────────────────────
// HomeScreen
// SwiftUI HomeView → Compose
// ──────────────────────────────────────
@Composable
fun HomeScreen(
    appState: AppState,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val userProfile by authViewModel.userProfile.collectAsState()
    val level by homeViewModel.level.collectAsState()
    val expPercent by homeViewModel.expPercent.collectAsState()
    val dailyCount by homeViewModel.dailyCount.collectAsState()
    val dailyIsCompleted by homeViewModel.dailyIsCompleted.collectAsState()
    val monthlyCount by homeViewModel.monthlyCount.collectAsState()
    val monthlyIsCompleted by homeViewModel.monthlyIsCompleted.collectAsState()
    val dailyIsEnabled by homeViewModel.dailyIsEnabled.collectAsState()
    val dailyTitle by homeViewModel.dailyTitle.collectAsState()
    val dailySubtitle by homeViewModel.dailySubtitle.collectAsState()
    val dailyTargetCount by homeViewModel.dailyTargetCount.collectAsState()
    val monthlyIsEnabled by homeViewModel.monthlyIsEnabled.collectAsState()
    val monthlyTitle by homeViewModel.monthlyTitle.collectAsState()
    val monthlySubtitle by homeViewModel.monthlySubtitle.collectAsState()
    val monthlyTargetCount by homeViewModel.monthlyTargetCount.collectAsState()

    LaunchedEffect(Unit) { homeViewModel.startListeningUserData() }
    DisposableEffect(Unit) { onDispose { homeViewModel.stopListeningUserData() } }

    val greetingText = userProfile?.nickname?.takeIf { it.isNotEmpty() }
        ?.let { "반가워요 ${it}님" } ?: "반가워요"

    val stage = (userProfile?.character?.stage ?: "egg").trim().lowercase()
    val safeStage = if (stage in setOf("egg", "kid", "cobling", "legend")) stage else "egg"
    val characterRes = when (safeStage) {
        "egg" -> R.drawable.cobling_stage_egg
        "kid" -> R.drawable.cobling_stage_kid
        "cobling" -> R.drawable.cobling_stage_cobling
        "legend" -> R.drawable.cobling_stage_legend
        else -> R.drawable.cobling_stage_egg
    }

    val displayDailyCount = minOf(dailyCount, maxOf(dailyTargetCount, 1))
    val displayMonthlyCount = minOf(monthlyCount, maxOf(monthlyTargetCount, 1))
    val dailyTarget = maxOf(dailyTargetCount, 1)
    val monthlyTarget = maxOf(monthlyTargetCount, 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // 상단 제목
        Text(
            text = "홈",
            fontFamily = PretendardFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            color = Color.Black,
            modifier = Modifier.padding(start = 24.dp, top = 36.dp, bottom = 8.dp)
        )

        // 인삿말
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp)
        ) {
            Text(
                text = "$greetingText\n저는 코블링이에요!",
                fontFamily = LeeSeoyunFamily,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        // 캐릭터 이미지 영역
        Image(
            painter = painterResource(id = characterRes),
            contentDescription = "코블링 캐릭터",
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            contentScale = ContentScale.Fit
        )

        // 키우러 가기 버튼
        Button(
            onClick = { appState.selectedTab = TabItem.QUEST },
            modifier = Modifier
                .padding(top = 40.dp, start = 80.dp, end = 80.dp)
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorFromHex("#E9E8DD")),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("키우러 가기", color = Color.Black, fontSize = 16.sp)
        }

        // 레벨 카드
        Box(
            modifier = Modifier
                .padding(start = 28.dp, end = 28.dp, top = 34.dp, bottom = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colorFromHex("#F8F8F6"))
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Column {
                Text("Lv. $level", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = colorFromHex("#1D260D"))
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(colorFromHex("#E9E8DD"))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(expPercent.toFloat().coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .background(colorFromHex("#EA4C89"))
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${(expPercent * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colorFromHex("#1D260D"))
                }
            }
        }
        // 일일 미션 카드
        if (dailyIsEnabled) {
            MissionCard(
                title = dailyTitle,
                subtitle = "$dailySubtitle ($displayDailyCount/$dailyTarget)",
                isCompleted = dailyIsCompleted,
                bgColor = if (dailyIsCompleted) colorFromHex("#FFECEC") else colorFromHex("#F8F8F6")
            )
        }

        // 월간 미션 카드
        if (monthlyIsEnabled) {
            MissionCard(
                title = monthlyTitle,
                subtitle = "$monthlySubtitle ($displayMonthlyCount/$monthlyTarget)",
                isCompleted = monthlyIsCompleted,
                bgColor = if (monthlyIsCompleted) colorFromHex("#FFECEC") else colorFromHex("#F8F8F6")
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun MissionCard(title: String, subtitle: String, isCompleted: Boolean, bgColor: Color) {
    Box(
        modifier = Modifier
            .padding(horizontal = 28.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, fontSize = 14.sp, color = Color.Gray)
            }
            if (isCompleted) {
                Text("✓", fontSize = 24.sp, color = colorFromHex("#EA4C89"))
            }
        }
    }
}

// ──────────────────────────────────────
// GuestHomeScreen
// SwiftUI GuestHomeView → Compose
// ──────────────────────────────────────
@Composable
fun GuestHomeScreen(
    appState: AppState,
    onNavigateToSignup: () -> Unit
) {
    var showLoginAlert by remember { mutableStateOf(false) }

    if (showLoginAlert) {
        AlertDialog(
            onDismissRequest = { showLoginAlert = false },
            title = { Text("로그인 후 이용가능합니다") },
            text = { Text("로그인 하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = { showLoginAlert = false; onNavigateToSignup() }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showLoginAlert = false }) { Text("취소") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        Text("홈", fontWeight = FontWeight.Bold, fontSize = 34.sp, modifier = Modifier.padding(start = 24.dp, top = 20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp)
        ) {
            Text(
                text = "반가워요\n저는 코블링이에요!",
                fontFamily = LeeSeoyunFamily,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        Image(
            painter = painterResource(id = R.drawable.cobling_stage_legend),
            contentDescription = "코블링 캐릭터",
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            contentScale = ContentScale.Fit
        )

        Button(
            onClick = { showLoginAlert = true },
            modifier = Modifier
                .padding(top = 40.dp, start = 80.dp, end = 80.dp)
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorFromHex("#E9E8DD")),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("키우러 가기", color = Color.Black, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))



        // 소개 카드 1
        IntroCard(
            title = "블록을 조립해주세요",
            desc = "블록을 연결해 캐릭터를 움직이며\n코딩의 흐름을 쉽게 익힐 수 있어요."
        )

        // 소개 카드 2
        IntroCard(
            title = "게임처럼 즐기며 학습해요",
            desc = "미션을 해결하고 목표 지점에 도착하며\n자연스럽게 논리적 사고를 익혀요."
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun IntroCard(title: String, desc: String) {
    Box(
        modifier = Modifier
            .padding(horizontal = 28.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colorFromHex("#F8F8F6"))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                desc,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
