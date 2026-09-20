package com.swasthyasathi.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    val telemetry by viewModel.wearableTelemetry.collectAsState()

    var heartRate by remember { mutableFloatStateOf(telemetry.heartRate.toFloat()) }
    var spo2 by remember { mutableFloatStateOf(telemetry.spo2.toFloat()) }
    var bodyTemp by remember { mutableFloatStateOf(telemetry.bodyTemperature) }
    var ambientTemp by remember { mutableFloatStateOf(telemetry.ambientTemperature) }
    var isFall by remember { mutableStateOf(telemetry.fallDetected) }
    var isConnected by remember { mutableStateOf(telemetry.connectionStatus == WearableConnectionStatus.CONNECTED) }

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
                                text = "Demo Wearable Sensors",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text(
                                text = "SIH Judge Sensor Simulator",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppOutline
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AppOutline)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "SIMULATED SENSOR DATA: Adjust sliders below to demonstrate real-time AI risk alerts, health notifications, and emergency SOS queueing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                HorizontalDivider(color = SurfaceContainer)

                // 1. Connection Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Watch Connection State", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(if (isConnected) "🟢 Watch Connected" else "🔴 Watch Disconnected", style = MaterialTheme.typography.bodySmall, color = AppOutline)
                    }
                    Switch(
                        checked = isConnected,
                        onCheckedChange = {
                            isConnected = it
                            val status = if (it) WearableConnectionStatus.CONNECTED else WearableConnectionStatus.DISCONNECTED
                            viewModel.updateWearableTelemetry { t -> t.copy(connectionStatus = status) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                    )
                }

                // 2. Heart Rate Slider
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

                // 3. SpO2 Slider
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

                // 4. Body Temperature Slider
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

                // 5. Fall Detection Toggle
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

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply & Close Simulator", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
