package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.JarvisDatabase
import com.example.data.local.JarvisSettings
import com.example.data.local.MemoryEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.VoiceShortcutEntity
import com.example.data.repository.JarvisRepository
import com.example.data.security.JarvisKeyStoreManager
import com.example.ui.components.JarvisCoreState
import com.example.voice.JarvisSpeechManager
import com.example.voice.JarvisTtsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

sealed class VoiceActionResult {
  data class Navigated(val destination: String, val message: String) : VoiceActionResult()
  data class SpokenFeedback(val phrase: String) : VoiceActionResult()
  data class Error(val reason: String) : VoiceActionResult()
}

class JarvisViewModel(application: Application) : AndroidViewModel(application) {
  private val database = JarvisDatabase.getDatabase(application)
  private val preferences = AppPreferences(application)
  val repository = JarvisRepository(application, database, preferences)

  val speechManager = JarvisSpeechManager(application)
  val ttsManager = JarvisTtsManager(application)

  val settings: StateFlow<JarvisSettings> = preferences.settings

  // User Profile & Authentication State
  val currentUser: StateFlow<UserProfileEntity?> = repository.currentUser
  val allProfiles: StateFlow<List<UserProfileEntity>> = repository.getAllProfiles()
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  private val _isAuthChecking = MutableStateFlow(true)
  val isAuthChecking: StateFlow<Boolean> = _isAuthChecking.asStateFlow()

  // API Key BYOK state for current user
  private val _maskedApiKey = MutableStateFlow<String>("")
  val maskedApiKey: StateFlow<String> = _maskedApiKey.asStateFlow()

  private val _isVerifyingKey = MutableStateFlow(false)
  val isVerifyingKey: StateFlow<Boolean> = _isVerifyingKey.asStateFlow()

  private val _keyVerificationStatus = MutableStateFlow<String?>(null)
  val keyVerificationStatus: StateFlow<String?> = _keyVerificationStatus.asStateFlow()

  // Current active chat session
  private val _activeSessionId = MutableStateFlow<String?>(null)
  val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

  private val _currentMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
  val currentMessages: StateFlow<List<ChatMessageEntity>> = _currentMessages.asStateFlow()

  private val _allSessions = MutableStateFlow<List<ChatSessionEntity>>(emptyList())
  val allSessions: StateFlow<List<ChatSessionEntity>> = _allSessions.asStateFlow()

  private val _allMemories = MutableStateFlow<List<MemoryEntity>>(emptyList())
  val allMemories: StateFlow<List<MemoryEntity>> = _allMemories.asStateFlow()

  private val _allShortcuts = MutableStateFlow<List<VoiceShortcutEntity>>(emptyList())
  val allShortcuts: StateFlow<List<VoiceShortcutEntity>> = _allShortcuts.asStateFlow()

  // Core Status state
  private val _isQueryProcessing = MutableStateFlow(false)
  val isQueryProcessing: StateFlow<Boolean> = _isQueryProcessing.asStateFlow()

