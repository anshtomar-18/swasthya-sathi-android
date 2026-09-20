package com.swasthyasathi.app.sos

enum class SOSState(val label: String) {
    NORMAL("Normal Safety Operational"),
    EMERGENCY_DETECTED("🚨 Emergency Detected!"),
    SOS_QUEUED("🟡 SOS Queued Offline"),
    WAITING_FOR_CONNECTION("⏳ Waiting for Reconnection"),
    SOS_TRANSMITTING("📡 Transmitting SOS Packet..."),
    SOS_SENT("🟢 SOS Transmitted Successfully"),
    SOS_FAILED("🔴 SOS Transmission Failed")
}

data class SOSEvent(
    val eventId: String = "sos_${System.currentTimeMillis()}",
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "user_default",
    val locationName: String = "New Delhi, Delhi NCR",
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val heartRate: Int = 0,
    val spo2: Int = 0,
    val bodyTemperature: Float = 0.0f,
    val ambientTemperature: Float = 0.0f,
    val riskLevel: String = "High",
    val detectedCondition: String = "Manual Distress Siren Triggered",
    val emergencyContactName: String = "Rajesh Sharma",
    val emergencyContactPhone: String = "+91 98765 43210",
    val isTransmitted: Boolean = false,
    val queuedOffline: Boolean = false,
    val transmissionStatus: String = "SOS_QUEUED"
)
