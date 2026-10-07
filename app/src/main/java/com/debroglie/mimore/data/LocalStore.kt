package com.debroglie.mimore.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("mimore_local", Context.MODE_PRIVATE)

    fun loadSettings(): AppSettings = AppSettings(
        themeMode = runCatching { ThemeMode.valueOf(prefs.getString("theme", ThemeMode.SYSTEM.name)!!) }.getOrDefault(ThemeMode.SYSTEM),
        accent = runCatching { Accent.valueOf(prefs.getString("accent", Accent.Orange.name)!!) }.getOrDefault(Accent.Orange),
        hapticsEnabled = prefs.getBoolean("haptics", true),
        hapticLevel = prefs.getInt("haptic_level", 2),
        grouping = prefs.getBoolean("grouping", true),
        keepHistory = prefs.getBoolean("keep_history", true)
    )

    fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putString("theme", settings.themeMode.name)
            .putString("accent", settings.accent.name)
            .putBoolean("haptics", settings.hapticsEnabled)
            .putInt("haptic_level", settings.hapticLevel)
            .putBoolean("grouping", settings.grouping)
            .putBoolean("keep_history", settings.keepHistory)
            .apply()
    }

    fun loadHistory(): List<HistoryEntry> {
        if (!prefs.getBoolean("keep_history", true)) return emptyList()
        val raw = prefs.getString("history", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(
                        HistoryEntry(
                            id = item.getLong("id"),
                            expression = item.getString("expression"),
                            result = item.getString("result"),
                            timestamp = item.getLong("timestamp")
                        )
                    )
                }
            }
        }.getOrElse { emptyList() }
    }

    fun saveHistory(history: List<HistoryEntry>) {
        val array = JSONArray()
        history.take(100).forEach { entry ->
            array.put(
                JSONObject().apply {
                    put("id", entry.id)
                    put("expression", entry.expression)
                    put("result", entry.result)
                    put("timestamp", entry.timestamp)
                }
            )
        }
        prefs.edit().putString("history", array.toString()).apply()
    }
}
