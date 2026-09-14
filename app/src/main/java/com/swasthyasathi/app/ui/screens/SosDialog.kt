package com.swasthyasathi.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.swasthyasathi.app.ui.components.bounceClick
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel

@Composable
fun SosDialog(
    viewModel: HealthViewModel,
    onDismiss: () -> Unit
) {
    val status by viewModel.sosStatus.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val city by viewModel.currentCity.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val risk by viewModel.riskResult.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            Text(
                                text = "Emergency SOS Protocol",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text(
                                text = "SIH26181 Rapid Distress Simulation",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppOutline
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AppOutline)
                    }
                }

                HorizontalDivider(color = SurfaceContainer)

                when (status) {
                    "idle" -> {
                        Text(
                            text = "Triggering SOS simulates dispatching an encrypted telemetry packet containing your GPS coordinates, local weather hazards, and risk classification to your caregiver.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        // Telemetry Preview Box
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = RiskCriticalBg,
                            border = BorderStroke(1.dp, Color(0xFFFECACA))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PreviewRow("GPS Coordinates:", "${city.lat}° N, ${city.lon}° E")
                                PreviewRow("Detected City:", "${city.name}, ${city.state}")
                                PreviewRow("Environmental Stress:", "${telemetry.temperatureC}°C Heat • AQI ${telemetry.aqi}")
                                PreviewRow("Personal Risk Tier:", risk.level.uppercase(), isBold = true, valueColor = SecondaryCoral)
                                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
                                PreviewRow("Designated Contact:", "${profile.emergencyContactName} (${profile.emergencyContactPhone})")
                            }
                        }

                        // Honest Simulation Notice
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceContainerLow
                        ) {
                            Text(
                                text = "Hackathon MVP Notice: This is a simulated demonstration. In accordance with the SIH prompt, no actual emergency services or cellular carriers will be billed or called during Round 1.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppOutline,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(10.dp),
                                lineHeight = 16.sp
                            )
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = onDismiss
                            ) {
                                Text("Cancel", color = AppOutline)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.triggerSos() },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryCoral),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Confirm & Broadcast SOS", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    "broadcasting" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = SecondaryCoral,
                                strokeWidth = 4.dp
                            )
                            Text(
                                text = "Encrypting & Dispatching Telemetry Packet...",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text(
                                text = "Broadcasting coordinates to ${profile.emergencyContactName}...",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppOutline
                            )
                        }
                    }

                    "sent" -> {
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
                                text = "Simulated Distress Packet Broadcasted!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )

                            Text(
                                text = "Caregiver ${profile.emergencyContactName} (${profile.emergencyContactPhone}) received simulated packet with coordinates ${city.lat}° N, ${city.lon}° E and ${risk.level} Risk alert.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                lineHeight = 18.sp
                            )

                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Close Distress Protocol", fontWeight = FontWeight.Bold)
                            }
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
