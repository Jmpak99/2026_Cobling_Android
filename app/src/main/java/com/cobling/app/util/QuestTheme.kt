package com.cobling.app.util

import androidx.compose.ui.graphics.Color

object QuestTheme {
    private val palette: List<Color> = listOf(
        Color(0xFFFFEEEF), // 1챕터 핑크
        Color(0xFFF3E8FF), // 2챕터 퍼플
        Color(0xFFE3EDFB), // 3챕터 블루
        Color(0xFFDFF6E8), // 4챕터 초록
        Color(0xFFFFF1DB), // 5챕터 베이지
        Color(0xFFFFF4E6)  // 6챕터 오렌지
    )

    fun backgroundColor(order: Int): Color {
        if (palette.isEmpty()) return Color.White
        val index = maxOf(order - 1, 0) % palette.size
        return palette[index]
    }
}
