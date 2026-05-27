package com.cobling.app.ui.theme

import androidx.compose.ui.graphics.Color

// Cobling 앱 전용 색상 (Swift Color+Hex 대응)
val CoblingBackground = Color(0xFFFFF7E9)
val CoblingCard = Color(0xFFF8F8F6)
val CoblingButtonBg = Color(0xFFE9E8DD)
val CoblingPrimary = Color(0xFFFFB84D)
val CoblingExpBar = Color(0xFFEA4C89)
val CoblingGreen = Color(0xFF58ED98)
val CoblingRed = Color(0xFFE85A5A)
val CoblingDark = Color(0xFF3A3A3A)
val CoblingText = Color(0xFF2B3A1E)
val CoblingGold = Color(0xFFFFD475)
val CoblingGreenAccent = Color(0xFF6B8F5D)
val CoblingRepeatBlue = Color(0xFF86B0FF)
val CoblingIfGreen = Color(0xFF4CCB7A)

fun colorFromHex(hex: String): Color {
    val clean = hex.trimStart('#')
    val long = clean.toLong(16)
    return when (clean.length) {
        6 -> Color(
            red = ((long shr 16) and 0xFF) / 255f,
            green = ((long shr 8) and 0xFF) / 255f,
            blue = (long and 0xFF) / 255f
        )
        8 -> Color(
            alpha = ((long shr 24) and 0xFF) / 255f,
            red = ((long shr 16) and 0xFF) / 255f,
            green = ((long shr 8) and 0xFF) / 255f,
            blue = (long and 0xFF) / 255f
        )
        else -> Color.Black
    }
}
