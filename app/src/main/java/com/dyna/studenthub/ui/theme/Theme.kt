package com.dyna.studenthub.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont.Provider
import com.dyna.studenthub.R

val StudyBlue = Color(0xFF4F7CF0)
private val BackgroundPink = Color(0xFFFFF5F7)
private val BackgroundBlue = Color(0xFFEEF4FF)
private val Navy = Color(0xFF1E2A4A)
private val SecondaryText = Color(0xFF7A869A)

private val NunitoProvider = Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val Nunito = FontFamily(
    Font(
        GoogleFont("Nunito"),
        NunitoProvider,
        FontWeight.Normal,
    ),
    Font(
        GoogleFont("Nunito"),
        NunitoProvider,
        FontWeight.Bold,
    ),
)

private val StudentHubTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
        headlineLarge = headlineLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
        bodyLarge = bodyLarge.copy(fontFamily = Nunito),
        bodyMedium = bodyMedium.copy(fontFamily = Nunito, color = SecondaryText),
        labelLarge = labelLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
    )
}

private val StudentHubColors = lightColorScheme(
    primary = StudyBlue,
    onPrimary = Color.White,
    secondary = Color(0xFF1FB57A),
    onSecondary = Color.White,
    background = BackgroundPink,
    onBackground = Navy,
    surface = Color.White.copy(alpha = 0.9f),
    onSurface = Navy,
)

@Composable
fun StudentHubTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StudentHubColors,
        typography = StudentHubTypography,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BackgroundPink, BackgroundBlue),
                    ),
                ),
        ) {
            content()
        }
    }
}
