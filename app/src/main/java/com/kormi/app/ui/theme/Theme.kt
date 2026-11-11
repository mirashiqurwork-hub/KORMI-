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

@Composable
fun KormiTheme(
    fontScale: Float = 1.0f, // 🌟 ফন্ট সাইজ কন্ট্রোল
    content: @Composable () -> Unit
) {
    // ফন্ট স্কেল অনুযায়ী সব ফন্ট সাইজ অটোমেটিক পরিবর্তন হবে
    val KormiTypography = Typography(
        headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = (28 * fontScale).sp, letterSpacing = (-0.5).sp),
        headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = (22 * fontScale).sp),
        titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = (18 * fontScale).sp),
        titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = (15 * fontScale).sp),
        bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = (14 * fontScale).sp),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = (13 * fontScale).sp),
        labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = (11 * fontScale).sp, letterSpacing = 0.5.sp)
    )

    MaterialTheme(
        colorScheme = LightColors,
        typography = KormiTypography,
        content = content
    )
}
