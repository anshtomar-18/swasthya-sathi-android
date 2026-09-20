package com.swasthyasathi.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SOSQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSosEvent(event: SOSQueueEntity)

    @Query("SELECT * FROM sos_queue WHERE isTransmitted = 0 ORDER BY timestamp DESC")
    suspend fun getPendingSosEvents(): List<SOSQueueEntity>

    @Query("SELECT * FROM sos_queue ORDER BY timestamp DESC")
    fun getAllSosEvents(): Flow<List<SOSQueueEntity>>

    @Query("UPDATE sos_queue SET isTransmitted = 1, transmissionStatus = 'SOS_SENT' WHERE eventId = :eventId")
    suspend fun markAsSent(eventId: String)

    @Query("DELETE FROM sos_queue WHERE eventId = :eventId")
    suspend fun deleteSosEvent(eventId: String)
}

@Dao
interface AISettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AISettingsEntity)

    @Query("SELECT * FROM ai_settings WHERE id = 1")
    suspend fun getSettings(): AISettingsEntity?
}