  val coreState: StateFlow<JarvisCoreState> = combine(
    speechManager.isListening,
    _isQueryProcessing,
    ttsManager.isSpeaking
  ) { isListening, isProcessing, isSpeaking ->
    when {
      isListening -> JarvisCoreState.LISTENING
      isProcessing -> JarvisCoreState.PROCESSING
      isSpeaking -> JarvisCoreState.SPEAKING
      else -> JarvisCoreState.IDLE
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JarvisCoreState.IDLE)

  // Chat UI state
  private val _chatLanguage = MutableStateFlow("auto")
  val chatLanguage: StateFlow<String> = _chatLanguage.asStateFlow()

  private val _networkLatency = MutableStateFlow<Long?>(null)
  val networkLatency: StateFlow<Long?> = _networkLatency.asStateFlow()

  private val _lastError = MutableStateFlow<String?>(null)
  val lastError: StateFlow<String?> = _lastError.asStateFlow()

  // Voice command action events
  private val _voiceActionEvent = MutableSharedFlow<VoiceActionResult>()
  val voiceActionEvent: SharedFlow<VoiceActionResult> = _voiceActionEvent.asSharedFlow()

  // Image Analysis state
  private val _selectedImageUri = MutableStateFlow<Uri?>(null)
  val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

  private val _isAnalyzingImage = MutableStateFlow(false)
  val isAnalyzingImage: StateFlow<Boolean> = _isAnalyzingImage.asStateFlow()

  private val _imageAnalysisResult = MutableStateFlow<String?>(null)
  val imageAnalysisResult: StateFlow<String?> = _imageAnalysisResult.asStateFlow()

  // Test Connection state in Settings
  private val _testConnectionResult = MutableStateFlow<String?>(null)
  val testConnectionResult: StateFlow<String?> = _testConnectionResult.asStateFlow()

  private val _isTestingConnection = MutableStateFlow(false)
  val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

  init {
    viewModelScope.launch {
      val user = repository.initializeAuth()
      _isAuthChecking.value = false
      if (user != null) {
        onUserLoggedIn(user)
      }
    }
  }

  // --- Auth Actions ---
  fun signup(
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String,
    rememberMe: Boolean,
    onSuccess: (UserProfileEntity) -> Unit,
    onError: (String) -> Unit
  ) {
    if (password != confirmPassword) {
      onError("Passwords do not match.")
      return
    }

    viewModelScope.launch {
      val result = repository.signup(fullName, email, password, rememberMe)
      if (result.isSuccess) {
        val user = result.getOrNull()!!
        onUserLoggedIn(user)
        onSuccess(user)
      } else {
        onError(result.exceptionOrNull()?.message ?: "Signup failed.")
      }
    }
  }

  fun login(
    email: String,
    password: String,
    rememberMe: Boolean,
    onSuccess: (UserProfileEntity) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val result = repository.login(email, password, rememberMe)
      if (result.isSuccess) {
        val user = result.getOrNull()!!
        onUserLoggedIn(user)
        onSuccess(user)
      } else {
        onError(result.exceptionOrNull()?.message ?: "Login failed.")
      }
    }
  }

  fun logout(onSuccess: () -> Unit) {
    viewModelScope.launch {
      repository.logout()
      _activeSessionId.value = null
      _currentMessages.value = emptyList()
      _allSessions.value = emptyList()
      _allMemories.value = emptyList()
      _maskedApiKey.value = ""
      onSuccess()
    }
  }

  fun switchProfile(userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    viewModelScope.launch {
      val result = repository.switchUser(userId)
      if (result.isSuccess) {
        onUserLoggedIn(result.getOrNull()!!)
        onSuccess()
      } else {
        onError(result.exceptionOrNull()?.message ?: "Could not switch profile.")
      }
    }
  }

  fun deleteProfile(userId: String, onSuccess: () -> Unit) {
    viewModelScope.launch {
      repository.deleteProfile(userId)
      onSuccess()
    }
  }

  private suspend fun onUserLoggedIn(user: UserProfileEntity) {
    preferences.switchUser(user.userId)

    // Load masked API key if available
    val key = repository.getUserDecryptedApiKey(user.userId)
    if (!key.isNullOrBlank()) {
      _maskedApiKey.value = JarvisKeyStoreManager.maskApiKey(key)
    } else {
      _maskedApiKey.value = ""
    }

    // Refresh isolated flows
    observeUserData(user.userId)

    // Start or load initial chat session for this user
    val sessions = database.chatDao().getAllSessionsListForUser(user.userId)
    val sessionId = if (sessions.isNotEmpty()) {
      sessions.first().sessionId
    } else {
      repository.createNewSession("JARVIS Protocol Alpha", user.userId)
    }
    _activeSessionId.value = sessionId
    observeSessionMessages(sessionId, user.userId)
  }

  private fun observeUserData(userId: String) {
    viewModelScope.launch {
      repository.getAllSessions(userId).collect { sessions ->
        _allSessions.value = sessions
      }
    }

    viewModelScope.launch {
      repository.getAllMemories(userId).collect { memories ->
        _allMemories.value = memories
      }
    }

    viewModelScope.launch {
      repository.getAllVoiceShortcuts(userId).collect { shortcuts ->
        _allShortcuts.value = shortcuts
      }
    }
  }

