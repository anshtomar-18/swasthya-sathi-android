package com.swasthyasathi.app.wearable

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WearableManager(context: Context) {

    val bleManager = BleManager(context)

    private val _telemetryState = MutableStateFlow(
        WearableTelemetry(
            heartRate = 78,
            spo2 = 98,
            bodyTemperature = 36.8f,
            ambientTemperature = 34.0f,
            humidity = 58,
            steps = 4120,
            activityLevel = "moderate",
            fallDetected = false,
            batteryLevel = 92,
            connectionStatus = WearableConnectionStatus.CONNECTED,
            deviceName = "SwasthyaWatch Pro (BLE/Simulated)"
        )
    )
    val telemetryState: StateFlow<WearableTelemetry> = _telemetryState.asStateFlow()

    private val _isDemoMode = MutableStateFlow(true)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    fun updateTelemetry(builder: (WearableTelemetry) -> WearableTelemetry) {
        val updated = builder(_telemetryState.value)
        _telemetryState.value = updated
        bleManager.updateSimulatedTelemetry(updated)
    }

    fun setDemoMode(active: Boolean) {
        _isDemoMode.value = active
    }

    fun setConnectionStatus(status: WearableConnectionStatus) {
        updateTelemetry { it.copy(connectionStatus = status) }
    }

    fun triggerSimulatedFall() {
        updateTelemetry { it.copy(fallDetected = true, heartRate = 128) }
    }

    fun resetFall() {
        updateTelemetry { it.copy(fallDetected = false) }
    }
}
