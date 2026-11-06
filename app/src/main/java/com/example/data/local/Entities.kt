package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "user_profiles",
  indices = [Index(value = ["email"], unique = true)]
)
data class UserProfileEntity(
  @PrimaryKey
  val userId: String,
  val fullName: String,
  val email: String,
  val passwordHash: String,
  val salt: String,
  val encryptedApiKey: String? = null,
  val apiProvider: String = "GEMINI", // Initially "GEMINI"
  val isApiKeyVerified: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val lastLoginAt: Long = System.currentTimeMillis(),
  val rememberMe: Boolean = false
)

@Entity(
  tableName = "chat_messages",
  indices = [Index(value = ["userId"]), Index(value = ["sessionId"])]
)
data class ChatMessageEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val userId: String = "default_user",
  val sessionId: String,
  val sender: String, // "user" or "jarvis"
  val content: String,
  val timestamp: Long = System.currentTimeMillis(),
  val language: String = "auto", // "en", "bn", "auto"
  val imageUri: String? = null,
  val latencyMs: Long = 0
)

@Entity(
  tableName = "chat_sessions",
  indices = [Index(value = ["userId"])]
)
data class ChatSessionEntity(
  @PrimaryKey
  val sessionId: String,
  val userId: String = "default_user",
  val title: String,
  val lastMessage: String,
  val updatedAt: Long = System.currentTimeMillis(),
  val messageCount: Int = 0
)

@Entity(
  tableName = "jarvis_memories",
  indices = [Index(value = ["userId"])]
)
data class MemoryEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val userId: String = "default_user",
  val category: String, // "Profile", "Preference", "Fact", "Instruction", "Custom"
  val key: String,
  val value: String,
  val isEnabled: Boolean = true,
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "voice_shortcuts",
  indices = [Index(value = ["userId"])]
)
data class VoiceShortcutEntity(
  @PrimaryKey
  val id: String,
  val userId: String = "default_user",
  val title: String,
  val phrase: String,
  val alternativePhrases: String, // comma separated
  val actionType: String,
  val isEnabled: Boolean = true,
  val description: String = ""
)
