package com.swasthyasathi.app.ui.components

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel
import com.swasthyasathi.app.wearable.WearableConnectionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoWearableDialog(
    viewModel: HealthViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val telemetry by viewModel.wearableTelemetry.collectAsState()
    val status by viewModel.watchConnectionStatus.collectAsState()
    val isDemoMode by viewModel.isDemoWearableMode.collectAsState()

    var selectedTab by remember { mutableIntStateOf(if (isDemoMode) 1 else 0) }

    var heartRate by remember { mutableFloatStateOf(if (telemetry.heartRate > 0) telemetry.heartRate.toFloat() else 78f) }
    var spo2 by remember { mutableFloatStateOf(if (telemetry.spo2 > 0) telemetry.spo2.toFloat() else 98f) }
    var bodyTemp by remember { mutableFloatStateOf(if (telemetry.bodyTemperature > 0f) telemetry.bodyTemperature else 36.8f) }
    var isFall by remember { mutableStateOf(telemetry.fallDetected) }

    // Activity Result Launcher for BLE permissions
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
            Toast.makeText(context, "Bluetooth permissions are required for watch sync", Toast.LENGTH_SHORT).show()
        }
    }

    val requestConnect = {
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Watch, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text(
                                text = "SwasthyaSathi Watch & Devices",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text(
                                text = "ESP32-S3 BLE Central Gateway",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppOutline
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AppOutline)
                    }
                }

                // Tab Selector: Live Hardware Watch vs Judge Demo Simulator
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceContainerLowest,
                    contentColor = PrimaryTeal,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            viewModel.setDemoWearableMode(false)
                        },
                        text = {
                            Text(
                                "Live Watch (BLE)",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) EmeraldPrimary else OnSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            viewModel.setDemoWearableMode(true)
                        },
                        text = {
                            Text(
                                "Judge Simulator",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) PrimaryTeal else OnSurfaceVariant
                            )
                        }
                    )
                }

                HorizontalDivider(color = SurfaceContainer)

                if (selectedTab == 0) {
                    // TAB 0: Live BLE Hardware Management
                    val isConnected = status == WearableConnectionStatus.CONNECTED

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceContainerLow,
                        border = BorderStroke(1.dp, SurfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Target Device:", style = MaterialTheme.typography.bodySmall, color = AppOutline)
                                Text("SWASTHYASATHI WATCH", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Connection Status:", style = MaterialTheme.typography.bodySmall, color = AppOutline)
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = if (isConnected) EmeraldContainer else SurfaceContainerHigh
                                ) {
                                    Text(
                                        text = status.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isConnected) EmeraldPrimary else OnSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Watch Battery:", style = MaterialTheme.typography.bodySmall, color = AppOutline)
                                Text(
                                    text = if (telemetry.batteryLevel > 0) "${telemetry.batteryLevel}% ${telemetry.batteryVoltage?.let { "($it V)" } ?: ""}" else "--",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Watch GPS Satellites:", style = MaterialTheme.typography.bodySmall, color = AppOutline)
                                Text(
                                    text = if (telemetry.gps) "${telemetry.satellites} Satellites locked" else "No GPS lock",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (telemetry.gps) EmeraldPrimary else AppOutline
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Last Synced:", style = MaterialTheme.typography.bodySmall, color = AppOutline)
                                Text(
                                    text = if (telemetry.lastSyncFormatted.isNotBlank()) telemetry.lastSyncFormatted else if (isConnected) "Active stream" else "--",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppOutline
                                )
                            }
                        }
                    }

                    // Live Diagnostics Section (For debug testing)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainerLowest,
                        border = BorderStroke(1.dp, SurfaceContainerHigh)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("BLE Diagnostics:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AppOutline)
                            Text("Service UUID: 7c8e0001-... [Found]", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = if (isConnected) EmeraldPrimary else AppOutline)
                            Text("Telemetry Characteristic: 7c8e0003-... [Notifications Active]", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = if (isConnected) EmeraldPrimary else AppOutline)
                            Text("Command Characteristic: 7c8e0004-... [Write Ready]", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = if (isConnected) EmeraldPrimary else AppOutline)
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isConnected) {
                            OutlinedButton(
                                onClick = {
                                    val ok = viewModel.sendWatchCommand("PING")
                                    Toast.makeText(context, if (ok) "Ping sent to watch" else "Ping failed", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Ping")
                            }

                            OutlinedButton(
                                onClick = {
                                    val ok = viewModel.sendWatchCommand("GET_STATUS")
                                    Toast.makeText(context, if (ok) "Status requested" else "Command failed", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Status")
                            }

                            Button(
                                onClick = { viewModel.disconnectWatch() },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Disconnect", color = SecondaryCoral, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = requestConnect,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Connect Watch (Start BLE Scan)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                } else {
                    // TAB 1: Judge Demo Simulator
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PrimaryTealFixed.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "SIMULATOR MODE: Drag sliders to demonstrate real-time risk alerts and SOS queueing for hackathon evaluation without requiring the physical watch nearby.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurface,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    // 1. Heart Rate Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Heart Rate (BPM):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text("${heartRate.toInt()} BPM", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (heartRate > 110) SecondaryCoral else OnSurface)
                        }
                        Slider(
                            value = heartRate,
                            onValueChange = {
                                heartRate = it
                                viewModel.updateWearableTelemetry { t -> t.copy(heartRate = it.toInt()) }
                            },
                            valueRange = 50f..170f,
                            colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                        )
                    }

                    // 2. SpO2 Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SpO2 Blood Oxygen (%):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text("${spo2.toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (spo2 < 94) SecondaryCoral else OnSurface)
                        }
                        Slider(
                            value = spo2,
                            onValueChange = {
                                spo2 = it
                                viewModel.updateWearableTelemetry { t -> t.copy(spo2 = it.toInt()) }
                            },
                            valueRange = 85f..100f,
                            colors = SliderDefaults.colors(thumbColor = PrimaryTeal, activeTrackColor = PrimaryTeal)
                        )
                    }

                    // 3. Body Temperature Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Body Temperature (°C):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text("${String.format("%.1f", bodyTemp)}°C", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (bodyTemp > 38.0) SecondaryCoral else OnSurface)
                        }
                        Slider(
                            value = bodyTemp,
                            onValueChange = {
                                bodyTemp = it
                                viewModel.updateWearableTelemetry { t -> t.copy(bodyTemperature = it) }
                            },
                            valueRange = 35.0f..41.0f,
                            colors = SliderDefaults.colors(thumbColor = SecondaryCoral, activeTrackColor = SecondaryCoral)
                        )
                    }

                    // 4. Fall Detection Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Simulate Fall Event", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(if (isFall) "🚨 FALL DETECTED!" else "Normal posture", style = MaterialTheme.typography.bodySmall, color = if (isFall) SecondaryCoral else AppOutline)
                        }
                        Switch(
                            checked = isFall,
                            onCheckedChange = {
                                isFall = it
                                viewModel.updateWearableTelemetry { t -> t.copy(fallDetected = it, heartRate = if (it) 128 else 78) }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = SecondaryCoral)
                        )
                    }

                    // Quick Presets
                    Text("Preset Scenarios for Judges:", style = MaterialTheme.typography.labelMedium, color = AppOutline)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                heartRate = 75f
                                spo2 = 98f
                                bodyTemp = 36.8f
                                isFall = false
                                viewModel.updateWearableTelemetry { t -> t.copy(heartRate = 75, spo2 = 98, bodyTemperature = 36.8f, fallDetected = false) }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("Normal Baseline", fontSize = 11.sp, color = OnSurface)
                        }

                        Button(
                            onClick = {
                                heartRate = 125f
                                spo2 = 92f
                                bodyTemp = 39.1f
                                isFall = true
                                viewModel.updateWearableTelemetry { t -> t.copy(heartRate = 125, spo2 = 92, bodyTemperature = 39.1f, fallDetected = true) }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = RiskCriticalBg),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("Heat Emergency", fontSize = 11.sp, color = SecondaryCoral, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
