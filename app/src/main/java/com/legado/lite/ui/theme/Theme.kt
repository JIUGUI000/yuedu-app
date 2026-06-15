package com.legado.lite.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Light = lightColorScheme(
    primary = Color(0xFF5B6CFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDEE0FF),
    onPrimaryContainer = Color(0xFF0A1B6B),
    secondary = Color(0xFF5E5C71),
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF),
    surfaceVariant = Color(0xFFEEEDF3),
    onBackground = Color(0xFF1B1B1F),
    onSurface = Color(0xFF1B1B1F),
    onSurfaceVariant = Color(0xFF46464F),
    outline = Color(0xFF777680)
)

private val Dark = darkColorScheme(
    primary = Color(0xFFBAC4FF),
    onPrimary = Color(0xFF0A1B6B),
    primaryContainer = Color(0xFF253489),
    onPrimaryContainer = Color(0xFFDEE0FF),
    secondary = Color(0xFFC7C4DC),
    background = Color(0xFF131316),
    surface = Color(0xFF131316),
    surfaceVariant = Color(0xFF222227),
    onBackground = Color(0xFFE5E1E6),
    onSurface = Color(0xFFE5E1E6),
    onSurfaceVariant = Color(0xFFC8C5D0),
    outline = Color(0xFF918F9A)
)

private val Typo = Typography(
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 12.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun LegadoTheme(content: @Composable () -> Unit) {
    val scheme = if (isSystemInDarkTheme()) Dark else Light
    MaterialTheme(colorScheme = scheme, typography = Typo, content = content)
}
