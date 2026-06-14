package com.localreels.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003355),
    background = Color(0xFF1A1C1E),
    surface = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF185FA5),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFFCFCFF),
    surface = Color(0xFFFCFCFF),
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E),
)

@Composable
fun LocalReelsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,   // video apps look better dark by default
        content = content
    )
}
