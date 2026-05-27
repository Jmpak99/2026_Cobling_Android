package com.cobling.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CoblingColorScheme = lightColorScheme(
    primary = Color(0xFFFFB84D),
    secondary = Color(0xFF6B8F5D),
    background = Color(0xFFFFF7E9),
    surface = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color(0xFF3A3A3A),
    onSurface = Color(0xFF3A3A3A)
)

@Composable
fun CoblingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CoblingColorScheme,
        typography = CoblingTypography,
        content = content
    )
}
