package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MentorPersonaEntity
import com.example.data.model.ReadingRecordEntity
import com.example.data.model.TarotCardEntity
import com.example.data.model.WorldbookEntryEntity

@Database(
  entities =
    [
      TarotCardEntity::class,
      MentorPersonaEntity::class,
      WorldbookEntryEntity::class,
      ReadingRecordEntity::class,
      ChatMessageEntity::class,
    ],
  version = 1,
  exportSchema = false,
)
abstract class TarotDatabase : RoomDatabase() {
  abstract fun tarotCardDao(): TarotCardDao

  abstract fun mentorPersonaDao(): MentorPersonaDao

  abstract fun worldbookDao(): WorldbookDao

  abstract fun readingRecordDao(): ReadingRecordDao

  abstract fun chatMessageDao(): ChatMessageDao

  companion object {
    @Volatile private var INSTANCE: TarotDatabase? = null

    fun getInstance(context: Context): TarotDatabase {
      return INSTANCE
        ?: synchronized(this) {
          INSTANCE
            ?: Room.databaseBuilder(
                context.applicationContext,
                TarotDatabase::class.java,
                "aurelia_tarot_sanctuary.db",
              )
              .fallbackToDestructiveMigration(dropAllTables = true)
              .build()
              .also { INSTANCE = it }
        }
    }
  }
}
