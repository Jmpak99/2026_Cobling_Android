package com.cobling.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.R
import com.cobling.app.ui.theme.GmarketFamily
import com.cobling.app.ui.theme.LeeSeoyunFamily
import com.cobling.app.ui.theme.colorFromHex
import com.cobling.app.viewmodel.AuthViewModel

// ──────────────────────────────────────
// SignupScreen
// SwiftUI SignupView → Compose
// ──────────────────────────────────────
@Composable
fun SignupScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onTapLogin: () -> Unit,
    onTapEmailSignup: () -> Unit
) {
    val isSignedIn by authViewModel.isSignedIn.collectAsState()

    LaunchedEffect(isSignedIn) {
        if (isSignedIn) onTapLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFromHex("#FFF2DC"))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(130.dp))

            // 코블링 이미지
            Image(
                painter = painterResource(id = R.drawable.cobling_stage_super),
                contentDescription = "코블링 캐릭터",
                modifier = Modifier.size(180.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "코블링",
                fontFamily = LeeSeoyunFamily,
                fontSize = 48.sp,
                color = Color.Black
            )

            // 코블링 글자와 설명 문구 사이 간격
            Spacer(modifier = Modifier.height(14.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "코블링과 함께하는 코딩 모험",
                    fontFamily = GmarketFamily,
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.65f)
                )
                Text(
                    text = "지금 시작해요 !",
                    fontFamily = GmarketFamily,
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.65f)
                )
            }

            Spacer(modifier = Modifier.height(140.dp))

            // 이메일로 시작하기 버튼
            Button(
                onClick = { onTapLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(58.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    text = "이메일로 시작하기",
                    color = Color.Black,
                    fontFamily = GmarketFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // 하단 저작권
        Text(
            text = "Copyright 2026. BlockBlock",
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.25f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
    }
}

// ──────────────────────────────────────
// LearnMoreScreen
// SwiftUI LearnMoreView → Compose
// ──────────────────────────────────────
@Composable
fun LearnMoreScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFromHex("#FFF2DC"))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = "코블링은 어떤 앱인가요?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                text = "코블링은 블록을 연결해 캐릭터를 움직이며\n퍼즐을 해결하는 블록코딩 학습 게임입니다.",
                fontSize = 16.sp,
                color = Color.Black.copy(alpha = 0.75f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            listOf(
                "게임 방법" to "블록을 순서대로 배치하면 코블링이 직접 움직여요.\n앞으로 가기, 회전, 반복문 같은 블록으로 길을 찾아가며 깃발에 도착하면 성공입니다.",
                "이런 재미가 있어요" to "퀘스트를 클리어하면서 점점 더 다양한 블록을 배우고,\n퍼즐을 해결하며 자연스럽게 코딩 사고력을 기를 수 있어요.",
                "로그인하면 가능한 기능" to "로그인 후에는 퀘스트 진행도 저장, 경험치와 레벨 반영,\n캐릭터 성장, 랭킹 시스템 등의 기능을 사용할 수 있어요."
            ).forEach { (title, desc) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = desc,
                            fontSize = 15.sp,
                            color = Color.Black.copy(alpha = 0.72f)
                        )
                    }
                }
            }
        }
    }
}