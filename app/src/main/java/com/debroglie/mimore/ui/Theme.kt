package com.debroglie.mimore.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.debroglie.mimore.data.Accent
import com.debroglie.mimore.data.ThemeMode

@Immutable
data class MimorePalette(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val key: Color,
    val keyPressed: Color,
    val operator: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentSoft: Color,
    val danger: Color
)

val LocalMimorePalette = staticCompositionLocalOf {
    MimorePalette(
        background = Color(0xFF0B0C0E),
        surface = Color(0xFF111318),
        surfaceRaised = Color(0xFF171A20),
        key = Color(0xFF1B1E24),
        keyPressed = Color(0xFF242832),
        operator = Color(0xFF20242B),
        textPrimary = Color(0xFFF6F7F9),
        textSecondary = Color(0xFF9DA4B0),
        textTertiary = Color(0xFF6F7682),
        accent = Color(0xFFFF7A2A),
        accentSoft = Color(0x22FF7A2A),
        danger = Color(0xFFFF5F61)
    )
}

private fun palette(accent: Accent, dark: Boolean): MimorePalette {
    val accentColor = accent.color
    return if (dark) {
        MimorePalette(
            background = Color(0xFF090A0C),
            surface = Color(0xFF101217),
            surfaceRaised = Color(0xFF161920),
            key = Color(0xFF191C22),
            keyPressed = Color(0xFF252A33),
            operator = Color(0xFF20242B),
            textPrimary = Color(0xFFF6F7F9),
            textSecondary = Color(0xFF9CA3AE),
            textTertiary = Color(0xFF6D7480),
            accent = accentColor,
            accentSoft = accentColor.copy(alpha = 0.15f),
            danger = Color(0xFFFF5F61)
        )
    } else {
        MimorePalette(
            background = Color(0xFFF4F5F7),
            surface = Color.White,
            surfaceRaised = Color(0xFFF0F1F3),
            key = Color(0xFFE7E8EB),
            keyPressed = Color(0xFFD9DBE0),
            operator = Color(0xFFE0E2E6),
            textPrimary = Color(0xFF141518),
            textSecondary = Color(0xFF646A73),
            textTertiary = Color(0xFF90959D),
            accent = accentColor,
            accentSoft = accentColor.copy(alpha = 0.12f),
            danger = Color(0xFFC92832)
        )
    }
}

private fun colorsFor(palette: MimorePalette, dark: Boolean): ColorScheme = if (dark) {
    darkColorScheme(
        primary = palette.accent,
        onPrimary = Color.White,
        background = palette.background,
        onBackground = palette.textPrimary,
        surface = palette.surface,
        onSurface = palette.textPrimary,
        surfaceContainer = palette.surfaceRaised,
        onSurfaceVariant = palette.textSecondary,
        error = palette.danger
    )
} else {
    lightColorScheme(
        primary = palette.accent,
        onPrimary = Color.White,
        background = palette.background,
        onBackground = palette.textPrimary,
        surface = palette.surface,
        onSurface = palette.textPrimary,
        surfaceContainer = palette.surfaceRaised,
        onSurfaceVariant = palette.textSecondary,
        error = palette.danger
    )
}

@Composable
fun MimoreTheme(
    mode: ThemeMode,
    accent: Accent,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (mode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val target = palette(accent, dark)
    val animated = MimorePalette(
            background = animateColorAsState(target.background, label = "background").value,
            surface = animateColorAsState(target.surface, label = "surface").value,
            surfaceRaised = animateColorAsState(target.surfaceRaised, label = "surfaceRaised").value,
            key = animateColorAsState(target.key, label = "key").value,
            keyPressed = animateColorAsState(target.keyPressed, label = "keyPressed").value,
            operator = animateColorAsState(target.operator, label = "operator").value,
            textPrimary = animateColorAsState(target.textPrimary, label = "textPrimary").value,
            textSecondary = animateColorAsState(target.textSecondary, label = "textSecondary").value,
            textTertiary = animateColorAsState(target.textTertiary, label = "textTertiary").value,
            accent = animateColorAsState(target.accent, label = "accent").value,
            accentSoft = animateColorAsState(target.accentSoft, label = "accentSoft").value,
            danger = animateColorAsState(target.danger, label = "danger").value
    )

    androidx.compose.runtime.CompositionLocalProvider(LocalMimorePalette provides animated) {
        MaterialTheme(
            colorScheme = colorsFor(animated, dark),
            typography = MimoreTypography,
            shapes = MimoreShapes,
            content = content
        )
    }
}

