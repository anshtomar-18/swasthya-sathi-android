package com.swasthyasathi.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.ui.components.EcgWaveform
import com.swasthyasathi.app.ui.components.bounceClick
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel
import com.swasthyasathi.app.wearable.WearableConnectionStatus

@Composable
fun OverviewScreen(
    viewModel: HealthViewModel,
    onNavigateToShelters: () -> Unit,
    onNavigateToClimate: () -> Unit,
    onNavigateToHydrate: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val wearableTelemetry by viewModel.wearableTelemetry.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()
    val riskResult by viewModel.riskResult.collectAsState()

    // Interactive Checklist Tasks
    var task1Done by remember { mutableStateOf(true) }
    var task2Done by remember { mutableStateOf(false) }
    var task3Done by remember { mutableStateOf(false) }

    val doneCount = listOf(task1Done, task2Done, task3Done).count { it }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppSurface)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. User Greeting & Location
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Good afternoon, ${profile.firstName}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("👋", fontSize = 20.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(Icons.Default.NearMe, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(14.dp))
                    Text(
                        text = "${currentCity.name}, ${currentCity.state}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Text("•", color = AppOutline)
                    Text(
                        text = "LIVE TELEMETRY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryTeal
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onNavigateToProfile() },
                shape = CircleShape,
                color = EmeraldContainer,
                border = BorderStroke(1.5.dp, EmeraldPrimary)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Badge, contentDescription = "Profile", tint = EmeraldPrimary, modifier = Modifier.size(22.dp))
                }
            }
        }

        // 2. Personal Health Advisory Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            border = BorderStroke(1.dp, SurfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = RiskModerateBg
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = RiskModerate, modifier = Modifier.size(18.dp))
                            }
                        }
                        Text("Personal Health Advisory", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = OnSurfaceVariant)
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = RiskModerateBg
                    ) {
                        Text(
                            text = "MODERATE CAUTION",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = RiskModerate,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Strain Score & Circular Arc
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Combined Heat & Air Strain", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "62",
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                    color = RiskModerate
                                )
                                Text(" / 100 Index", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
                            }
                            Text("Status: Elevated heat burden for active labor", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        }

                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = RiskModerateBg,
                            border = BorderStroke(3.dp, RiskModerate)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = RiskModerate, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }

                // Plain Language Advice
                Text(
                    text = "High midday heat combined with elevated PM2.5 particles. Plan outdoor tasks before 11:00 AM or after 4:00 PM.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface,
                    lineHeight = 20.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                        Text("Drink 500ml ORS electrolyte water by 11:30 AM to offset salt depletion.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = SecondaryCoral, modifier = Modifier.size(16.dp))
                        Text("Limit unshaded street activity between 12:00 PM and 3:30 PM (Sunburn 8.5 UV).", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }
            }
        }

        // 3. 4 Atmospheric Telemetry Tiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Atmospheric Telemetry", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
            TextButton(onClick = onNavigateToClimate) {
                Text("View 24h Radar >", style = MaterialTheme.typography.labelSmall, color = PrimaryTeal)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Air Temp
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Air Temp", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Icon(Icons.Default.Thermostat, contentDescription = null, tint = RiskHigh, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${telemetry.temperatureC}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                        Text("°C", style = MaterialTheme.typography.titleSmall, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
                    }
                    Text("Feels like ${telemetry.feelsLikeC}°C", style = MaterialTheme.typography.labelSmall, color = SecondaryCoral)
                }
            }

            // AQI
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Air Quality", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Icon(Icons.Default.Air, contentDescription = null, tint = RiskHigh, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${telemetry.aqi}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = RiskHigh)
                        Text(" AQI", style = MaterialTheme.typography.titleSmall, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
                    }
                    Text("POOR • PM2.5", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = RiskHigh)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Humidity
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Humidity", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Icon(Icons.Default.Opacity, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${telemetry.humidityPct}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                        Text("%", style = MaterialTheme.typography.titleSmall, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
                    }
                    Text("Slow sweat cooling", style = MaterialTheme.typography.labelSmall, color = RiskModerate)
                }
            }

            // UV Index
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Solar UV Index", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = RiskCritical, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("8.5", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = RiskCritical)
                        Text(" UVI", style = MaterialTheme.typography.titleSmall, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
                    }
                    Text("VERY HIGH", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = RiskCritical)
                }
            }
        }

        // 4. Personal Biometric Rhythm Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            border = BorderStroke(1.dp, SurfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = RiskLow)
                        Text("Biometric Rhythm", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                    }
                    val isWatchConnected = wearableTelemetry.connectionStatus == WearableConnectionStatus.CONNECTED
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isWatchConnected) EmeraldContainer else SurfaceContainer
                    ) {
                        Text(
                            text = if (isWatchConnected) "BLE CONNECTED" else "DISCONNECTED",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isWatchConnected) EmeraldPrimary else OnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Animated ECG Waveform (Visual Rhythm Representation)
                val isConnected = wearableTelemetry.connectionStatus == WearableConnectionStatus.CONNECTED
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        EcgWaveform(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            lineColor = if (isConnected) EmeraldPrimary else RiskLow,
                            bpm = if (isConnected && wearableTelemetry.heartRate > 0) wearableTelemetry.heartRate else 72
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Heart Rate", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Text(
                                    text = if (isConnected && wearableTelemetry.heartRate > 0) "${wearableTelemetry.heartRate} bpm" else "--",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = if (isConnected) "Watch Live" else "Waiting",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isConnected) EmeraldPrimary else AppOutline
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SpO2 (Est.)", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Text(
                                    text = if (isConnected && wearableTelemetry.spo2 > 0) "${wearableTelemetry.spo2}%" else "--",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = if (isConnected) "*Prototype" else "Waiting",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isConnected) RiskModerate else AppOutline
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onNavigateToHydrate() }
                            ) {
                                Text("Hydration", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Text("65%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryTeal)
                                Text("Drink Soon", style = MaterialTheme.typography.labelSmall, color = PrimaryTeal)
                            }
                        }
                    }
                }
            }
        }

        // 5. Simple Actions for Today (Interactive Checklist)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Simple Actions for Today", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
            Text("$doneCount of 3 Done", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryTeal)
        }

        // Task 1
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { task1Done = !task1Done },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            border = BorderStroke(1.dp, SurfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = SurfaceContainer, modifier = Modifier.size(36.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Vaccines, contentDescription = null, tint = RiskLow, modifier = Modifier.size(18.dp))
                        }
                    }
                    Column {
                        Text(
                            text = "Morning asthma inhaler prophylactic dose",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (task1Done) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (task1Done) OnSurfaceVariant else OnSurface
                        )
                        Text("2 puffs before 9:00 AM outdoor transit", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }
                Checkbox(checked = task1Done, onCheckedChange = { task1Done = it })
            }
        }

        // Task 2
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { task2Done = !task2Done },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            border = BorderStroke(1.dp, SurfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = SurfaceContainer, modifier = Modifier.size(36.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(18.dp))
                        }
                    }
                    Column {
                        Text(
                            text = "Pack 2 bottles of chilled ORS electrolyte water",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (task2Done) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (task2Done) OnSurfaceVariant else OnSurface
                        )
                        Text("Electrolyte balance safeguards cardiac stability", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }
                Checkbox(checked = task2Done, onCheckedChange = { task2Done = it })
            }
        }

        // Task 3
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { task3Done = !task3Done },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            border = BorderStroke(1.dp, SurfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = SurfaceContainer, modifier = Modifier.size(36.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Deck, contentDescription = null, tint = SecondaryCoral, modifier = Modifier.size(18.dp))
                        }
                    }
                    Column {
                        Text(
                            text = "Schedule shaded break: 1:00 - 1:45 PM",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (task3Done) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (task3Done) OnSurfaceVariant else OnSurface
                        )
                        Text("Peak solar radiation window avoidance", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }
                Checkbox(checked = task3Done, onCheckedChange = { task3Done = it })
            }
        }

        // 6. Nearest Cool Shelter Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            border = BorderStroke(1.dp, SurfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = RiskLow)
                        Text("Nearest Climate Relief", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                    }
                    Text("OPEN NOW", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = RiskLow)
                }

                Surface(shape = RoundedCornerShape(12.dp), color = SurfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("NDMC Climate Relief Hub 04", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                                Text("Connaught Place Block-B Inner Circle", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                            Text("320m", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryTeal)
                        }
                        Text("• 4 Misting Fans • Chilled RO Water • First Aid Nurse", style = MaterialTheme.typography.labelSmall, color = PrimaryTeal)
                    }
                }

                Button(
                    onClick = onNavigateToShelters,
                    modifier = Modifier.fillMaxWidth().bounceClick(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Get Direct Shaded Route")
                }
            }
        }
    }
}
