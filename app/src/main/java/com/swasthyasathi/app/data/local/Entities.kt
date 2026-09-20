package com.swasthyasathi.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sos_queue")
data class SOSQueueEntity(
    @PrimaryKey val eventId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "user_default",
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val heartRate: Int = 0,
    val spo2: Int = 0,
    val bodyTemperature: Float = 0.0f,
    val ambientTemperature: Float = 0.0f,
    val riskLevel: String = "High",
    val detectedCondition: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val isTransmitted: Boolean = false,
    val queuedOffline: Boolean = true,
    val transmissionStatus: String = "SOS_QUEUED" // SOS_QUEUED, SOS_TRANSMITTING, SOS_SENT, SOS_FAILED
)

@Entity(tableName = "ai_settings")
data class AISettingsEntity(
    @PrimaryKey val id: Int = 1,
    val languageCode: String = "en", // en, hi, bn
    val isVoiceEnabled: Boolean = true,
    val isAutoLanguageDetection: Boolean = true
)
