package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JarvisSpeechManager(private val context: Context) {
  private var speechRecognizer: SpeechRecognizer? = null

  private val _isListening = MutableStateFlow(false)
  val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

  private val _rmsLevel = MutableStateFlow(0f)
  val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

  private val _lastRecognizedText = MutableStateFlow<String?>(null)
  val lastRecognizedText: StateFlow<String?> = _lastRecognizedText.asStateFlow()

  private val _speechError = MutableStateFlow<String?>(null)
  val speechError: StateFlow<String?> = _speechError.asStateFlow()

  fun isAvailable(): Boolean {
    return SpeechRecognizer.isRecognitionAvailable(context)
  }

  fun startListening(languageCode: String = "en-US", onResult: (String) -> Unit) {
    if (!isAvailable()) {
      _speechError.value = "Speech recognition service unavailable on this device."
      return
    }

    stopListening()

    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
      setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
          _isListening.value = true
          _speechError.value = null
        }

        override fun onBeginningOfSpeech() {
          _isListening.value = true
        }

        override fun onRmsChanged(rmsdB: Float) {
          // Normalize typical -2dB..10dB to 0f..1f range for visualizer
          val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
          _rmsLevel.value = normalized
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
          _isListening.value = false
          _rmsLevel.value = 0f
        }

        override fun onError(error: Int) {
          _isListening.value = false
          _rmsLevel.value = 0f
          val errorMsg = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client speech error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No voice match recognized"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
            else -> "Speech error code $error"
          }
          Log.w("JarvisSpeech", errorMsg)
          _speechError.value = errorMsg
        }

        override fun onResults(results: Bundle?) {
          _isListening.value = false
          _rmsLevel.value = 0f
          val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          if (!matches.isNullOrEmpty()) {
            val text = matches[0]
            _lastRecognizedText.value = text
            onResult(text)
          }
        }

        override fun onPartialResults(partialResults: Bundle?) {
          val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          if (!partialMatches.isNullOrEmpty()) {
            _lastRecognizedText.value = partialMatches[0]
          }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
      })
    }

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
      putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

      val locale = if (languageCode.startsWith("bn")) {
        Locale("bn", "BD")
      } else {
        Locale.US
      }
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale.toLanguageTag())
    }

    try {
      speechRecognizer?.startListening(intent)
    } catch (e: Exception) {
      _speechError.value = "Failed to activate speech recognizer: ${e.message}"
      _isListening.value = false
    }
  }

  fun stopListening() {
    try {
      speechRecognizer?.stopListening()
      speechRecognizer?.destroy()
    } catch (_: Exception) {}
    speechRecognizer = null
    _isListening.value = false
    _rmsLevel.value = 0f
  }

  fun clearError() {
    _speechError.value = null
  }
}
