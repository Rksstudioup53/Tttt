package com.hindivideo.translator.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF97316),       // Vibrant Orange
    secondary = Color(0xFF38BDF8),     // Sky Blue
    background = Color(0xFF0F172A),    // Slate 900
    surface = Color(0xFF1E293B),       // Slate 800
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun HindiVideoTranslatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}