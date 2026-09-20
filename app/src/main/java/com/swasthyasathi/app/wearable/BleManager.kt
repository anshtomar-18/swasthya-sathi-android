package com.swasthyasathi.app.wearable

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter
    }

    private val _connectionState = MutableStateFlow(WearableConnectionStatus.DISCONNECTED)
    val connectionState: StateFlow<WearableConnectionStatus> = _connectionState.asStateFlow()

    private val _telemetry = MutableStateFlow(WearableTelemetry())
    val telemetry: StateFlow<WearableTelemetry> = _telemetry.asStateFlow()

    private var scanner: BluetoothLeScanner? = null
    private var isScanning = false

    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun startScan(onDeviceFound: (name: String, address: String) -> Unit = { _, _ -> }) {
        if (!isBluetoothEnabled()) {
            _connectionState.value = WearableConnectionStatus.ERROR
            return
        }
        _connectionState.value = WearableConnectionStatus.SCANNING
        isScanning = true

        try {
            scanner = bluetoothAdapter?.bluetoothLeScanner
            scanner?.startScan(object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    result?.device?.let { dev ->
                        val name = dev.name ?: "SwasthyaWatch Sensor"
                        onDeviceFound(name, dev.address)
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    _connectionState.value = WearableConnectionStatus.ERROR
                    isScanning = false
                }
            })
        } catch (e: Exception) {
            _connectionState.value = WearableConnectionStatus.ERROR
            isScanning = false
        }
    }

    fun stopScan() {
        if (isScanning) {
            try {
                scanner?.stopScan(object : ScanCallback() {})
            } catch (e: Exception) {
                // Ignore scanner stop error
            }
            isScanning = false
        }
    }

    fun updateSimulatedTelemetry(newTelemetry: WearableTelemetry) {
        _telemetry.value = newTelemetry
        _connectionState.value = newTelemetry.connectionStatus
    }
}
