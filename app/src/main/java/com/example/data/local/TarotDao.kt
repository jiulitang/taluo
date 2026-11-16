package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MentorPersonaEntity
import com.example.data.model.ReadingRecordEntity
import com.example.data.model.TarotCardEntity
import com.example.data.model.WorldbookEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TarotCardDao {
  @Query("SELECT * FROM tarot_cards ORDER BY id ASC")
  fun observeAllCards(): Flow<List<TarotCardEntity>>

  @Query("SELECT * FROM tarot_cards ORDER BY id ASC")
  suspend fun getAllCardsOnce(): List<TarotCardEntity>

  @Query("SELECT * FROM tarot_cards WHERE id = :id LIMIT 1")
  suspend fun getCardById(id: Int): TarotCardEntity?

  @Query("SELECT COUNT(*) FROM tarot_cards")
  suspend fun getCardCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(cards: List<TarotCardEntity>)

  @Update
  suspend fun updateCard(card: TarotCardEntity)
}

@Dao
interface MentorPersonaDao {
  @Query("SELECT * FROM mentor_personas ORDER BY isActive DESC, isDefault DESC, updatedAt DESC")
  fun observeAllPersonas(): Flow<List<MentorPersonaEntity>>

  @Query("SELECT * FROM mentor_personas WHERE isActive = 1 LIMIT 1")
  fun observeActivePersona(): Flow<MentorPersonaEntity?>

  @Query("SELECT * FROM mentor_personas WHERE isActive = 1 LIMIT 1")
  suspend fun getActivePersonaOnce(): MentorPersonaEntity?

  @Query("SELECT COUNT(*) FROM mentor_personas")
  suspend fun getPersonaCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(personas: List<MentorPersonaEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPersona(persona: MentorPersonaEntity): Long

  @Update
  suspend fun updatePersona(persona: MentorPersonaEntity)

  @Query("UPDATE mentor_personas SET isActive = 0")
  suspend fun clearActiveFlags()

  @Query("UPDATE mentor_personas SET isActive = 1 WHERE id = :personaId")
  suspend fun setActiveFlag(personaId: Long)

  @Query("DELETE FROM mentor_personas WHERE id = :personaId")
  suspend fun deletePersonaById(personaId: Long)
}

@Dao
interface WorldbookDao {
  @Query("SELECT * FROM worldbook_entries ORDER BY isAlwaysActive DESC, updatedAt DESC")
  fun observeAllEntries(): Flow<List<WorldbookEntryEntity>>

  @Query("SELECT * FROM worldbook_entries WHERE isEnabled = 1")
  suspend fun getEnabledEntriesOnce(): List<WorldbookEntryEntity>

  @Query("SELECT COUNT(*) FROM worldbook_entries")
  suspend fun getEntryCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(entries: List<WorldbookEntryEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEntry(entry: WorldbookEntryEntity): Long

  @Update
  suspend fun updateEntry(entry: WorldbookEntryEntity)

  @Query("DELETE FROM worldbook_entries WHERE id = :id")
  suspend fun deleteEntryById(id: Long)
}

@Dao
interface ReadingRecordDao {
  @Query("SELECT * FROM reading_records ORDER BY createdAt DESC")
  fun observeAllReadings(): Flow<List<ReadingRecordEntity>>

  @Query("SELECT * FROM reading_records WHERE isDailyDraw = 1 AND dateKey = :dateKey ORDER BY createdAt DESC LIMIT 1")
  fun observeDailyReading(dateKey: String): Flow<ReadingRecordEntity?>

  @Query("SELECT * FROM reading_records ORDER BY createdAt DESC LIMIT 1")
  suspend fun getLatestReadingOnce(): ReadingRecordEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReading(record: ReadingRecordEntity): Long

  @Update
  suspend fun updateReading(record: ReadingRecordEntity)

  @Query("DELETE FROM reading_records WHERE id = :id")
  suspend fun deleteReadingById(id: Long)
}

@Dao
interface ChatMessageDao {
  @Query("SELECT * FROM chat_messages WHERE personaId = :personaId ORDER BY timestamp ASC")
  fun observeMessagesForPersona(personaId: Long): Flow<List<ChatMessageEntity>>

  @Query("SELECT * FROM chat_messages WHERE personaId = :personaId ORDER BY timestamp ASC")
  suspend fun getMessagesForPersonaOnce(personaId: Long): List<ChatMessageEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: ChatMessageEntity): Long

  @Query("DELETE FROM chat_messages WHERE personaId = :personaId")
  suspend fun clearMessagesForPersona(personaId: Long)
}
