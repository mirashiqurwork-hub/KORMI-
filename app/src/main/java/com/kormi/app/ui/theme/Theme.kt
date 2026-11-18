package com.kormi.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kormi.app.data.prefs.FontScaleOption

// ===== KORMI - BLACK & YELLOW THEME =====
object KormiColors {
    val PureBlack = Color(0xFF000000)
    val SoftBlack = Color(0xFF0A0A0A)
    val CardBlack = Color(0xFF141414)
    val CardBlackLight = Color(0xFF1E1E1E)
    val BorderGrey = Color(0xFF2A2A2A)

    val MainYellow = Color(0xFFFFD400)
    val DarkYellow = Color(0xFFFFC107)
    val LightYellow = Color(0xFFFFE566)
    val YellowGlow = Color(0xFFFFF59D)

    val CleanWhite = Color(0xFFFFFFFF)
    val OffWhite = Color(0xFFF2F4F7)
    val MutedGrey = Color(0xFF9E9E9E)
    val SlateInk = Color(0xFF1E293B)

    val ActiveGreen = Color(0xFF22C55E)
    val WarningAmber = Color(0xFFFF9800)
    val AlertRed = Color(0xFFFF5252)
}

val LocalKormiFontScale = staticCompositionLocalOf { FontScaleOption.NORMAL }

// Light scheme ও Black base-এ রাখা, যাতে সাদা বক্স না আসে
private val KormiLightColorScheme = lightColorScheme(
    primary = KormiColors.PureBlack,
    onPrimary = KormiColors.MainYellow,
    primaryContainer = KormiColors.SoftBlack,
    onPrimaryContainer = KormiColors.MainYellow,
    secondary = KormiColors.MainYellow,
    onSecondary = KormiColors.PureBlack,
    secondaryContainer = KormiColors.DarkYellow,
    onSecondaryContainer = KormiColors.PureBlack,
    background = KormiColors.PureBlack,
    onBackground = KormiColors.CleanWhite,
    surface = KormiColors.CardBlack,
    onSurface = KormiColors.CleanWhite,
    surfaceVariant = KormiColors.CardBlackLight,
    onSurfaceVariant = KormiColors.MutedGrey,
    error = KormiColors.AlertRed,
    onError = KormiColors.CleanWhite
)

private val KormiDarkColorScheme = darkColorScheme(
    primary = KormiColors.MainYellow,
    onPrimary = KormiColors.PureBlack,
    primaryContainer = KormiColors.SoftBlack,
    onPrimaryContainer = KormiColors.MainYellow,
    secondary = KormiColors.LightYellow,
    onSecondary = KormiColors.PureBlack,
    background = KormiColors.PureBlack,
    onBackground = KormiColors.CleanWhite,
    surface = KormiColors.CardBlack,
    onSurface = KormiColors.CleanWhite,
    surfaceVariant = KormiColors.CardBlackLight,
    onSurfaceVariant = KormiColors.MutedGrey,
    error = KormiColors.AlertRed,
    onError = KormiColors.PureBlack
)

val KormiShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

fun buildKormiTypography(fontScale: FontScaleOption): Typography {
    val s = fontScale.scaleFactor
    return Typography(
        displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = (32 * s).sp, lineHeight = (38 * s).sp),
        headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = (24 * s).sp, lineHeight = (30 * s).sp),
        headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = (20 * s).sp, lineHeight = (26 * s).sp),
        titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = (16 * s).sp, lineHeight = (22 * s).sp),
        titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = (14 * s).sp, lineHeight = (18 * s).sp),
        bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = (15 * s).sp, lineHeight = (22 * s).sp),
        bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = (14 * s).sp, lineHeight = (20 * s).sp),
        labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = (14 * s).sp, lineHeight = (20 * s).sp)
    )
}

@Composable
fun KormiTheme(
    fontScale: FontScaleOption = FontScaleOption.NORMAL,
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) KormiDarkColorScheme else KormiLightColorScheme
    CompositionLocalProvider(LocalKormiFontScale provides fontScale) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = buildKormiTypography(fontScale),
            shapes = KormiShapes,
            content = content
        )
    }
}

// ===== Glass Black Card - নতুন UI এর মত =====
@Composable
fun KormiGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1E1E),
                        Color(0xFF0A0A0A)
                    )
                )
            )
            .border(1.dp, Color(0xFFFFD400).copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
        content = content
    )
}

// ===== Yellow Gradient Button Background =====
fun kormiYellowGradient(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(KormiColors.MainYellow, KormiColors.DarkYellow)
    )
}

// ===== Factory Silhouette Background - Login এর জন্য =====
@Composable
fun KormiBlackBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KormiColors.PureBlack),
        content = content
    )
}
