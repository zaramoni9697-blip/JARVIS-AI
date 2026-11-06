package com.example.data.repository

import android.content.Context
import com.example.data.local.AppPreferences
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.JarvisDatabase
import com.example.data.local.MemoryEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.VoiceShortcutEntity
import com.example.data.remote.JarvisApiClient
import com.example.data.security.JarvisKeyStoreManager
import com.example.data.security.PasswordHasher
import com.example.voice.VoiceShortcutMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class JarvisRepository(
  private val context: Context,
  private val database: JarvisDatabase,
  val preferences: AppPreferences
) {
  private val userDao = database.userDao()
  private val chatDao = database.chatDao()
  private val memoryDao = database.memoryDao()
  private val shortcutDao = database.voiceShortcutDao()
  private val apiClient = JarvisApiClient()

  private val _currentUser = MutableStateFlow<UserProfileEntity?>(null)
  val currentUser: StateFlow<UserProfileEntity?> = _currentUser.asStateFlow()

  suspend fun initializeAuth(): UserProfileEntity? = withContext(Dispatchers.IO) {
    // 1. Check remembered user from database
    val remembered = userDao.getRememberedUser()
    if (remembered != null) {
      _currentUser.value = remembered
      preferences.setLastActiveUserId(remembered.userId)
      seedDefaultShortcutsForUser(remembered.userId)
      return@withContext remembered
    }

    // 2. Check last active user from prefs
    val lastUserId = preferences.getLastActiveUserId()
    if (lastUserId != null) {
      val user = userDao.getUserById(lastUserId)
      if (user != null) {
        _currentUser.value = user
        seedDefaultShortcutsForUser(user.userId)
        return@withContext user
      }
    }

    null
  }

  // --- Authentication ---
  suspend fun signup(
    fullName: String,
    email: String,
    password: String,
    rememberMe: Boolean
  ): Result<UserProfileEntity> = withContext(Dispatchers.IO) {
    val cleanEmail = email.trim().lowercase()
    val cleanName = fullName.trim()

    if (cleanName.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Full Name cannot be empty."))
    }
    if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
      return@withContext Result.failure(IllegalArgumentException("Please provide a valid email address."))
    }
    if (password.length < 6) {
      return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
    }

    // Check if email already registered locally
    val existing = userDao.getUserByEmail(cleanEmail)
    if (existing != null) {
      return@withContext Result.failure(IllegalStateException("A local profile with this email already exists on this device."))
    }

    if (rememberMe) {
      userDao.clearAllRememberMe()
    }

    val salt = PasswordHasher.generateSalt()
    val hash = PasswordHasher.hashPassword(password, salt)
    val userId = UUID.randomUUID().toString()

    val newUser = UserProfileEntity(
      userId = userId,
      fullName = cleanName,
      email = cleanEmail,
      passwordHash = hash,
      salt = salt,
      encryptedApiKey = null,
      apiProvider = "GEMINI",
      isApiKeyVerified = false,
      createdAt = System.currentTimeMillis(),
      lastLoginAt = System.currentTimeMillis(),
      rememberMe = rememberMe
    )

    userDao.insertUser(newUser)
    _currentUser.value = newUser
    preferences.setLastActiveUserId(userId)
    seedDefaultShortcutsForUser(userId)

    Result.success(newUser)
  }

  suspend fun login(
    email: String,
    password: String,
    rememberMe: Boolean
  ): Result<UserProfileEntity> = withContext(Dispatchers.IO) {
    val cleanEmail = email.trim().lowercase()

    val user = userDao.getUserByEmail(cleanEmail)
      ?: return@withContext Result.failure(IllegalArgumentException("No local profile found for $cleanEmail on this device."))

    val isPasswordValid = PasswordHasher.verifyPassword(password, user.salt, user.passwordHash)
    if (!isPasswordValid) {
      return@withContext Result.failure(IllegalArgumentException("Incorrect password for local profile."))
    }

    if (rememberMe) {
      userDao.clearAllRememberMe()
    }

    val updatedUser = user.copy(
      rememberMe = rememberMe,
      lastLoginAt = System.currentTimeMillis()
    )
    userDao.updateUser(updatedUser)

    _currentUser.value = updatedUser
    preferences.setLastActiveUserId(updatedUser.userId)
    seedDefaultShortcutsForUser(updatedUser.userId)

    Result.success(updatedUser)
  }

  suspend fun logout() = withContext(Dispatchers.IO) {
    val current = _currentUser.value
    if (current != null) {
      userDao.updateUser(current.copy(rememberMe = false))
    }
    _currentUser.value = null
    preferences.setLastActiveUserId(null)
  }

  suspend fun switchUser(userId: String): Result<UserProfileEntity> = withContext(Dispatchers.IO) {
    val user = userDao.getUserById(userId)
      ?: return@withContext Result.failure(IllegalArgumentException("Profile not found."))

    userDao.clearAllRememberMe()
    val updated = user.copy(lastLoginAt = System.currentTimeMillis(), rememberMe = true)
    userDao.updateUser(updated)

    _currentUser.value = updated
    preferences.setLastActiveUserId(userId)
    seedDefaultShortcutsForUser(userId)
    Result.success(updated)
  }

  fun getAllProfiles(): Flow<List<UserProfileEntity>> = userDao.getAllUsers()

  suspend fun deleteProfile(userId: String) = withContext(Dispatchers.IO) {
    // 1. Delete all isolated records for this profile
    chatDao.clearAllMessages(userId)
    chatDao.clearAllSessions(userId)
    memoryDao.clearAllMemories(userId)
    userDao.deleteUser(userId)
    preferences.deleteUserData(userId)

    if (_currentUser.value?.userId == userId) {
      _currentUser.value = null
      preferences.setLastActiveUserId(null)
    }
  }

  // --- Personal API Key (BYOK) Management ---
  suspend fun verifyGeminiApiKey(apiKey: String): Result<String> {
    return apiClient.validateGeminiApiKey(apiKey)
  }

  suspend fun saveUserApiKey(userId: String, rawApiKey: String, isVerified: Boolean) = withContext(Dispatchers.IO) {
    val encrypted = JarvisKeyStoreManager.encrypt(rawApiKey.trim())
    val user = userDao.getUserById(userId) ?: return@withContext
    val updated = user.copy(
      encryptedApiKey = encrypted,
      isApiKeyVerified = isVerified
    )
    userDao.updateUser(updated)
    if (_currentUser.value?.userId == userId) {
      _currentUser.value = updated
    }
  }

  suspend fun removeUserApiKey(userId: String) = withContext(Dispatchers.IO) {
    val user = userDao.getUserById(userId) ?: return@withContext
    val updated = user.copy(
      encryptedApiKey = null,
      isApiKeyVerified = false
    )
    userDao.updateUser(updated)
    if (_currentUser.value?.userId == userId) {
      _currentUser.value = updated
    }
  }

  suspend fun getUserDecryptedApiKey(userId: String): String? = withContext(Dispatchers.IO) {
    val user = userDao.getUserById(userId) ?: return@withContext null
    val encrypted = user.encryptedApiKey ?: return@withContext null
    val decrypted = JarvisKeyStoreManager.decrypt(encrypted)
    decrypted.ifBlank { null }
  }

  // --- Chat & Sessions (User Isolated) ---
  fun getMessagesForSession(sessionId: String, userId: String): Flow<List<ChatMessageEntity>> =
    chatDao.getMessagesForSession(sessionId, userId)

  fun getAllSessions(userId: String): Flow<List<ChatSessionEntity>> =
    chatDao.getAllSessions(userId)

  suspend fun createNewSession(initialTitle: String = "JARVIS Protocol", userId: String): String = withContext(Dispatchers.IO) {
    val newSessionId = UUID.randomUUID().toString()
    val session = ChatSessionEntity(
      sessionId = newSessionId,
      userId = userId,
      title = initialTitle,
      lastMessage = "Session initialized.",
      updatedAt = System.currentTimeMillis(),
      messageCount = 0
    )
    chatDao.insertOrUpdateSession(session)
    newSessionId
  }

  suspend fun sendMessage(
    sessionId: String,
    userId: String,
    userText: String,
    languageHint: String,
    imageUri: String? = null,
    imageBase64: String? = null
  ): ChatMessageEntity = withContext(Dispatchers.IO) {
    // 1. Record User Message
    val userMsg = ChatMessageEntity(
      userId = userId,
      sessionId = sessionId,
      sender = "user",
      content = userText,
      timestamp = System.currentTimeMillis(),
      language = languageHint,
      imageUri = imageUri
    )
    chatDao.insertMessage(userMsg)

    // 2. Fetch active memories for this user only
    val activeMemories = if (preferences.settings.value.isMemoryOptIn) {
      memoryDao.getActiveMemoriesList(userId).map { "${it.key}: ${it.value}" }
    } else {
      emptyList()
    }
    val memoryContext = if (activeMemories.isNotEmpty()) {
      activeMemories.joinToString("; ")
    } else null

    // 3. Retrieve user's decrypted personal API key (Keystore)
    val personalApiKey = getUserDecryptedApiKey(userId) ?: ""

    // 4. Query API
    val currentSettings = preferences.settings.value
    val (aiText, latency) = apiClient.sendQuery(
      baseUrl = currentSettings.apiBaseUrl,
      apiKey = personalApiKey,
      prompt = userText,
      language = if (languageHint == "auto") {
        if (com.example.data.remote.AutonomousJarvisEngine.isBanglaText(userText)) "bn" else "en"
      } else languageHint,
      memoryContext = memoryContext,
      imageBase64 = imageBase64,
      activeMemories = activeMemories
    )

    // 5. Save AI response
    val aiMsg = ChatMessageEntity(
      userId = userId,
      sessionId = sessionId,
      sender = "jarvis",
      content = aiText,
      timestamp = System.currentTimeMillis(),
      language = if (com.example.data.remote.AutonomousJarvisEngine.isBanglaText(aiText)) "bn" else "en",
      latencyMs = latency
    )
    chatDao.insertMessage(aiMsg)

    // 6. Update session summary
    val sessionTitle = if (userText.length > 28) userText.take(28) + "..." else userText
    val updatedSession = ChatSessionEntity(
      sessionId = sessionId,
      userId = userId,
      title = sessionTitle,
      lastMessage = if (aiText.length > 40) aiText.take(40) + "..." else aiText,
      updatedAt = System.currentTimeMillis(),
      messageCount = 2
    )
    chatDao.insertOrUpdateSession(updatedSession)

    aiMsg
  }

  suspend fun deleteSession(sessionId: String, userId: String) = withContext(Dispatchers.IO) {
    chatDao.deleteSession(sessionId, userId)
    chatDao.deleteMessagesForSession(sessionId, userId)
  }

  suspend fun clearAllHistory(userId: String) = withContext(Dispatchers.IO) {
    chatDao.clearAllSessions(userId)
    chatDao.clearAllMessages(userId)
  }

  // --- Memory Manager (User Isolated) ---
  fun getAllMemories(userId: String): Flow<List<MemoryEntity>> = memoryDao.getAllMemories(userId)

  suspend fun saveMemory(category: String, key: String, value: String, userId: String, isEnabled: Boolean = true) =
    withContext(Dispatchers.IO) {
      val memory = MemoryEntity(
        userId = userId,
        category = category.ifBlank { "Preference" },
        key = key.trim(),
        value = value.trim(),
        isEnabled = isEnabled,
        updatedAt = System.currentTimeMillis()
      )
      memoryDao.insertOrUpdate(memory)
    }

  suspend fun updateMemory(memory: MemoryEntity) = withContext(Dispatchers.IO) {
    memoryDao.insertOrUpdate(memory)
  }

  suspend fun deleteMemory(id: Long, userId: String) = withContext(Dispatchers.IO) {
    memoryDao.deleteMemory(id, userId)
  }

  suspend fun clearAllMemories(userId: String) = withContext(Dispatchers.IO) {
    memoryDao.clearAllMemories(userId)
  }

  // --- Voice Shortcuts (User Isolated) ---
  suspend fun seedDefaultShortcutsForUser(userId: String) = withContext(Dispatchers.IO) {
    if (shortcutDao.getCount(userId) == 0) {
      val userShortcuts = VoiceShortcutMatcher.DEFAULT_SHORTCUTS.map {
        it.copy(id = "${userId}_${it.id}", userId = userId)
      }
      shortcutDao.insertAll(userShortcuts)
    }
  }

  fun getAllVoiceShortcuts(userId: String): Flow<List<VoiceShortcutEntity>> =
    shortcutDao.getAllShortcuts(userId)

  suspend fun updateVoiceShortcut(shortcut: VoiceShortcutEntity) = withContext(Dispatchers.IO) {
    shortcutDao.update(shortcut)
  }

  suspend fun resetDefaultShortcuts(userId: String) = withContext(Dispatchers.IO) {
    val userShortcuts = VoiceShortcutMatcher.DEFAULT_SHORTCUTS.map {
      it.copy(id = "${userId}_${it.id}", userId = userId)
    }
    shortcutDao.insertAll(userShortcuts)
  }

  suspend fun matchVoiceCommand(spokenInput: String, userId: String): VoiceShortcutEntity? = withContext(Dispatchers.IO) {
    val enabledShortcuts = shortcutDao.getEnabledShortcuts(userId)
    VoiceShortcutMatcher.matchCommand(spokenInput, enabledShortcuts)
  }

  // --- Export User Data ---
  suspend fun exportUserData(userId: String): String = withContext(Dispatchers.IO) {
    val user = userDao.getUserById(userId)
    val sessions = chatDao.getAllSessionsListForUser(userId)
    val messages = chatDao.getAllMessagesForUser(userId)
    val memories = memoryDao.getAllMemoriesList(userId)

    val root = JSONObject()
    root.put("exportTimestamp", System.currentTimeMillis())
    root.put("profileName", user?.fullName ?: "JARVIS User")
    root.put("profileEmail", user?.email ?: "")
    root.put("hasConfiguredApiKey", user?.encryptedApiKey != null)

    val sessionsArray = JSONArray()
    sessions.forEach { s ->
      val sObj = JSONObject()
        .put("sessionId", s.sessionId)
        .put("title", s.title)
        .put("updatedAt", s.updatedAt)
      sessionsArray.put(sObj)
    }
    root.put("sessions", sessionsArray)

    val messagesArray = JSONArray()
    messages.forEach { m ->
      val mObj = JSONObject()
        .put("sessionId", m.sessionId)
        .put("sender", m.sender)
        .put("content", m.content)
        .put("timestamp", m.timestamp)
        .put("language", m.language)
      messagesArray.put(mObj)
    }
    root.put("messages", messagesArray)

    val memoriesArray = JSONArray()
    memories.forEach { mem ->
      val memObj = JSONObject()
        .put("category", mem.category)
        .put("key", mem.key)
        .put("value", mem.value)
        .put("isEnabled", mem.isEnabled)
      memoriesArray.put(memObj)
    }
    root.put("memories", memoriesArray)

    root.toString(2)
  }

  suspend fun testEndpointConnection(): Result<Long> {
    val settings = preferences.settings.value
    val userId = _currentUser.value?.userId ?: ""
    val personalKey = if (userId.isNotBlank()) getUserDecryptedApiKey(userId) ?: "" else ""
    return apiClient.testEndpoint(settings.apiBaseUrl, personalKey)
  }
}
