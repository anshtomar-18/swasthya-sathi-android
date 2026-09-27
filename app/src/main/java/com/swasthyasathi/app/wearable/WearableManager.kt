package com.swasthyasathi.app.wearable

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WearableManager(context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    val bleManager = BleManager(context)

    // Demo Mode toggle: defaults to false so the app is in live hardware BLE mode by default
    private val _isDemoMode = MutableStateFlow(false)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    // Unified Telemetry StateFlow
    private val _telemetryState = MutableStateFlow(
        WearableTelemetry(
            heartRate = 0,
            spo2 = 0,
            bodyTemperature = 0.0f,
            ambientTemperature = 0.0f,
            humidity = 0,
            steps = 0,
            activityLevel = "resting",
            fallDetected = false,
            batteryLevel = 0,
            connectionStatus = WearableConnectionStatus.DISCONNECTED,
            deviceName = BleManager.TARGET_DEVICE_NAME,
            isRealWatchData = false
        )
    )
    val telemetryState: StateFlow<WearableTelemetry> = _telemetryState.asStateFlow()

    val connectionStatus: StateFlow<WearableConnectionStatus> = bleManager.connectionState
    val sosEvents: SharedFlow<WatchPacket> = bleManager.sosEvents
    val lastErrorMessage: StateFlow<String?> = bleManager.lastErrorMessage

    init {
        // Collect live BLE telemetry from physical watch when not in manual demo simulation mode
        scope.launch {
            bleManager.telemetry.collect { liveTelemetry ->
                if (!_isDemoMode.value) {
                    _telemetryState.value = liveTelemetry
                }
            }
        }

        // Keep connection status synchronized
        scope.launch {
            bleManager.connectionState.collect { status ->
                if (!_isDemoMode.value) {
                    _telemetryState.value = _telemetryState.value.copy(connectionStatus = status)
                }
            }
        }
    }

    fun connectWatch() {
        _isDemoMode.value = false
        bleManager.startScan()
    }

    fun disconnectWatch() {
        bleManager.disconnect()
    }

    fun stopScan() {
        bleManager.stopScan()
    }

    fun sendCommand(command: String): Boolean {
        return bleManager.sendCommand(command)
    }

    fun isBluetoothEnabled(): Boolean = bleManager.isBluetoothEnabled()

    fun hasRequiredPermissions(): Boolean = bleManager.hasRequiredPermissions()

    fun setDemoMode(active: Boolean) {
        _isDemoMode.value = active
        if (active) {
            // Provide baseline simulator values for judge demonstration
            if (_telemetryState.value.heartRate == 0) {
                _telemetryState.value = WearableTelemetry(
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
                    deviceName = "SwasthyaWatch (Demo Simulator)",
                    isRealWatchData = false
                )
            }
        } else {
            // Return to live BLE state
            _telemetryState.value = bleManager.telemetry.value
        }
    }

    fun updateTelemetry(builder: (WearableTelemetry) -> WearableTelemetry) {
        val updated = builder(_telemetryState.value)
        _telemetryState.value = updated
        bleManager.updateSimulatedTelemetry(updated)
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
