package com.dyna.studenthub.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dyna.studenthub.domain.StudentHubThemeChoice

data class ThemePreviewColors(
    val primary: Color,
    val primaryDark: Color,
    val background: Color,
    val surface: Color,
    val secondaryBackground: Color,
    val accent: Color,
    val text: Color,
    val textSecondary: Color,
    val onPrimary: Color,
    val isDark: Boolean = false,
)

fun themePreviewColors(choice: StudentHubThemeChoice) = when (choice) {
    StudentHubThemeChoice.LAVENDER -> ThemePreviewColors(
        primary = Color(0xFFB9A7E8), primaryDark = Color(0xFF75649E), background = Color(0xFFFAF8FF),
        surface = Color.White, secondaryBackground = Color(0xFFF1ECFA), accent = Color(0xFFE9CDE6),
        text = Color(0xFF373044), textSecondary = Color(0xFF8A8297), onPrimary = Color(0xFF373044),
    )
    StudentHubThemeChoice.SKY -> ThemePreviewColors(
        primary = Color(0xFF91BDEB), primaryDark = Color(0xFF527DA8), background = Color(0xFFF6FAFF),
        surface = Color.White, secondaryBackground = Color(0xFFEAF4FD), accent = Color(0xFFB7D9F5),
        text = Color(0xFF263849), textSecondary = Color(0xFF7D91A5), onPrimary = Color(0xFF263849),
    )
    StudentHubThemeChoice.PINK -> ThemePreviewColors(
        primary = Color(0xFFE9AFC8), primaryDark = Color(0xFFB96E91), background = Color(0xFFFFF8FA),
        surface = Color.White, secondaryBackground = Color(0xFFFCECF2), accent = Color(0xFFF6D0C0),
        text = Color(0xFF44323B), textSecondary = Color(0xFF9A7F8A), onPrimary = Color(0xFF44323B),
    )
    StudentHubThemeChoice.MINT -> ThemePreviewColors(
        primary = Color(0xFF91CDB5), primaryDark = Color(0xFF4D9278), background = Color(0xFFF7FCF9),
        surface = Color.White, secondaryBackground = Color(0xFFEAF6EF), accent = Color(0xFFB7CDB9),
        text = Color(0xFF2F4038), textSecondary = Color(0xFF80958A), onPrimary = Color(0xFF2F4038),
    )
    StudentHubThemeChoice.CREAM -> ThemePreviewColors(
        primary = Color(0xFFD8C2A3), primaryDark = Color(0xFF96775D), background = Color(0xFFFBF8F1),
        surface = Color.White, secondaryBackground = Color(0xFFF1E8D9), accent = Color(0xFFC89A6B),
        text = Color(0xFF443A32), textSecondary = Color(0xFF8E8175), onPrimary = Color(0xFF443A32),
    )
    StudentHubThemeChoice.NIGHT -> ThemePreviewColors(
        primary = Color(0xFFC0B3FF), primaryDark = Color(0xFF9AD9BF), background = Color(0xFF151321),
        surface = Color(0xFF211E30), secondaryBackground = Color(0xFF39334F), accent = Color(0xFF39334F),
        text = Color(0xFFF3F0FA), textSecondary = Color(0xFFC9C4D5), onPrimary = Color(0xFF28233D), isDark = true,
    )
}

private val StudentHubTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        headlineLarge = headlineLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        headlineMedium = headlineMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(fontFamily = FontFamily.SansSerif),
        bodyMedium = bodyMedium.copy(fontFamily = FontFamily.SansSerif),
        labelLarge = labelLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
    )
}

private val StudentHubShapes = Shapes(
    small = RoundedCornerShape(14.dp), medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp),
)

private fun colorScheme(choice: StudentHubThemeChoice): ColorScheme {
    val p = themePreviewColors(choice)
    val base = if (p.isDark) darkColorScheme(
        primary = p.primary, onPrimary = p.onPrimary, secondary = p.primaryDark, onSecondary = Color(0xFF17251F),
        tertiary = Color(0xFFFFB7CC), onTertiary = Color(0xFF35212B), background = p.background,
        onBackground = p.text, surface = p.surface, onSurface = p.text,
        surfaceVariant = p.secondaryBackground, onSurfaceVariant = p.textSecondary,
        error = Color(0xFFFFB4AB), onError = Color(0xFF690005), errorContainer = Color(0xFF4D292B),
    ) else lightColorScheme(
        primary = p.primary, onPrimary = p.onPrimary, secondary = p.primaryDark, onSecondary = Color.White,
        tertiary = p.primaryDark, onTertiary = Color.White, background = p.background, onBackground = p.text,
        surface = p.surface, onSurface = p.text, surfaceVariant = p.secondaryBackground,
        onSurfaceVariant = p.textSecondary, error = Color(0xFFB3261E), onError = Color.White,
        errorContainer = Color(0xFFFDECEC), onErrorContainer = Color(0xFF5F1412),
    )
    val primaryContainer = when (choice) {
        StudentHubThemeChoice.LAVENDER -> Color(0xFFE7DDF8)
        StudentHubThemeChoice.SKY -> Color(0xFFDCEEFF)
        StudentHubThemeChoice.PINK -> Color(0xFFF8DFE9)
        StudentHubThemeChoice.MINT -> Color(0xFFDDF3E9)
        StudentHubThemeChoice.CREAM -> Color(0xFFF7F0E4)
        StudentHubThemeChoice.NIGHT -> Color(0xFF312B4B)
    }
    val secondaryContainer = if (p.isDark) Color(0xFF233B32) else p.secondaryBackground
    val tertiaryContainer = if (p.isDark) Color(0xFF482D3D) else p.accent
    return base.copy(
        primaryContainer = primaryContainer,
        onPrimaryContainer = if (p.isDark) Color(0xFFE8E2FF) else p.text,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = if (p.isDark) Color(0xFFD0F5E6) else p.text,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = if (p.isDark) Color(0xFFFFD9E6) else p.text,
        outline = p.textSecondary.copy(alpha = .55f),
        outlineVariant = if (p.isDark) Color(0xFF49445B) else p.secondaryBackground,
        surfaceContainerLowest = p.surface,
        surfaceContainerLow = p.surface,
        surfaceContainer = if (p.isDark) p.surface else p.secondaryBackground,
        surfaceContainerHigh = if (p.isDark) Color(0xFF302C40) else p.secondaryBackground,
        surfaceContainerHighest = if (p.isDark) Color(0xFF39334F) else p.secondaryBackground,
    )
}

@Composable
fun StudentHubTheme(choice: StudentHubThemeChoice = StudentHubThemeChoice.LAVENDER, content: @Composable () -> Unit) {
    val colors = colorScheme(choice)
    MaterialTheme(colorScheme = colors, typography = StudentHubTypography, shapes = StudentHubShapes) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant.copy(alpha = if (choice == StudentHubThemeChoice.NIGHT) .32f else .12f))),
            ),
        ) { content() }
    }
}
