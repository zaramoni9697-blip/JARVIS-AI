package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class JarvisSettings(
  val apiBaseUrl: String = "https://ais-dev-o2aw2k2hnd53zxkaf44osk-459053698035.asia-southeast1.run.app",
  val themeMode: String = "CYAN_DEFAULT", // "CYAN_DEFAULT", "EMERALD_MATRIX", "PURPLE_NEON"
  val isDarkTheme: Boolean = true,
  val defaultLanguage: String = "AUTO", // "AUTO", "EN", "BN"
  val ttsSpeed: Float = 1.0f,
  val ttsPitch: Float = 1.0f,
  val autoSpeakResponse: Boolean = true,
  val isMemoryOptIn: Boolean = true,
  val notificationsEnabled: Boolean = true
)

class AppPreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("jarvis_prefs", Context.MODE_PRIVATE)

  private var currentUserId: String = "default"

  private val _settings = MutableStateFlow(loadSettings(currentUserId))
  val settings: StateFlow<JarvisSettings> = _settings.asStateFlow()

  fun getLastActiveUserId(): String? {
    return prefs.getString("last_active_user_id", null)
  }

  fun setLastActiveUserId(userId: String?) {
    if (userId != null) {
      prefs.edit().putString("last_active_user_id", userId).apply()
      switchUser(userId)
    } else {
      prefs.edit().remove("last_active_user_id").apply()
    }
  }

  fun switchUser(userId: String) {
    currentUserId = userId
    _settings.value = loadSettings(userId)
  }

  private fun loadSettings(userId: String): JarvisSettings {
    val prefix = "user_${userId}_"
    return JarvisSettings(
      apiBaseUrl = prefs.getString("${prefix}api_base_url", "https://ais-dev-o2aw2k2hnd53zxkaf44osk-459053698035.asia-southeast1.run.app")
        ?: "https://ais-dev-o2aw2k2hnd53zxkaf44osk-459053698035.asia-southeast1.run.app",
      themeMode = prefs.getString("${prefix}theme_mode", "CYAN_DEFAULT") ?: "CYAN_DEFAULT",
      isDarkTheme = prefs.getBoolean("${prefix}is_dark_theme", true),
      defaultLanguage = prefs.getString("${prefix}default_language", "AUTO") ?: "AUTO",
      ttsSpeed = prefs.getFloat("${prefix}tts_speed", 1.0f),
      ttsPitch = prefs.getFloat("${prefix}tts_pitch", 1.0f),
      autoSpeakResponse = prefs.getBoolean("${prefix}auto_speak_response", true),
      isMemoryOptIn = prefs.getBoolean("${prefix}is_memory_opt_in", true),
      notificationsEnabled = prefs.getBoolean("${prefix}notifications_enabled", true)
    )
  }

  fun updateApiBaseUrl(url: String) {
    val cleanUrl = url.trim().trimEnd('/')
    val prefix = "user_${currentUserId}_"
    prefs.edit().putString("${prefix}api_base_url", cleanUrl).apply()
    _settings.value = _settings.value.copy(apiBaseUrl = cleanUrl)
  }

  fun updateThemeMode(theme: String) {
    val prefix = "user_${currentUserId}_"
    prefs.edit().putString("${prefix}theme_mode", theme).apply()
    _settings.value = _settings.value.copy(themeMode = theme)
  }

  fun updateDarkTheme(isDark: Boolean) {
    val prefix = "user_${currentUserId}_"
    prefs.edit().putBoolean("${prefix}is_dark_theme", isDark).apply()
    _settings.value = _settings.value.copy(isDarkTheme = isDark)
  }

  fun updateDefaultLanguage(lang: String) {
    val prefix = "user_${currentUserId}_"
    prefs.edit().putString("${prefix}default_language", lang).apply()
    _settings.value = _settings.value.copy(defaultLanguage = lang)
  }

  fun updateTtsSettings(speed: Float, pitch: Float, autoSpeak: Boolean) {
    val prefix = "user_${currentUserId}_"
    prefs.edit()
      .putFloat("${prefix}tts_speed", speed)
      .putFloat("${prefix}tts_pitch", pitch)
      .putBoolean("${prefix}auto_speak_response", autoSpeak)
      .apply()
    _settings.value = _settings.value.copy(
      ttsSpeed = speed,
      ttsPitch = pitch,
      autoSpeakResponse = autoSpeak
    )
  }

  fun updateMemoryOptIn(optIn: Boolean) {
    val prefix = "user_${currentUserId}_"
    prefs.edit().putBoolean("${prefix}is_memory_opt_in", optIn).apply()
    _settings.value = _settings.value.copy(isMemoryOptIn = optIn)
  }

  fun updateNotificationsEnabled(enabled: Boolean) {
    val prefix = "user_${currentUserId}_"
    prefs.edit().putBoolean("${prefix}notifications_enabled", enabled).apply()
    _settings.value = _settings.value.copy(notificationsEnabled = enabled)
  }

  fun deleteUserData(userId: String) {
    val prefix = "user_${userId}_"
    prefs.edit()
      .remove("${prefix}api_base_url")
      .remove("${prefix}theme_mode")
      .remove("${prefix}is_dark_theme")
      .remove("${prefix}default_language")
      .remove("${prefix}tts_speed")
      .remove("${prefix}tts_pitch")
      .remove("${prefix}auto_speak_response")
      .remove("${prefix}is_memory_opt_in")
      .remove("${prefix}notifications_enabled")
      .apply()
  }
}
