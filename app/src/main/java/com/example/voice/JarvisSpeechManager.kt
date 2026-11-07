package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Build
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

    private val _detectedLanguage = MutableStateFlow("auto")
    val detectedLanguage: StateFlow<String> = _detectedLanguage.asStateFlow()

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(
        languageCode: String = "auto",
        onResult: (String) -> Unit
    ) {
        if (!isAvailable()) {
            _speechError.value = "Speech recognition unavailable."
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
                    _rmsLevel.value =
                        ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                    _rmsLevel.value = 0f
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _rmsLevel.value = 0f

                    _speechError.value = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Speech client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                            "Microphone permission required"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                        SpeechRecognizer.ERROR_SERVER -> "Recognition server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                        else -> "Speech error: $error"
                    }
                    Log.w("JarvisSpeech", _speechError.value ?: "")
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    _rmsLevel.value = 0f

                    val matches = results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )

                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0].trim()
                        _lastRecognizedText.value = text

                        _detectedLanguage.value = detectLanguage(text)
                        onResult(text)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )

                    if (!matches.isNullOrEmpty()) {
                        _lastRecognizedText.value = matches[0]
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

            when (languageCode.lowercase()) {
                "bn", "bn-bd" -> {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE,
                        Locale("bn", "BD").toLanguageTag()
                    )
                }

                "en", "en-us" -> {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE,
                        Locale.US.toLanguageTag()
                    )
                }

                else -> {
                    // Android 10 fallback: use device locale.
                    // Final language selection is also handled from text.
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE,
                        Locale.getDefault().toLanguageTag()
                    )
                }
            }
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _speechError.value = e.message ?: "Unable to start recognition"
            _isListening.value = false
        }
    }

    private fun detectLanguage(text: String): String {
        val banglaCount = text.count { it in '\u0980'..'\u09FF' }
        val latinCount = text.count { it in 'A'..'Z' || it in 'a'..'z' }

        return when {
            banglaCount > 0 && latinCount > 0 -> "mixed"
            banglaCount > 0 -> "bn"
            latinCount > 0 -> "en"
            else -> "auto"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }

        speechRecognizer = null
        _isListening.value = false
        _rmsLevel.value = 0f
    }

    fun clearError() {
        _speechError.value = null
    }
}
