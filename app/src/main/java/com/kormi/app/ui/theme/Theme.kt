package com.kormi.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Navy = Color(0xFF0A1D3F)
val NavyLight = Color(0xFF1B3763)
val Gold = Color(0xFFC9A961)
val OffWhite = Color(0xFFF7F8FA)
val TextPrimary = Color(0xFF0A1D3F)
val TextSecondary = Color(0xFF5A6478)

// Force Light Theme for premium look
private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    secondary = Gold,
    onSecondary = Navy,
    background = OffWhite,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary
)

private val KormiTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.5.sp)
)

@Composable
fun KormiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = KormiTypography,
        content = content
    )
}
