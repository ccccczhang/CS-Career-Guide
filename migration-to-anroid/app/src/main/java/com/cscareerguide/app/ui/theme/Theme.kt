package com.cscareerguide.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object CareerPalette {
    val Canvas = Color(0xFFFAF9F6)
    val Surface = Color(0xFFFFFFFF)
    val Ink = Color(0xFF242723)
    val Muted = Color(0xFF6E716A)
    val Border = Color(0xFFE1E2DB)
    val Accent = Color(0xFF333F32)
    val AccentSoft = Color(0xFFEFF2E9)
    val Moss = Color(0xFF526349)
    val WarningSoft = Color(0xFFF8F1E0)
    val Warning = Color(0xFF855A1C)
    val Error = Color(0xFFB42318)
    val ErrorSoft = Color(0xFFFFF4F2)
}

private val CareerColorScheme = lightColorScheme(
    primary = CareerPalette.Accent,
    onPrimary = CareerPalette.Surface,
    primaryContainer = CareerPalette.AccentSoft,
    onPrimaryContainer = CareerPalette.Ink,
    secondary = CareerPalette.Moss,
    onSecondary = CareerPalette.Surface,
    background = CareerPalette.Canvas,
    onBackground = CareerPalette.Ink,
    surface = CareerPalette.Surface,
    onSurface = CareerPalette.Ink,
    surfaceVariant = CareerPalette.AccentSoft,
    onSurfaceVariant = CareerPalette.Muted,
    outline = CareerPalette.Border,
    error = CareerPalette.Error,
    errorContainer = CareerPalette.ErrorSoft,
)

private val CareerTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(
            fontSize = 36.sp,
            lineHeight = 46.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        headlineMedium = headlineMedium.copy(
            fontSize = 28.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        titleLarge = titleLarge.copy(
            fontSize = 22.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        titleMedium = titleMedium.copy(
            fontSize = 17.sp,
            lineHeight = 25.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        bodyLarge = bodyLarge.copy(fontSize = 16.sp, lineHeight = 26.sp),
        bodyMedium = bodyMedium.copy(fontSize = 14.sp, lineHeight = 23.sp),
        labelLarge = labelLarge.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
        ),
    )
}

@Composable
fun CsCareerGuideTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CareerColorScheme,
        typography = CareerTypography,
        content = content,
    )
}
