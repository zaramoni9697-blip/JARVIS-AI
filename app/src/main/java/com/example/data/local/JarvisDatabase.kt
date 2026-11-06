package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    UserProfileEntity::class,
    ChatMessageEntity::class,
    ChatSessionEntity::class,
    MemoryEntity::class,
    VoiceShortcutEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
  abstract fun chatDao(): ChatDao
  abstract fun memoryDao(): MemoryDao
  abstract fun voiceShortcutDao(): VoiceShortcutDao

  companion object {
    @Volatile
    private var INSTANCE: JarvisDatabase? = null

    fun getDatabase(context: Context): JarvisDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          JarvisDatabase::class.java,
          "jarvis_core.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
