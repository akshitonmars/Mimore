package com.debroglie.mimore.data

import androidx.compose.ui.graphics.Color

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class Accent(val label: String, val color: Color) {
    Orange("Orange", Color(0xFFFF7A2A)),
    Blue("Blue", Color(0xFF4F8CFF)),
    Purple("Purple", Color(0xFF9B7BFF)),
    Green("Green", Color(0xFF34C759)),
    Red("Red", Color(0xFFFF5B64)),
    Pink("Pink", Color(0xFFFF6EB4))
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accent: Accent = Accent.Orange,
    val hapticsEnabled: Boolean = true,
    val hapticLevel: Int = 2,
    val grouping: Boolean = true,
    val keepHistory: Boolean = true
)

data class HistoryEntry(
    val id: Long,
    val expression: String,
    val result: String,
    val timestamp: Long
)
