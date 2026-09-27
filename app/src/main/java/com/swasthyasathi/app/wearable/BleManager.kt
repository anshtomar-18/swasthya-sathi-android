package com.swasthyasathi.app.wearable

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Production-ready Bluetooth Low Energy Central / GATT Client
 * Connects the Android phone to the ESP32-S3 "SWASTHYASATHI WATCH".
 */
class BleManager(private val context: Context) {

    companion object {
        private const val TAG = "BleWatchManager"

        // Watch Specifications
        const val TARGET_DEVICE_NAME = "SWASTHYASATHI WATCH"
        val SERVICE_UUID: UUID = UUID.fromString("7c8e0001-6b7a-4a91-9c21-6d7f4a100001")
        val STATUS_CHAR_UUID: UUID = UUID.fromString("7c8e0002-6b7a-4a91-9c21-6d7f4a100002")
        val TELEMETRY_CHAR_UUID: UUID = UUID.fromString("7c8e0003-6b7a-4a91-9c21-6d7f4a100003")
        val COMMAND_CHAR_UUID: UUID = UUID.fromString("7c8e0004-6b7a-4a91-9c21-6d7f4a100004")
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private const val SCAN_TIMEOUT_MS = 15000L
        private const val MAX_RECONNECT_ATTEMPTS = 3
    }

    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val mainHandler = Handler(Looper.getMainLooper())

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter
    }

    // Reactive StateFlows
    private val _connectionState = MutableStateFlow(WearableConnectionStatus.DISCONNECTED)
    val connectionState: StateFlow<WearableConnectionStatus> = _connectionState.asStateFlow()

    private val _telemetry = MutableStateFlow(WearableTelemetry())
    val telemetry: StateFlow<WearableTelemetry> = _telemetry.asStateFlow()

    private val _sosEvents = MutableSharedFlow<WatchPacket>(extraBufferCapacity = 1)
    val sosEvents: SharedFlow<WatchPacket> = _sosEvents.asSharedFlow()

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage.asStateFlow()

    // BLE Internals
    private var scanner: BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null
    private var isScanning = false
    private var scanTimeoutRunnable: Runnable? = null

    private var targetDevice: BluetoothDevice? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var commandCharacteristic: BluetoothGattCharacteristic? = null

    private var reconnectAttempts = 0
    private var isManualDisconnect = false

    // Capability and Permission Queries
    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val scanGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            val connectGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            scanGranted && connectGranted
        } else {
            val btGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
            val adminGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
            val fineLocGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            btGranted && adminGranted && fineLocGranted
        }
    }

    /**
     * Start scanning for "SWASTHYASATHI WATCH"
     */
    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!isBluetoothSupported()) {
            _lastErrorMessage.value = "Bluetooth is not supported on this device"
            _connectionState.value = WearableConnectionStatus.ERROR
            return
        }

        if (!isBluetoothEnabled()) {
            _lastErrorMessage.value = "Bluetooth is currently disabled. Please enable Bluetooth."
            _connectionState.value = WearableConnectionStatus.ERROR
            return
        }

        if (!hasRequiredPermissions()) {
            _lastErrorMessage.value = "Nearby devices / Bluetooth permissions are required."
            _connectionState.value = WearableConnectionStatus.ERROR
            return
        }

        if (isScanning) {
            stopScan()
        }

        isManualDisconnect = false
        reconnectAttempts = 0
        _lastErrorMessage.value = null
        _connectionState.value = WearableConnectionStatus.SCANNING
        Log.d(TAG, "BLE: scan started for $TARGET_DEVICE_NAME")

        scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            _lastErrorMessage.value = "Could not obtain Bluetooth LE scanner"
            _connectionState.value = WearableConnectionStatus.ERROR
            return
        }

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.let { scanResult ->
                    val device = scanResult.device
                    val devName = try { device.name ?: scanResult.scanRecord?.deviceName } catch (e: SecurityException) { null }

                    if (devName != null && devName.contains(TARGET_DEVICE_NAME, ignoreCase = true)) {
                        Log.d(TAG, "BLE: device found $devName (${device.address})")
                        stopScan()
                        connectToDevice(device)
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "BLE: scan failed with error code $errorCode")
                isScanning = false
                _lastErrorMessage.value = "BLE Scan failed (code $errorCode)"
                _connectionState.value = WearableConnectionStatus.ERROR
            }
        }

        try {
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            isScanning = true
            scanner?.startScan(null, settings, scanCallback)

            // Auto-cancel scan after timeout if watch is not found
            scanTimeoutRunnable = Runnable {
                if (isScanning) {
                    Log.w(TAG, "BLE: scan timeout reached. Watch not found.")
                    stopScan()
                    _connectionState.value = WearableConnectionStatus.NOT_FOUND
                    _lastErrorMessage.value = "Watch not found nearby. Make sure it is powered on."
                }
            }
            mainHandler.postDelayed(scanTimeoutRunnable!!, SCAN_TIMEOUT_MS)

        } catch (e: Exception) {
            Log.e(TAG, "BLE: error starting scan", e)
            isScanning = false
            _lastErrorMessage.value = "Error starting scan: ${e.localizedMessage}"
            _connectionState.value = WearableConnectionStatus.ERROR
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (isScanning) {
            scanTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
            try {
                scanCallback?.let { scanner?.stopScan(it) }
            } catch (e: Exception) {
                Log.w(TAG, "BLE: error stopping scan", e)
            }
            isScanning = false
            scanCallback = null
            Log.d(TAG, "BLE: scan stopped")
        }
    }

    /**
     * Connect to the discovered BluetoothDevice
     */
    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        targetDevice = device
        _connectionState.value = WearableConnectionStatus.CONNECTING
        Log.d(TAG, "BLE: connecting to ${device.name ?: TARGET_DEVICE_NAME}...")

        cleanupGatt()

        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, gattCallback)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d(TAG, "BLE: onConnectionStateChange status=$status, newState=$newState")

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "BLE: connected! Requesting MTU 512 for large JSON payloads...")
                reconnectAttempts = 0
                _connectionState.value = WearableConnectionStatus.CONNECTING

                // Request MTU 512 so complete JSON telemetry is never truncated
                val mtuRequested = gatt?.requestMtu(512) == true
                if (!mtuRequested) {
                    // Fallback directly to service discovery if MTU request not supported
                    Log.d(TAG, "BLE: requestMtu failed or immediate fallback; discovering services...")
                    gatt?.discoverServices()
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w(TAG, "BLE: watch disconnected. status=$status")
                commandCharacteristic = null
                cleanupGatt()

                if (isManualDisconnect) {
                    _connectionState.value = WearableConnectionStatus.DISCONNECTED
                    updateTelemetryConnection(WearableConnectionStatus.DISCONNECTED)
                } else if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS && targetDevice != null) {
                    // Limited backoff reconnection
                    reconnectAttempts++
                    val backoffDelay = (reconnectAttempts * 2000L)
                    Log.d(TAG, "BLE: attempting reconnect ($reconnectAttempts/$MAX_RECONNECT_ATTEMPTS) in ${backoffDelay}ms...")
                    _connectionState.value = WearableConnectionStatus.RECONNECTING

                    scope.launch {
                        delay(backoffDelay)
                        targetDevice?.let { dev ->
                            if (!isManualDisconnect) {
                                connectToDevice(dev)
                            }
                        }
                    }
                } else {
                    _connectionState.value = WearableConnectionStatus.DISCONNECTED
                    updateTelemetryConnection(WearableConnectionStatus.DISCONNECTED)
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            Log.d(TAG, "BLE: onMtuChanged mtu=$mtu, status=$status. Discovering services...")
            _connectionState.value = WearableConnectionStatus.DISCOVERING_SERVICES
            gatt?.discoverServices()
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS || gatt == null) {
                Log.e(TAG, "BLE: service discovery failed with status $status")
                _lastErrorMessage.value = "Service discovery failed"
                _connectionState.value = WearableConnectionStatus.ERROR
                return
            }

            Log.d(TAG, "BLE: services discovered successfully")
            val service = gatt.getService(SERVICE_UUID)
            if (service == null) {
                Log.e(TAG, "BLE: required service $SERVICE_UUID not found on watch!")
                _lastErrorMessage.value = "SwasthyaSathi custom service not found"
                _connectionState.value = WearableConnectionStatus.ERROR
                return
            }

            // Command characteristic for writing PING, GET_STATUS, GET_SOS
            commandCharacteristic = service.getCharacteristic(COMMAND_CHAR_UUID)
            if (commandCharacteristic != null) {
                Log.d(TAG, "BLE: command characteristic found")
            } else {
                Log.w(TAG, "BLE: command characteristic $COMMAND_CHAR_UUID not found")
            }

            // Telemetry characteristic for receiving live notifications
            val telemetryChar = service.getCharacteristic(TELEMETRY_CHAR_UUID)
            if (telemetryChar == null) {
                Log.e(TAG, "BLE: telemetry characteristic $TELEMETRY_CHAR_UUID not found!")
                _lastErrorMessage.value = "Telemetry characteristic unavailable"
                _connectionState.value = WearableConnectionStatus.ERROR
                return
            }

            Log.d(TAG, "BLE: telemetry characteristic found. Enabling notifications...")
            val notificationSet = gatt.setCharacteristicNotification(telemetryChar, true)
            if (!notificationSet) {
                Log.e(TAG, "BLE: failed to set characteristic notification locally")
            }

            val descriptor = telemetryChar.getDescriptor(CCCD_UUID)
            if (descriptor != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(descriptor)
                }
                Log.d(TAG, "BLE: CCCD notification descriptor write initiated")
            } else {
                Log.w(TAG, "BLE: CCCD descriptor not found on telemetry characteristic")
            }

            _connectionState.value = WearableConnectionStatus.CONNECTED
            updateTelemetryConnection(WearableConnectionStatus.CONNECTED)
            Log.d(TAG, "BLE: watch successfully connected and configured!")
        }

        // For Android 13+ (API 33+)
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleCharacteristicBytes(characteristic.uuid, value)
        }

        // For Android < 13
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            if (characteristic != null) {
                @Suppress("DEPRECATION")
                val value = characteristic.value
                if (value != null) {
                    handleCharacteristicBytes(characteristic.uuid, value)
                }
            }
        }
    }

    /**
     * Process incoming notification bytes from the watch
     */
    private fun handleCharacteristicBytes(uuid: UUID, value: ByteArray) {
        if (uuid != TELEMETRY_CHAR_UUID) return

        val jsonString = try {
            String(value, Charsets.UTF_8).trim()
        } catch (e: Exception) {
            Log.e(TAG, "BLE: byte decode error", e)
            return
        }

        if (jsonString.isBlank()) return

        try {
            val packet = gson.fromJson(jsonString, WatchPacket::class.java)
            if (packet == null) {
                Log.w(TAG, "BLE: invalid telemetry JSON null payload")
                return
            }

            val packetType = packet.type?.lowercase() ?: "telemetry"

            if (packetType == "sos") {
                Log.w(TAG, "BLE: 🚨 WATCH SOS PACKET RECEIVED: $jsonString")
                scope.launch {
                    _sosEvents.emit(packet)
                }
            }

            // Update live telemetry StateFlow
            val current = _telemetry.value
            val timeFormatted = SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date())

            val hasValidGps = packet.gps == true && packet.latitude != null && packet.longitude != null && packet.latitude != 0.0

            val updated = current.copy(
                timestamp = System.currentTimeMillis(),
                heartRate = packet.hr ?: current.heartRate,
                spo2 = packet.spo2 ?: current.spo2,
                ambientTemperature = (packet.temperature ?: current.ambientTemperature.toDouble()).toFloat(),
                humidity = (packet.humidity ?: current.humidity.toDouble()).toInt(),
                batteryLevel = packet.battery ?: current.batteryLevel,
                batteryVoltage = packet.batteryVoltage ?: current.batteryVoltage,
                gps = packet.gps ?: current.gps,
                satellites = packet.satellites ?: current.satellites,
                latitude = if (hasValidGps) packet.latitude!! else current.latitude,
                longitude = if (hasValidGps) packet.longitude!! else current.longitude,
                wifi = packet.wifi ?: current.wifi,
                ble = packet.ble ?: true,
                isRealWatchData = true,
                lastSyncFormatted = timeFormatted,
                connectionStatus = WearableConnectionStatus.CONNECTED,
                deviceName = TARGET_DEVICE_NAME
            )

            _telemetry.value = updated
            Log.d(TAG, "BLE: telemetry parsed successfully -> HR: ${updated.heartRate}, SpO2: ${updated.spo2}%, Battery: ${updated.batteryLevel}%")

        } catch (e: Exception) {
            // Malformed JSON: log error safely, ignore packet, do NOT crash
            Log.e(TAG, "BLE: invalid telemetry JSON ignored: ${e.localizedMessage}")
        }
    }

    /**
     * Send command to ESP32-S3 Watch (PING, GET_STATUS, GET_SOS)
     */
    @SuppressLint("MissingPermission")
    fun sendCommand(command: String): Boolean {
        val gatt = bluetoothGatt
        val char = commandCharacteristic
        if (gatt == null || char == null) {
            Log.w(TAG, "BLE: cannot send command '$command' - not connected or char missing")
            return false
        }

        return try {
            val bytes = command.toByteArray(Charsets.UTF_8)
            val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val res = gatt.writeCharacteristic(char, bytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                res == BluetoothGatt.GATT_SUCCESS
            } else {
                @Suppress("DEPRECATION")
                char.value = bytes
                @Suppress("DEPRECATION")
                char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                @Suppress("DEPRECATION")
                gatt.writeCharacteristic(char)
            }
            Log.d(TAG, "BLE: sent command '$command', success=$success")
            success
        } catch (e: Exception) {
            Log.e(TAG, "BLE: error sending command '$command'", e)
            false
        }
    }

    /**
     * Disconnect and release GATT resources
     */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        isManualDisconnect = true
        stopScan()
        try {
            bluetoothGatt?.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "BLE: error on disconnect", e)
        }
        cleanupGatt()
        _connectionState.value = WearableConnectionStatus.DISCONNECTED
        updateTelemetryConnection(WearableConnectionStatus.DISCONNECTED)
        Log.d(TAG, "BLE: watch disconnected and cleaned up")
    }

    @SuppressLint("MissingPermission")
    private fun cleanupGatt() {
        try {
            bluetoothGatt?.close()
        } catch (e: Exception) {
            Log.w(TAG, "BLE: error closing GATT", e)
        }
        bluetoothGatt = null
        commandCharacteristic = null
    }

    private fun updateTelemetryConnection(status: WearableConnectionStatus) {
        _telemetry.value = _telemetry.value.copy(connectionStatus = status)
    }

    /**
     * Simulated telemetry updater for testing and offline presentations
     */
    fun updateSimulatedTelemetry(newTelemetry: WearableTelemetry) {
        _telemetry.value = newTelemetry
        _connectionState.value = newTelemetry.connectionStatus
    }
}
