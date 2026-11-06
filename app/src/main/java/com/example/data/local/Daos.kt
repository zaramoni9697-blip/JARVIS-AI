package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertUser(user: UserProfileEntity): Long

  @Update
  suspend fun updateUser(user: UserProfileEntity)

  @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
  suspend fun getUserById(userId: String): UserProfileEntity?

  @Query("SELECT * FROM user_profiles WHERE LOWER(email) = LOWER(:email) LIMIT 1")
  suspend fun getUserByEmail(email: String): UserProfileEntity?

  @Query("SELECT * FROM user_profiles ORDER BY lastLoginAt DESC")
  fun getAllUsers(): Flow<List<UserProfileEntity>>

  @Query("SELECT * FROM user_profiles ORDER BY lastLoginAt DESC")
  suspend fun getAllUsersList(): List<UserProfileEntity>

  @Query("SELECT * FROM user_profiles WHERE rememberMe = 1 ORDER BY lastLoginAt DESC LIMIT 1")
  suspend fun getRememberedUser(): UserProfileEntity?

  @Query("UPDATE user_profiles SET rememberMe = 0")
  suspend fun clearAllRememberMe()

  @Query("DELETE FROM user_profiles WHERE userId = :userId")
  suspend fun deleteUser(userId: String)
}

@Dao
interface ChatDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: ChatMessageEntity): Long

  @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId AND userId = :userId ORDER BY timestamp ASC")
  fun getMessagesForSession(sessionId: String, userId: String): Flow<List<ChatMessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateSession(session: ChatSessionEntity)

  @Query("SELECT * FROM chat_sessions WHERE userId = :userId ORDER BY updatedAt DESC")
  fun getAllSessions(userId: String): Flow<List<ChatSessionEntity>>

  @Query("DELETE FROM chat_sessions WHERE sessionId = :sessionId AND userId = :userId")
  suspend fun deleteSession(sessionId: String, userId: String)

  @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId AND userId = :userId")
  suspend fun deleteMessagesForSession(sessionId: String, userId: String)

  @Query("DELETE FROM chat_sessions WHERE userId = :userId")
  suspend fun clearAllSessions(userId: String)

  @Query("DELETE FROM chat_messages WHERE userId = :userId")
  suspend fun clearAllMessages(userId: String)

  @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
  suspend fun getAllMessagesForUser(userId: String): List<ChatMessageEntity>

  @Query("SELECT * FROM chat_sessions WHERE userId = :userId ORDER BY updatedAt DESC")
  suspend fun getAllSessionsListForUser(userId: String): List<ChatSessionEntity>
}

@Dao
interface MemoryDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(memory: MemoryEntity): Long

  @Query("SELECT * FROM jarvis_memories WHERE userId = :userId ORDER BY updatedAt DESC")
  fun getAllMemories(userId: String): Flow<List<MemoryEntity>>

  @Query("SELECT * FROM jarvis_memories WHERE userId = :userId AND isEnabled = 1")
  suspend fun getActiveMemoriesList(userId: String): List<MemoryEntity>

  @Query("DELETE FROM jarvis_memories WHERE id = :id AND userId = :userId")
  suspend fun deleteMemory(id: Long, userId: String)

  @Query("DELETE FROM jarvis_memories WHERE userId = :userId")
  suspend fun clearAllMemories(userId: String)

  @Query("SELECT * FROM jarvis_memories WHERE userId = :userId ORDER BY updatedAt DESC")
  suspend fun getAllMemoriesList(userId: String): List<MemoryEntity>
}

@Dao
interface VoiceShortcutDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(shortcuts: List<VoiceShortcutEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(shortcut: VoiceShortcutEntity)

  @Query("SELECT * FROM voice_shortcuts WHERE userId = :userId")
  fun getAllShortcuts(userId: String): Flow<List<VoiceShortcutEntity>>

  @Query("SELECT * FROM voice_shortcuts WHERE userId = :userId AND isEnabled = 1")
  suspend fun getEnabledShortcuts(userId: String): List<VoiceShortcutEntity>

  @Query("SELECT COUNT(*) FROM voice_shortcuts WHERE userId = :userId")
  suspend fun getCount(userId: String): Int

  @Update
  suspend fun update(shortcut: VoiceShortcutEntity)
}