  // --- Personal API Key (BYOK) Actions ---
  fun verifyApiKeyOnly(apiKey: String) {
    val cleanKey = apiKey.trim()
    if (cleanKey.isBlank()) {
      _keyVerificationStatus.value = "Please enter an API Key to verify."
      return
    }

    viewModelScope.launch {
      _isVerifyingKey.value = true
      _keyVerificationStatus.value = null
      val result = repository.verifyGeminiApiKey(cleanKey)
      _isVerifyingKey.value = false
      if (result.isSuccess) {
        _keyVerificationStatus.value = "SUCCESS: ${result.getOrNull()}"
      } else {
        _keyVerificationStatus.value = "ERROR: ${result.exceptionOrNull()?.message ?: "Key validation rejected"}"
      }
    }
  }

  fun saveApiKey(apiKey: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val user = currentUser.value
    if (user == null) {
      onError("No active local profile found.")
      return
    }

    val cleanKey = apiKey.trim()
    if (cleanKey.isBlank()) {
      onError("API key cannot be empty.")
      return
    }

    viewModelScope.launch {
      _isVerifyingKey.value = true
      val validation = repository.verifyGeminiApiKey(cleanKey)
      _isVerifyingKey.value = false

      val isVerified = validation.isSuccess
      repository.saveUserApiKey(user.userId, cleanKey, isVerified)
      _maskedApiKey.value = JarvisKeyStoreManager.maskApiKey(cleanKey)

      if (isVerified) {
        onSuccess()
      } else {
        // Still allow saving if user desires offline/custom, but report warning
        onError("Saved with warning: ${validation.exceptionOrNull()?.message}")
      }
    }
  }

