package com.cobling.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cobling.app.R

// Pretendard, GmarketSans, LeeSeoyun 폰트
// 실제 앱에서는 assets/fonts/ 에 폰트 파일 추가 필요
// Swift의 Font extension → Android FontFamily

val PretendardFamily = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_bold, FontWeight.Bold)
) // 실제 폰트로 교체
val GmarketFamily = FontFamily(
    Font(R.font.gmarketsansmedium, FontWeight.Medium),
    Font(R.font.gmarketsansbold, FontWeight.Bold)
)
val LeeSeoyunFamily = FontFamily(
    Font(R.font.leeseoyun, FontWeight.Normal)
)

val CoblingTypography = Typography(
    bodyLarge = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    titleLarge = TextStyle(fontFamily = GmarketFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = GmarketFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    titleSmall = TextStyle(fontFamily = GmarketFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    displayLarge = TextStyle(fontFamily = LeeSeoyunFamily, fontWeight = FontWeight.Normal, fontSize = 48.sp),
    displayMedium = TextStyle(fontFamily = LeeSeoyunFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp),
    displaySmall = TextStyle(fontFamily = LeeSeoyunFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    headlineLarge = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.Bold, fontSize = 34.sp),
    headlineMedium = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
)
