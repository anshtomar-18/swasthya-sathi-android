package com.swasthyasathi.app.ui.components

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel
import com.swasthyasathi.app.wearable.WearableConnectionStatus

/**
 * Native Material 3 Wearable Companion Card
 * Polished interface to pair, monitor, and interact with the ESP32-S3 SwasthyaSathi Watch.
 */
@Composable
fun WatchConnectionCard(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val telemetry by viewModel.wearableTelemetry.collectAsState()
    val status by viewModel.watchConnectionStatus.collectAsState()
    val errorMsg by viewModel.watchErrorMessage.collectAsState()

    // Activity Result Launchers for Permissions and Bluetooth
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            if (!viewModel.isBluetoothEnabled()) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                context.startActivity(enableBtIntent)
            } else {
                viewModel.connectWatch()
            }
        } else {
            Toast.makeText(context, "Bluetooth permissions are required to sync your watch", Toast.LENGTH_SHORT).show()
        }
    }

    val requestConnection = {
        if (!viewModel.hasRequiredBlePermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                )
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        } else if (!viewModel.isBluetoothEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            context.startActivity(enableBtIntent)
        } else {
            viewModel.connectWatch()
        }
    }

    val isConnected = status == WearableConnectionStatus.CONNECTED
    val isBusy = status == WearableConnectionStatus.SCANNING ||
            status == WearableConnectionStatus.CONNECTING ||
            status == WearableConnectionStatus.DISCOVERING_SERVICES ||
            status == WearableConnectionStatus.RECONNECTING

    // Pulsing indicator for active connection
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        border = BorderStroke(
            1.2.dp,
            if (isConnected) EmeraldPrimary.copy(alpha = 0.6f) else SurfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Watch Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isConnected) EmeraldContainer else SurfaceContainerHigh,
                        border = BorderStroke(1.dp, if (isConnected) EmeraldPrimary else AppOutline.copy(alpha = 0.4f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Watch,
                                contentDescription = null,
                                tint = if (isConnected) EmeraldPrimary else OnSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "SwasthyaSathi Watch",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                        Text(
                            text = "ESP32-S3 BLE Companion",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppOutline
                        )
                    }
                }

                // Connection State Pill Badge
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = when (status) {
                        WearableConnectionStatus.CONNECTED -> EmeraldContainer
                        WearableConnectionStatus.SCANNING,
                        WearableConnectionStatus.CONNECTING,
                        WearableConnectionStatus.DISCOVERING_SERVICES,
                        WearableConnectionStatus.RECONNECTING -> SurfaceContainerHigh
                        WearableConnectionStatus.NOT_FOUND,
                        WearableConnectionStatus.ERROR -> RiskCriticalBg
                        WearableConnectionStatus.DISCONNECTED -> SurfaceContainerLow
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .scale(if (isConnected || isBusy) pulseScale else 1f)
                                .background(
                                    when (status) {
                                        WearableConnectionStatus.CONNECTED -> EmeraldPrimary
                                        WearableConnectionStatus.SCANNING,
                                        WearableConnectionStatus.CONNECTING,
                                        WearableConnectionStatus.DISCOVERING_SERVICES,
                                        WearableConnectionStatus.RECONNECTING -> PrimaryTeal
                                        WearableConnectionStatus.NOT_FOUND,
                                        WearableConnectionStatus.ERROR -> SecondaryCoral
                                        WearableConnectionStatus.DISCONNECTED -> AppOutline
                                    }
                                )
                        )

                        Text(
                            text = when (status) {
                                WearableConnectionStatus.CONNECTED -> "Connected"
                                WearableConnectionStatus.SCANNING -> "Scanning..."
                                WearableConnectionStatus.CONNECTING -> "Connecting..."
                                WearableConnectionStatus.DISCOVERING_SERVICES -> "Syncing GATT..."
                                WearableConnectionStatus.RECONNECTING -> "Reconnecting..."
                                WearableConnectionStatus.NOT_FOUND -> "Not Found"
                                WearableConnectionStatus.ERROR -> "Error"
                                WearableConnectionStatus.DISCONNECTED -> "Not Connected"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = when (status) {
                                WearableConnectionStatus.CONNECTED -> EmeraldPrimary
                                WearableConnectionStatus.SCANNING,
                                WearableConnectionStatus.CONNECTING,
                                WearableConnectionStatus.DISCOVERING_SERVICES,
                                WearableConnectionStatus.RECONNECTING -> PrimaryTeal
                                WearableConnectionStatus.NOT_FOUND,
                                WearableConnectionStatus.ERROR -> SecondaryCoral
                                WearableConnectionStatus.DISCONNECTED -> OnSurfaceVariant
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = SurfaceContainer)

            // Content Body based on connection status
            when (status) {
                WearableConnectionStatus.CONNECTED -> {
                    // Connected: Live Metric Grid
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Heart Rate Tile
                            MetricTile(
                                label = "HEART RATE",
                                value = if (telemetry.heartRate > 0) "${telemetry.heartRate}" else "--",
                                unit = "BPM",
                                icon = Icons.Default.Favorite,
                                iconColor = SecondaryCoral,
                                modifier = Modifier.weight(1f)
                            )

                            // SpO2 Tile with Prototype Disclaimer
                            MetricTile(
                                label = "SpO₂ ESTIMATE*",
                                value = if (telemetry.spo2 > 0) "${telemetry.spo2}" else "--",
                                unit = "%",
                                icon = Icons.Default.Opacity,
                                iconColor = PrimaryTeal,
                                note = "*Prototype sensor reading",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Watch Ambient Temperature Tile
                            MetricTile(
                                label = "WATCH TEMP",
                                value = if (telemetry.ambientTemperature > 0f) String.format("%.1f", telemetry.ambientTemperature) else "--",
                                unit = "°C",
                                icon = Icons.Default.Thermostat,
                                iconColor = RiskModerate,
                                modifier = Modifier.weight(1f)
                            )

                            // Watch Humidity Tile
                            MetricTile(
                                label = "WATCH HUMIDITY",
                                value = if (telemetry.humidity > 0) "${telemetry.humidity}" else "--",
                                unit = "%",
                                icon = Icons.Default.WaterDrop,
                                iconColor = PrimaryTeal,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Watch Battery Tile
                            MetricTile(
                                label = "WATCH BATTERY",
                                value = if (telemetry.batteryLevel > 0) "${telemetry.batteryLevel}" else "--",
                                unit = "%",
                                icon = Icons.Default.BatteryChargingFull,
                                iconColor = if (telemetry.batteryLevel < 20) SecondaryCoral else EmeraldPrimary,
                                note = telemetry.batteryVoltage?.let { String.format("%.2fV", it) },
                                modifier = Modifier.weight(1f)
                            )

                            // Watch GPS Tile
                            MetricTile(
                                label = "WATCH GPS",
                                value = if (telemetry.gps) "Fix Locked" else "Searching",
                                unit = if (telemetry.gps) "(${telemetry.satellites} Sats)" else "",
                                icon = Icons.Default.LocationOn,
                                iconColor = if (telemetry.gps) EmeraldPrimary else AppOutline,
                                note = if (telemetry.gps && telemetry.latitude != 0.0) String.format("%.3f, %.3f", telemetry.latitude, telemetry.longitude) else "No Satellite Lock",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Last Sync Info Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (telemetry.lastSyncFormatted.isNotBlank()) "Last packet: ${telemetry.lastSyncFormatted}" else "Streaming live notifications",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppOutline
                            )

                            Text(
                                text = if (telemetry.wifi) "Watch Wi-Fi: Active" else "Watch Wi-Fi: Standby",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppOutline
                            )
                        }

                        // Connected Actions Row: Ping, Status & Disconnect
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val sent = viewModel.sendWatchCommand("PING")
                                    Toast.makeText(context, if (sent) "Ping sent to watch" else "Failed to send ping", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ping", style = MaterialTheme.typography.labelMedium)
                            }

                            OutlinedButton(
                                onClick = {
                                    val sent = viewModel.sendWatchCommand("GET_STATUS")
                                    Toast.makeText(context, if (sent) "Status request sent" else "Failed to send command", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Status", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = { viewModel.disconnectWatch() },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("Disconnect", color = SecondaryCoral, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                WearableConnectionStatus.SCANNING,
                WearableConnectionStatus.CONNECTING,
                WearableConnectionStatus.DISCOVERING_SERVICES,
                WearableConnectionStatus.RECONNECTING -> {
                    // Busy Scanning / Connecting State
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryTeal,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )

                        Text(
                            text = when (status) {
                                WearableConnectionStatus.SCANNING -> "Scanning for 'SWASTHYASATHI WATCH'..."
                                WearableConnectionStatus.CONNECTING -> "Found watch! Establishing GATT channel..."
                                WearableConnectionStatus.DISCOVERING_SERVICES -> "Negotiating MTU 512 & configuring sensors..."
                                WearableConnectionStatus.RECONNECTING -> "Watch link lost. Reconnecting with backoff..."
                                else -> "Connecting..."
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = OnSurface
                        )

                        Text(
                            text = "Keep your watch powered on and within Bluetooth range.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppOutline
                        )

                        OutlinedButton(
                            onClick = { viewModel.disconnectWatch() },
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text("Cancel Scan", color = AppOutline)
                        }
                    }
                }

                WearableConnectionStatus.NOT_FOUND,
                WearableConnectionStatus.ERROR -> {
                    // Not Found / Error Troubleshooting View
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = SecondaryCoral, modifier = Modifier.size(18.dp))
                            Text(
                                text = errorMsg ?: "Watch was not detected nearby.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = SecondaryCoral
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Troubleshooting Checklist:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                                Text("• ESP32-S3 Watch is powered ON with battery charged", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                Text("• Bluetooth is switched ON on your Android phone", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                Text("• Watch is advertising service 7c8e0001-... nearby", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }

                        Button(
                            onClick = requestConnection,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Try Again", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                WearableConnectionStatus.DISCONNECTED -> {
                    // Disconnected Default View
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Connect your SwasthyaSathi Watch to receive real-time physiological telemetry, microclimate readings, and physical SOS alerts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Button(
                            onClick = requestConnection,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Connect Watch",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    note: String? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = SurfaceContainerLow,
        border = BorderStroke(1.dp, SurfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                    color = AppOutline
                )
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(15.dp))
            }

            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                if (unit.isNotBlank()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppOutline,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            if (note != null) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = AppOutline,
                    maxLines = 1
                )
            }
        }
    }
}
