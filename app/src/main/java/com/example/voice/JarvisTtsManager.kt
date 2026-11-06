package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.remote.AutonomousJarvisEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JarvisTtsManager(context: Context) : TextToSpeech.OnInitListener {
  private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
  private var isInitialized = false

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      isInitialized = true
      tts?.language = Locale.US
      tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
          _isSpeaking.value = true
        }

        override fun onDone(utteranceId: String?) {
          _isSpeaking.value = false
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
          _isSpeaking.value = false
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
          _isSpeaking.value = false
        }
      })
    } else {
      Log.e("JarvisTts", "TextToSpeech init failed with status: $status")
    }
  }

  fun speak(text: String, pitch: Float = 1.0f, speed: Float = 1.0f) {
    if (!isInitialized || tts == null) return

    val cleanText = text.replace(Regex("\\[.*?\\]"), "").trim()
    if (cleanText.isEmpty()) return

    tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
    tts?.setSpeechRate(speed.coerceIn(0.5f, 2.0f))

    if (AutonomousJarvisEngine.isBanglaText(cleanText)) {
      val banglaLocale = Locale("bn", "BD")
      val available = tts?.isLanguageAvailable(banglaLocale)
      if (available == TextToSpeech.LANG_AVAILABLE || available == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
        tts?.language = banglaLocale
      } else {
        // Fallback to default/US if Bangla voice data not downloaded on device
        tts?.language = Locale.US
      }
    } else {
      tts?.language = Locale.US
    }

    val utteranceId = "jarvis_speech_${System.currentTimeMillis()}"
    tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
  }

  fun stop() {
    tts?.stop()
    _isSpeaking.value = false
  }

  fun release() {
    stop()
    tts?.shutdown()
    tts = null
    isInitialized = false
  }
}
