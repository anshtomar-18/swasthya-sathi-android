package com.swasthyasathi.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.data.model.RiskLevel
import com.swasthyasathi.app.ui.theme.*

@Composable
fun VitalsCard(
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val heartRate = when (riskLevel) {
        RiskLevel.Critical -> 88
        RiskLevel.High -> 82
        RiskLevel.Moderate -> 74
        RiskLevel.Low -> 68
    }

    val thermalStress = when (riskLevel) {
        RiskLevel.Critical -> 74
        RiskLevel.High -> 61
        RiskLevel.Moderate -> 45
        RiskLevel.Low -> 32
    }

    // 60 FPS Ventricular Pulse Animation on the Heart Icon
    val heartBeatTransition = rememberInfiniteTransition(label = "heartBeatTransition")
    val heartScale by heartBeatTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = (60_000 / heartRate.coerceAtLeast(60))
                1f at 0
                1.22f at 120 using FastOutSlowInEasing
                1.05f at 220 using LinearOutSlowInEasing
                1.25f at 340 using FastOutSlowInEasing
                1f at 500 using LinearOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "heartScale"
    )

    // Smooth Spring Animated Progress for Thermal Stress
    val animatedThermalProgress by animateFloatAsState(
        targetValue = thermalStress / 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "thermalProgress"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Vitals Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PHYSIOLOGICAL SENSOR VITALS",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                Text(
                    text = "Live telemetry cross-referenced with thermal workload",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppOutline
                )
            }

            Surface(
                shape = RoundedCornerShape(100.dp),
                color = PrimaryTealFixed.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "Band Pro X9: 60Hz BLE",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryTeal,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // 2x2 Grid of Vitals Cards with spring bounce and haptics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Heart Rate with running ECG & pulsing heart
            Card(
                modifier = Modifier
                    .weight(1f)
                    .bounceClick {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HEART RATE", style = MaterialTheme.typography.labelMedium, color = AppOutline)
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = SecondaryCoral,
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer {
                                    scaleX = heartScale
                                    scaleY = heartScale
                                }
                        )
                    }
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("$heartRate", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                        Text("BPM", style = MaterialTheme.typography.labelMedium, color = AppOutline, modifier = Modifier.padding(bottom = 3.dp))
                    }
                    EcgWaveform(bpm = heartRate, lineColor = PrimaryTeal, glowColor = PrimaryTealFixed)
                }
            }

            // Autonomic HRV
            Card(
                modifier = Modifier
                    .weight(1f)
                    .bounceClick {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AUTONOMIC HRV", style = MaterialTheme.typography.labelMedium, color = AppOutline)
                        Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                    }
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("64", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                        Text("ms", style = MaterialTheme.typography.labelMedium, color = AppOutline, modifier = Modifier.padding(bottom = 3.dp))
                    }
                    LinearProgressIndicator(
                        progress = { 0.64f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = PrimaryTeal,
                        trackColor = SurfaceContainerLow
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Blood Oxygen SpO2
            Card(
                modifier = Modifier
                    .weight(1f)
                    .bounceClick {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("BLOOD OXYGEN", style = MaterialTheme.typography.labelMedium, color = AppOutline)
                        Icon(Icons.Default.Opacity, contentDescription = null, tint = TertiaryNavy, modifier = Modifier.size(16.dp))
                    }
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("98", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                        Text("% SpO2", style = MaterialTheme.typography.labelMedium, color = AppOutline, modifier = Modifier.padding(bottom = 3.dp))
                    }
                    LinearProgressIndicator(
                        progress = { 0.98f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = TertiaryNavy,
                        trackColor = SurfaceContainerLow
                    )
                }
            }

            // Thermal Stress Index with dynamic spring progress
            Card(
                modifier = Modifier
                    .weight(1f)
                    .bounceClick {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("THERMAL STRESS", style = MaterialTheme.typography.labelMedium, color = AppOutline)
                        Icon(Icons.Default.Speed, contentDescription = null, tint = SecondaryCoralContainer, modifier = Modifier.size(16.dp))
                    }
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("$thermalStress", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                        Text("/ 100", style = MaterialTheme.typography.labelMedium, color = AppOutline, modifier = Modifier.padding(bottom = 3.dp))
                    }
                    LinearProgressIndicator(
                        progress = { animatedThermalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = if (thermalStress >= 65) SecondaryCoral else SecondaryCoralContainer,
                        trackColor = SurfaceContainerLow
                    )
                }
            }
        }
    }
}