  fun removeApiKey(onSuccess: () -> Unit) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      repository.removeUserApiKey(user.userId)
      _maskedApiKey.value = ""
      _keyVerificationStatus.value = null
      onSuccess()
    }
  }

  // --- Chat & Session Actions ---
  fun switchSession(sessionId: String) {
    val userId = currentUser.value?.userId ?: return
    _activeSessionId.value = sessionId
    observeSessionMessages(sessionId, userId)
  }

  fun createNewSessionAndSwitch(onCreated: ((String) -> Unit)? = null) {
    val userId = currentUser.value?.userId ?: return
    viewModelScope.launch {
      val newId = repository.createNewSession("JARVIS Conversation", userId)
      _activeSessionId.value = newId
      observeSessionMessages(newId, userId)
      onCreated?.invoke(newId)
    }
  }

  private fun observeSessionMessages(sessionId: String, userId: String) {
    viewModelScope.launch {
      repository.getMessagesForSession(sessionId, userId).collect { messages ->
        _currentMessages.value = messages
      }
    }
  }

  fun setChatLanguage(lang: String) {
    _chatLanguage.value = lang
  }

  fun sendChatMessage(text: String, imageUri: String? = null, imageBase64: String? = null) {
    val user = currentUser.value ?: return
    val sessionId = _activeSessionId.value ?: return
    if (text.isBlank() && imageBase64 == null) return

    viewModelScope.launch {
      _isQueryProcessing.value = true
      _lastError.value = null

      try {
        val aiMessage = repository.sendMessage(
          sessionId = sessionId,
          userId = user.userId,
          userText = text,
          languageHint = _chatLanguage.value,
          imageUri = imageUri,
          imageBase64 = imageBase64
        )
        _networkLatency.value = aiMessage.latencyMs

        if (settings.value.autoSpeakResponse) {
          ttsManager.speak(
            text = aiMessage.content,
            pitch = settings.value.ttsPitch,
            speed = settings.value.ttsSpeed
          )
        }
      } catch (e: Exception) {
        _lastError.value = "Transmission failed: ${e.message}"
      } finally {
        _isQueryProcessing.value = false
      }
    }
  }

  // --- Voice Commands ---
  fun startVoiceListening(languageCode: String = "auto") {
    speechManager.startListening(languageCode) { recognizedText ->
        handleSpokenInput(recognizedText)
    }
  }
  fun stopVoiceListening() {
    speechManager.stopListening()
  }

  fun handleSpokenInput(spokenText: String) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      val matchedShortcut = repository.matchVoiceCommand(spokenText, user.userId)
      if (matchedShortcut != null) {
        executeVoiceShortcut(matchedShortcut)
      } else {
        sendChatMessage(spokenText)
      }
    }
  }

  private suspend fun executeVoiceShortcut(shortcut: VoiceShortcutEntity) {
    when (shortcut.actionType) {
      "NAVIGATE_IMAGE_ANALYSIS" -> {
        ttsManager.speak("Activating holographic image analysis matrix.", settings.value.ttsPitch, settings.value.ttsSpeed)
        _voiceActionEvent.emit(VoiceActionResult.Navigated("vision", "Image Analysis activated"))
      }
      "NAVIGATE_NEW_CHAT" -> {
        createNewSessionAndSwitch {
          ttsManager.speak("New chat session initialized.", settings.value.ttsPitch, settings.value.ttsSpeed)
        }
        _voiceActionEvent.emit(VoiceActionResult.Navigated("chat", "New chat started"))
      }
      "PLAY_VOICE_STATUS" -> {
        val statusPhrase = "All systems operational, sir. JARVIS Core v4.2 is online with personal BYOK encryption. Local profile authenticated."
        ttsManager.speak(statusPhrase, settings.value.ttsPitch, settings.value.ttsSpeed)
        _voiceActionEvent.emit(VoiceActionResult.SpokenFeedback(statusPhrase))
      }
      "PLAY_BANGLA_QUOTE" -> {
        val quotePhrase = "জ্ঞানের আলোই মানুষের শ্রেষ্ঠ শক্তি। আপনার প্রতিটি পদক্ষেপে আমি সাথে আছি।"
        ttsManager.speak(quotePhrase, settings.value.ttsPitch, settings.value.ttsSpeed)
        _voiceActionEvent.emit(VoiceActionResult.SpokenFeedback(quotePhrase))
      }
      "NAVIGATE_MEMORY" -> {
        ttsManager.speak("Accessing memory manager.", settings.value.ttsPitch, settings.value.ttsSpeed)
        _voiceActionEvent.emit(VoiceActionResult.Navigated("memory", "Memory Manager opened"))
      }
      "NAVIGATE_VOICE" -> {
        ttsManager.speak("Voice assistant ready. Standing by.", settings.value.ttsPitch, settings.value.ttsSpeed)
        _voiceActionEvent.emit(VoiceActionResult.Navigated("voice", "Voice Assistant active"))
      }
      else -> {
        ttsManager.speak("Executing command: ${shortcut.title}", settings.value.ttsPitch, settings.value.ttsSpeed)
        _voiceActionEvent.emit(VoiceActionResult.SpokenFeedback("Command triggered: ${shortcut.title}"))
      }
    }
  }

  // --- Image Analysis ---
  fun setImageUri(uri: Uri?) {
    _selectedImageUri.value = uri
    _imageAnalysisResult.value = null
  }

  fun analyzeImage(contextPrompt: String = "Analyze this image in detail and describe what you see.") {
    val user = currentUser.value ?: return
    val uri = _selectedImageUri.value ?: return
    viewModelScope.launch {
      _isAnalyzingImage.value = true
      _lastError.value = null

      try {
        val base64 = withContext(Dispatchers.IO) {
          convertUriToBase64(uri)
        }

        val sessionId = _activeSessionId.value ?: repository.createNewSession("Visual Scan", user.userId)
        _activeSessionId.value = sessionId

        val aiMsg = repository.sendMessage(
          sessionId = sessionId,
          userId = user.userId,
          userText = contextPrompt,
          languageHint = _chatLanguage.value,
          imageUri = uri.toString(),
          imageBase64 = base64
        )

        _imageAnalysisResult.value = aiMsg.content

        if (settings.value.autoSpeakResponse) {
          ttsManager.speak(aiMsg.content, settings.value.ttsPitch, settings.value.ttsSpeed)
        }
      } catch (e: Exception) {
        _imageAnalysisResult.value = "Image analysis could not be completed: ${e.message}"
      } finally {
        _isAnalyzingImage.value = false
      }
    }
  }

  private fun convertUriToBase64(uri: Uri): String? {
    return try {
      val context = getApplication<Application>()
      val inputStream = context.contentResolver.openInputStream(uri)
      val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null
      val scaledBitmap = if (bitmap.width > 800 || bitmap.height > 800) {
        val scale = 800f / maxOf(bitmap.width, bitmap.height)
        Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
      } else bitmap

      val outputStream = ByteArrayOutputStream()
      scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
      Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    } catch (_: Exception) {
      null
    }
  }

  // --- Conversation History CRUD ---
  fun deleteSession(sessionId: String) {
    val userId = currentUser.value?.userId ?: return
    viewModelScope.launch {
      repository.deleteSession(sessionId, userId)
      if (_activeSessionId.value == sessionId) {
        createNewSessionAndSwitch()
      }
    }
  }

  fun clearAllHistory() {
    val userId = currentUser.value?.userId ?: return
    viewModelScope.launch {
      repository.clearAllHistory(userId)
      createNewSessionAndSwitch()
    }
  }

  // --- Memory Manager CRUD ---
  fun addMemory(category: String, key: String, value: String) {
    val userId = currentUser.value?.userId ?: return
    viewModelScope.launch {
      repository.saveMemory(category, key, value, userId)
    }
  }

  fun updateMemory(memory: MemoryEntity) {
    viewModelScope.launch {
      repository.updateMemory(memory)
    }
  }

  fun deleteMemory(id: Long) {
    val userId = currentUser.value?.userId ?: return
    viewModelScope.launch {
      repository.deleteMemory(id, userId)
    }
  }

  fun clearAllMemories() {
    val userId = currentUser.value?.userId ?: return
    viewModelScope.launch {
      repository.clearAllMemories(userId)
    }
  }

  fun toggleMemoryOptIn(optIn: Boolean) {
    preferences.updateMemoryOptIn(optIn)
  }

  // --- Voice Shortcuts Config ---
  fun updateVoiceShortcut(shortcut: VoiceShortcutEntity) {
    viewModelScope.launch {
      repository.updateVoiceShortcut(shortcut)
    }
  }

  fun resetDefaultShortcuts() {
    val userId = currentUser.value?.userId ?: return
    viewModelScope.launch {
      repository.resetDefaultShortcuts(userId)
    }
  }

  // --- Settings & Diagnostics ---
  fun updateApiBaseUrl(url: String) {
    preferences.updateApiBaseUrl(url)
  }

  fun updateTheme(theme: String) {
    preferences.updateThemeMode(theme)
  }

  fun updateDarkTheme(isDark: Boolean) {
    preferences.updateDarkTheme(isDark)
  }

  fun updateDefaultLanguage(lang: String) {
    preferences.updateDefaultLanguage(lang)
    _chatLanguage.value = if (lang == "AUTO") "auto" else lang.lowercase()
  }

  fun updateTtsSettings(speed: Float, pitch: Float, autoSpeak: Boolean) {
    preferences.updateTtsSettings(speed, pitch, autoSpeak)
  }

  fun updateNotifications(enabled: Boolean) {
    preferences.updateNotificationsEnabled(enabled)
  }

  fun testBackendConnection() {
    viewModelScope.launch {
      _isTestingConnection.value = true
      _testConnectionResult.value = null
      val result = repository.testEndpointConnection()
      _isTestingConnection.value = false
      if (result.isSuccess) {
        val latency = result.getOrNull() ?: 0L
        _testConnectionResult.value = "ONLINE: Verified HTTPS handshake in ${latency}ms"
      } else {
        _testConnectionResult.value = "STATUS: ${result.exceptionOrNull()?.message ?: "Host unreachable"}. Autonomous fallback is active."
      }
    }
  }

  suspend fun exportCurrentUserData(): String {
    val userId = currentUser.value?.userId ?: return "{}"
    return repository.exportUserData(userId)
  }

  override fun onCleared() {
    super.onCleared()
    speechManager.stopListening()
    ttsManager.release()
  }
}
