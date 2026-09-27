package com.swasthyasathi.app.wearable

import com.google.gson.annotations.SerializedName

enum class WearableConnectionStatus(val label: String) {
    DISCONNECTED("Disconnected"),
    SCANNING("Scanning for watch..."),
    CONNECTING("Connecting..."),
    DISCOVERING_SERVICES("Configuring sensors..."),
    CONNECTED("Watch Connected"),
    RECONNECTING("Reconnecting..."),
    NOT_FOUND("Watch Not Found"),
    ERROR("Connection Error")
}

data class WearableTelemetry(
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("heartRate") val heartRate: Int = 0,
    @SerializedName("spo2") val spo2: Int = 0,
    @SerializedName("bodyTemperature") val bodyTemperature: Float = 0.0f,
    @SerializedName("ambientTemperature") val ambientTemperature: Float = 0.0f,
    @SerializedName("humidity") val humidity: Int = 0,
    @SerializedName("latitude") val latitude: Double = 0.0,
    @SerializedName("longitude") val longitude: Double = 0.0,
    @SerializedName("steps") val steps: Int = 0,
    @SerializedName("activityLevel") val activityLevel: String = "resting",
    @SerializedName("fallDetected") val fallDetected: Boolean = false,
    @SerializedName("batteryLevel") val batteryLevel: Int = 0,
    @SerializedName("batteryVoltage") val batteryVoltage: Double? = null,
    @SerializedName("gps") val gps: Boolean = false,
    @SerializedName("satellites") val satellites: Int = 0,
    @SerializedName("wifi") val wifi: Boolean = false,
    @SerializedName("ble") val ble: Boolean = false,
    @SerializedName("isRealWatchData") val isRealWatchData: Boolean = false,
    @SerializedName("lastSyncFormatted") val lastSyncFormatted: String = "",
    @SerializedName("connectionStatus") val connectionStatus: WearableConnectionStatus = WearableConnectionStatus.DISCONNECTED,
    @SerializedName("deviceName") val deviceName: String = "SWASTHYASATHI WATCH"
) {
    val isAnomaly: Boolean
        get() = (heartRate in 1..49 || heartRate > 115) || (spo2 in 1..93) || bodyTemperature > 38.0f || fallDetected

    val anomalySummary: String
        get() = when {
            fallDetected -> "CRITICAL: Fall Detected!"
            heartRate > 120 -> "Elevated Heart Rate (${heartRate} BPM)"
            spo2 in 1..93 -> "Low SpO2 Saturation (${spo2}%)"
            bodyTemperature > 38.0f -> "Elevated Body Temperature (${bodyTemperature}°C)"
            else -> "Normal Vitals Baseline"
        }
}

/**
 * Raw JSON packet received from ESP32-S3 SwasthyaSathi Watch over BLE characteristic notification
 * Supports both "telemetry" and "sos" types.
 */
data class WatchPacket(
    @SerializedName("type") val type: String? = null,
    @SerializedName("hr") val hr: Int? = null,
    @SerializedName("spo2") val spo2: Int? = null,
    @SerializedName("temperature") val temperature: Double? = null,
    @SerializedName("humidity") val humidity: Double? = null,
    @SerializedName("battery") val battery: Int? = null,
    @SerializedName("batteryVoltage") val batteryVoltage: Double? = null,
    @SerializedName("gps") val gps: Boolean? = null,
    @SerializedName("satellites") val satellites: Int? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("wifi") val wifi: Boolean? = null,
    @SerializedName("ble") val ble: Boolean? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)
