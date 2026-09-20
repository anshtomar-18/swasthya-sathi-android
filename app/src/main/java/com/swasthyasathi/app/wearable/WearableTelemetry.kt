package com.swasthyasathi.app.wearable

import com.google.gson.annotations.SerializedName

enum class WearableConnectionStatus(val label: String) {
    DISCONNECTED("Disconnected"),
    SCANNING("Scanning..."),
    CONNECTING("Connecting..."),
    CONNECTED("Watch Connected"),
    RECONNECTING("Reconnecting..."),
    ERROR("Connection Error")
}

data class WearableTelemetry(
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("heartRate") val heartRate: Int = 78,
    @SerializedName("spo2") val spo2: Int = 98,
    @SerializedName("bodyTemperature") val bodyTemperature: Float = 36.8f,
    @SerializedName("ambientTemperature") val ambientTemperature: Float = 34.0f,
    @SerializedName("humidity") val humidity: Int = 55,
    @SerializedName("latitude") val latitude: Double = 28.6139,
    @SerializedName("longitude") val longitude: Double = 77.2090,
    @SerializedName("steps") val steps: Int = 4250,
    @SerializedName("activityLevel") val activityLevel: String = "moderate", // low, moderate, high
    @SerializedName("fallDetected") val fallDetected: Boolean = false,
    @SerializedName("batteryLevel") val batteryLevel: Int = 88,
    @SerializedName("connectionStatus") val connectionStatus: WearableConnectionStatus = WearableConnectionStatus.CONNECTED,
    @SerializedName("deviceName") val deviceName: String = "SwasthyaWatch Ultra (BLE)"
) {
    val isAnomaly: Boolean
        get() = heartRate > 115 || spo2 < 94 || bodyTemperature > 38.0f || fallDetected

    val anomalySummary: String
        get() = when {
            fallDetected -> "CRITICAL: Fall Detected!"
            heartRate > 120 -> "Elevated Heart Rate (${heartRate} BPM)"
            spo2 < 94 -> "Low SpO2 Saturation (${spo2}%)"
            bodyTemperature > 38.0f -> "Elevated Body Temperature (${bodyTemperature}°C)"
            else -> "Normal Vitals Baseline"
        }
}
