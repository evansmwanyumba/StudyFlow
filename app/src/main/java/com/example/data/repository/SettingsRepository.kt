package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val morningOffsetSeconds: Int = 7200, // 2 hours default
    val sessionOffsetSeconds: Int = 1800, // 30 minutes default
    val teacherOffsetSeconds: Int = 900,  // 15 minutes default
    val notifyOnSessionStart: Boolean = true, // Notifies when the session is starting/started
    val selectedRingtone: String = "Default Loud Alarm",
    val vibrateInSilentMode: Boolean = true,
    val autoDiscardPassedTasks: Boolean = true,
    val customGeminiApiKey: String = ""
) {
    val morningOffsetMinutes: Int get() = morningOffsetSeconds / 60
    val sessionOffsetMinutes: Int get() = sessionOffsetSeconds / 60
    val teacherOffsetMinutes: Int get() = teacherOffsetSeconds / 60

    fun formatDuration(seconds: Int): String {
        return when {
            seconds % 3600 == 0 && seconds >= 3600 -> "${seconds / 3600} hr"
            seconds % 60 == 0 && seconds >= 60 -> "${seconds / 60} min"
            seconds >= 60 -> "${seconds / 60}m ${seconds % 60}s"
            else -> "$seconds sec"
        }
    }
}

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("studyflow_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        // Read seconds, with fallback to legacy minutes if present
        val morningSec = if (prefs.contains("morning_offset_sec")) {
            prefs.getInt("morning_offset_sec", 7200)
        } else {
            prefs.getInt("morning_offset", 120) * 60
        }

        val sessionSec = if (prefs.contains("session_offset_sec")) {
            prefs.getInt("session_offset_sec", 1800)
        } else {
            prefs.getInt("session_offset", 30) * 60
        }

        val teacherSec = if (prefs.contains("teacher_offset_sec")) {
            prefs.getInt("teacher_offset_sec", 900)
        } else {
            prefs.getInt("teacher_offset", 15) * 60
        }

        return UserSettings(
            morningOffsetSeconds = morningSec,
            sessionOffsetSeconds = sessionSec,
            teacherOffsetSeconds = teacherSec,
            notifyOnSessionStart = prefs.getBoolean("notify_session_start", true),
            selectedRingtone = prefs.getString("ringtone", "Default Loud Alarm") ?: "Default Loud Alarm",
            vibrateInSilentMode = prefs.getBoolean("vibrate_silent", true),
            autoDiscardPassedTasks = prefs.getBoolean("auto_discard", true),
            customGeminiApiKey = prefs.getString("gemini_key", "") ?: ""
        )
    }

    fun updateMorningOffsetSeconds(seconds: Int) {
        prefs.edit().putInt("morning_offset_sec", seconds).apply()
        _settings.value = _settings.value.copy(morningOffsetSeconds = seconds)
    }

    fun updateMorningOffset(minutes: Int) {
        updateMorningOffsetSeconds(minutes * 60)
    }

    fun updateSessionOffsetSeconds(seconds: Int) {
        prefs.edit().putInt("session_offset_sec", seconds).apply()
        _settings.value = _settings.value.copy(sessionOffsetSeconds = seconds)
    }

    fun updateSessionOffset(minutes: Int) {
        updateSessionOffsetSeconds(minutes * 60)
    }

    fun updateTeacherOffsetSeconds(seconds: Int) {
        prefs.edit().putInt("teacher_offset_sec", seconds).apply()
        _settings.value = _settings.value.copy(teacherOffsetSeconds = seconds)
    }

    fun updateTeacherOffset(minutes: Int) {
        updateTeacherOffsetSeconds(minutes * 60)
    }

    fun updateNotifyOnSessionStart(enabled: Boolean) {
        prefs.edit().putBoolean("notify_session_start", enabled).apply()
        _settings.value = _settings.value.copy(notifyOnSessionStart = enabled)
    }

    fun updateRingtone(ringtone: String) {
        prefs.edit().putString("ringtone", ringtone).apply()
        _settings.value = _settings.value.copy(selectedRingtone = ringtone)
    }

    fun updateVibrateInSilent(enabled: Boolean) {
        prefs.edit().putBoolean("vibrate_silent", enabled).apply()
        _settings.value = _settings.value.copy(vibrateInSilentMode = enabled)
    }

    fun updateAutoDiscard(enabled: Boolean) {
        prefs.edit().putBoolean("auto_discard", enabled).apply()
        _settings.value = _settings.value.copy(autoDiscardPassedTasks = enabled)
    }

    fun updateCustomGeminiKey(key: String) {
        prefs.edit().putString("gemini_key", key.trim()).apply()
        _settings.value = _settings.value.copy(customGeminiApiKey = key.trim())
    }

    fun resetAll() {
        prefs.edit().clear().apply()
        _settings.value = UserSettings()
    }
}
