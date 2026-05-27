package com.cobling.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.R
import com.cobling.app.ui.theme.GmarketFamily
import com.cobling.app.ui.theme.LeeSeoyunFamily
import com.cobling.app.ui.theme.colorFromHex
import kotlinx.coroutines.delay

// ──────────────────────────────────────
// SplashScreen
// SwiftUI SplashView → Compose
// ──────────────────────────────────────
@Composable
fun SplashScreen(onSplashDone: () -> Unit) {
    val bgColor = colorFromHex("#FFF7E9")

    LaunchedEffect(Unit) {
        delay(2000)
        onSplashDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // cobling_stage_super 이미지 (실제 drawable에 추가 필요)
            Box(
                modifier = Modifier.size(178.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cobling_stage_super),
                    contentDescription = "코블링 캐릭터",
                    modifier = Modifier.size(178.dp)
                )
            }

            Text(
                text = "코블링",
                fontFamily = LeeSeoyunFamily,
                fontSize = 48.sp,
                color = colorFromHex("#3A3A3A")
            )

            Text(
                text = "모바일 블록코딩앱",
                fontFamily = GmarketFamily,
                fontSize = 18.sp,
                color = colorFromHex("#3A3A3A")
            )
        }
    }
}
