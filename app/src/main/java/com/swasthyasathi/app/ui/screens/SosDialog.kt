package com.swasthyasathi.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.CloudOff
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
import com.swasthyasathi.app.sos.SOSState
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel

@Composable
fun SosDialog(
    viewModel: HealthViewModel,
    onDismiss: () -> Unit
) {
    val sosState by viewModel.sosState.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val isDemoSosMode by viewModel.isDemoSosMode.collectAsState()
    val pendingSosCount by viewModel.pendingSosCount.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val city by viewModel.currentCity.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val risk by viewModel.riskResult.collectAsState()
    val wearable by viewModel.wearableTelemetry.collectAsState()

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
                modifier = Modifier.padding(18.dp),
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RiskCriticalBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = SecondaryCoral, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Emergency SOS Protocol",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = if (isDemoSosMode) SurfaceContainerHigh else RiskCriticalBg
                                ) {
                                    Text(
                                        text = if (isDemoSosMode) "DEMO SOS" else "REAL SOS",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = if (isDemoSosMode) PrimaryTeal else SecondaryCoral,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "10–15 Min Offline Queueing & Transmission State Machine",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppOutline,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AppOutline)
                    }
                }

                HorizontalDivider(color = SurfaceContainer)

                when (sosState) {
                    SOSState.NORMAL -> {
                        Text(
                            text = "Triggering SOS generates an encrypted distress payload with GPS coordinates, wearable vitals, and risk level. If offline, the event is queued locally for 10-15 minutes and automatically transmitted upon connection recovery.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        // Telemetry & Vitals Payload Preview Box
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = RiskCriticalBg,
                            border = BorderStroke(1.dp, Color(0xFFFECACA))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                PreviewRow("GPS Location:", "${city.name} (${String.format("%.3f", city.lat)}° N, ${String.format("%.3f", city.lon)}° E)")
                                PreviewRow("Wearable Vitals:", "${wearable.heartRate} BPM • SpO2 ${wearable.spo2}% • ${wearable.bodyTemperature}°C")
                                PreviewRow("Environmental Stress:", "${telemetry.temperatureC}°C Heat • AQI ${telemetry.aqi}")
                                PreviewRow("Personal Risk Tier:", risk.level.uppercase(), isBold = true, valueColor = SecondaryCoral)
                                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
                                PreviewRow("Designated Contact:", "${profile.emergencyContactName} (${profile.emergencyContactPhone})")
                            }
                        }

                        // Network State & Queue Info
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceContainerLow
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.Emergency,
                                        contentDescription = null,
                                        tint = if (isOfflineMode) SecondaryCoral else PrimaryTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isOfflineMode) "Offline Mode (Will Queue to Room DB)" else "Online (Direct Transmission)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = OnSurface
                                    )
                                }
                                Text("Pending: $pendingSosCount", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AppOutline)
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel", color = AppOutline)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.triggerEmergencySOS() },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryCoral),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Confirm & Broadcast SOS", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    SOSState.SOS_QUEUED, SOSState.WAITING_FOR_CONNECTION -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = RiskHighBg,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = SecondaryCoral, modifier = Modifier.size(32.dp))
                                }
                            }

                            Text(
                                text = "🟡 SOS QUEUED — WAITING FOR CONNECTION",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = SecondaryCoral
                            )

                            Text(
                                text = "Internet connection is currently unavailable. Emergency distress packet has been encrypted and persisted to Room Database. Will automatically retry transmission over the configured 10–15 minute monitoring window.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                lineHeight = 18.sp
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SurfaceContainerLow,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    PreviewRow("Emergency Window:", "10-15 Minutes Active Persistence")
                                    PreviewRow("Pending Events in Room DB:", "$pendingSosCount queued event(s)")
                                    PreviewRow("Auto-Sync Engine:", "Android WorkManager Active")
                                }
                            }

                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Keep Monitoring in Background", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    SOSState.SOS_TRANSMITTING, SOSState.EMERGENCY_DETECTED -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = SecondaryCoral,
                                strokeWidth = 4.dp
                            )
                            Text(
                                text = "📡 Transmitting Encrypted Distress Packet...",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text(
                                text = "Broadcasting coordinates & vitals to ${profile.emergencyContactName}...",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppOutline
                            )
                        }
                    }

                    SOSState.SOS_SENT -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(RiskLowBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(32.dp))
                            }

                            Text(
                                text = "🟢 SOS TRANSMITTED SUCCESSFULLY!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )

                            Text(
                                text = "Caregiver ${profile.emergencyContactName} (${profile.emergencyContactPhone}) received telemetry packet containing coordinates ${city.lat}° N, ${city.lon}° E and ${risk.level} Risk alert.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                lineHeight = 18.sp
                            )

                            Button(
                                onClick = {
                                    viewModel.resetSosState()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Close Emergency Protocol", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    SOSState.SOS_FAILED -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("Transmission Error", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SecondaryCoral)
                            Text("Event remains queued in Room DB for retry.", style = MaterialTheme.typography.bodySmall)
                            Button(onClick = onDismiss) { Text("Close") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewRow(label: String, value: String, isBold: Boolean = false, valueColor: Color = OnSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = AppOutline)
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium),
            color = valueColor
        )
    }
}
