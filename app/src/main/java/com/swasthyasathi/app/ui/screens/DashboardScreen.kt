package com.swasthyasathi.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.data.engine.RiskEngine
import com.swasthyasathi.app.data.model.DEFAULT_INDIAN_CITIES
import com.swasthyasathi.app.data.model.RiskLevel
import com.swasthyasathi.app.ui.components.DisasterCard
import com.swasthyasathi.app.ui.components.RiskCard
import com.swasthyasathi.app.ui.components.TelemetryCard
import com.swasthyasathi.app.ui.components.VitalsCard
import com.swasthyasathi.app.ui.components.bounceClick
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: HealthViewModel,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val city by viewModel.currentCity.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val riskResult by viewModel.riskResult.collectAsState()
    val warnings by viewModel.disasterWarnings.collectAsState()
    val isOffline by viewModel.isOfflineMode.collectAsState()
    val isSimulatorActive by viewModel.isSimulatorActive.collectAsState()
    val simulatedTemp by viewModel.simulatedTemp.collectAsState()
    val simulatedAqi by viewModel.simulatedAqi.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val haptic = LocalHapticFeedback.current
    var showCityDropdown by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppSurface,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setChatOpen(true) },
                containerColor = PrimaryTeal,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Health Companion")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Top Telemetry & Location Bar
            Surface(
                color = SurfaceContainerLowest,
                border = BorderStroke(1.dp, SurfaceContainer),
                shadowElevation = 2.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Location Dropdown Trigger
                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceContainerLow,
                                border = BorderStroke(1.dp, SurfaceContainer),
                                modifier = Modifier.clickable { showCityDropdown = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "${city.name}, ${city.state}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = OnSurface
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AppOutline, modifier = Modifier.size(18.dp))
                                }
                            }

                            DropdownMenu(
                                expanded = showCityDropdown,
                                onDismissRequest = { showCityDropdown = false }
                            ) {
                                DEFAULT_INDIAN_CITIES.forEach { c ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(c.name, fontWeight = FontWeight.SemiBold)
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(c.state, color = AppOutline, style = MaterialTheme.typography.bodySmall)
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectCity(c)
                                            showCityDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Right Controls: Offline Toggle + Simulator Toggle + SOS Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // 60 FPS Simulator Lab Toggle
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSimulatorActive) PrimaryTealFixed.copy(alpha = 0.5f) else SurfaceContainerLow,
                                border = BorderStroke(1.dp, if (isSimulatorActive) PrimaryTeal else SurfaceContainer),
                                modifier = Modifier.bounceClick {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleSimulatorMode()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Sim",
                                        tint = if (isSimulatorActive) PrimaryTeal else AppOutline,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = if (isSimulatorActive) "Sim ON" else "Sim",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSimulatorActive) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSimulatorActive) PrimaryTeal else AppOutline
                                    )
                                }
                            }

                            // Offline demo icon toggle
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isOffline) RiskModerateBg else SurfaceContainerLow,
                                border = BorderStroke(1.dp, if (isOffline) RiskModerate else SurfaceContainer),
                                modifier = Modifier.bounceClick {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.toggleOfflineMode()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isOffline) Icons.Default.CloudOff else Icons.Default.CloudDone,
                                        contentDescription = "Offline Demo",
                                        tint = if (isOffline) RiskModerate else PrimaryTeal,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = if (isOffline) "Offline" else "Live",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isOffline) RiskModerate else PrimaryTeal
                                    )
                                }
                            }

                            // Emergency SOS Button
                            Button(
                                onClick = { viewModel.setSosOpen(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryCoral),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "SOS",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }

                    // Low Connectivity Banner
                    AnimatedVisibility(visible = isOffline) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CloudOff, contentDescription = null, tint = RiskModerate, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Simulated Low-Connectivity: On-device RiskEngine evaluates local thresholds with zero network dependency.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Scrollable Dashboard Body
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive 60 FPS Climate Simulator Panel (Smooth scrub & instant recalculation)
                AnimatedVisibility(
                    visible = isSimulatorActive,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        border = BorderStroke(1.5.dp, PrimaryTeal)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "60 FPS CLIMATE STRESS SIMULATOR",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryTeal
                                    )
                                }
                                Text(
                                    text = "Real-time Scrub",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppOutline
                                )
                            }

                            // Temperature Slider
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Simulated Ambient Temp", style = MaterialTheme.typography.bodySmall, color = OnSurface)
                                    Text(
                                        text = "${simulatedTemp.toInt()}°C",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (simulatedTemp >= 40) SecondaryCoral else PrimaryTeal
                                    )
                                }
                                Slider(
                                    value = simulatedTemp,
                                    onValueChange = { newTemp ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.updateSimulatorValues(newTemp, simulatedAqi)
                                    },
                                    valueRange = 20f..52f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = if (simulatedTemp >= 40) SecondaryCoral else PrimaryTeal,
                                        activeTrackColor = if (simulatedTemp >= 40) SecondaryCoral else PrimaryTeal
                                    )
                                )
                            }

                            // AQI Slider
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Simulated Particulate AQI", style = MaterialTheme.typography.bodySmall, color = OnSurface)
                                    Text(
                                        text = "${simulatedAqi.toInt()} AQI",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (simulatedAqi >= 300) RiskCritical else if (simulatedAqi >= 150) SecondaryCoral else PrimaryTeal
                                    )
                                }
                                Slider(
                                    value = simulatedAqi,
                                    onValueChange = { newAqi ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.updateSimulatorValues(simulatedTemp, newAqi)
                                    },
                                    valueRange = 25f..500f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = if (simulatedAqi >= 300) RiskCritical else PrimaryTeal,
                                        activeTrackColor = if (simulatedAqi >= 300) RiskCritical else PrimaryTeal
                                    )
                                )
                            }

                            // Quick Presets Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Triple("☀️ Heatwave", 46f, 180f),
                                    Triple("🏭 Smog 450", 34f, 450f),
                                    Triple("⚡ Crisis", 48f, 480f),
                                    Triple("🟢 Clean", 28f, 55f)
                                ).forEach { (label, t, a) ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SurfaceContainerLow,
                                        border = BorderStroke(1.dp, SurfaceContainer),
                                        modifier = Modifier
                                            .weight(1f)
                                            .bounceClick {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.setSimulatorPreset(t, a)
                                            }
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = OnSurface,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Active Profile Context Strip
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceContainerLowest,
                    border = BorderStroke(1.dp, SurfaceContainer),
                    modifier = Modifier.bounceClick {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateToProfile()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(18.dp))
                            Column {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = profile.firstName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = OnSurface
                                    )
                                    Text(
                                        text = "(${profile.ageGroup} • ${profile.outdoorActivityLevel} exertion)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppOutline
                                    )
                                }
                                if (profile.sensitivities.isNotEmpty()) {
                                    Text(
                                        text = "Sensitivities: ${profile.sensitivities.joinToString(", ")}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PrimaryTeal
                                    )
                                }
                            }
                        }

                        TextButton(onClick = onNavigateToProfile) {
                            Text("Edit", color = PrimaryTeal, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Headline Risk Assessment Card
                RiskCard(result = riskResult, isOffline = isOffline)

                // Interactive Symptom Triage Selector Chips (Tap to launch AI Companion)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INTERACTIVE SYMPTOM TRIAGE",
                            style = MaterialTheme.typography.labelLarge,
                            color = AppOutline
                        )
                        Text(
                            text = "Tap to Ask AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryTeal
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val symptoms = listOf(
                            Pair("🧠 Headache / Migraine", "I am feeling a throbbing headache and lightheadedness in this hot outdoor weather."),
                            Pair("🫁 Chest Wheezing", "I am experiencing shortness of breath and wheezing due to high PM2.5 smog."),
                            Pair("❤️ Heart Palpitations", "My heart rate feels unusually rapid and heavy under heat stress."),
                            Pair("💧 Heat Cramps & Fatigue", "I feel muscle cramps, intense dehydration, and exhaustion from heat."),
                            Pair("👁️ Burning Airway & Eyes", "My eyes and respiratory airway feel burned and irritated by toxic pollutants.")
                        )

                        symptoms.forEach { (label, prompt) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceContainerLowest,
                                border = BorderStroke(1.2.dp, PrimaryTealFixed),
                                modifier = Modifier
                                    .bounceClick {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setChatOpen(true)
                                        viewModel.sendChatMessage(prompt)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = OnSurface
                                    )
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Live Atmospheric Telemetry Card
                TelemetryCard(telemetry = telemetry)

                // Active Disaster Warnings Card
                DisasterCard(warnings = warnings, locationName = city.name)

                // Physiological Sensor Vitals Card (with live running ECG Canvas)
                VitalsCard(riskLevel = riskResult.riskLevel)

                // 12-Hour Predictive Timeline
                if (telemetry.hourly.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "12-HOUR PREDICTIVE IMPACT TIMELINE",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = "Hourly personal risk trajectory computed for your profile",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppOutline
                                )
                            }
                            Text(
                                text = "Diurnal Curve",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppOutline
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            telemetry.hourly.take(6).forEach { point ->
                                val pointRisk = RiskEngine.computeRisk(
                                    temperatureC = point.temperatureC.toDouble(),
                                    feelsLikeC = point.feelsLikeC.toDouble(),
                                    humidityPct = point.humidityPct.toDouble(),
                                    aqi = point.aqi.toDouble(),
                                    profile = profile
                                )

                                val tagColor = when (pointRisk.riskLevel) {
                                    RiskLevel.Critical -> RiskCritical
                                    RiskLevel.High -> RiskHigh
                                    RiskLevel.Moderate -> RiskModerate
                                    RiskLevel.Low -> PrimaryTeal
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = SurfaceContainerLowest,
                                    border = BorderStroke(1.dp, SurfaceContainer),
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = point.hourLabel, style = MaterialTheme.typography.labelMedium, color = AppOutline)
                                        Text(text = "${point.temperatureC}°", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                                        Text(text = "AQI ${point.aqi}", style = MaterialTheme.typography.labelMedium, color = AppOutline)
                                        Text(
                                            text = pointRisk.level.uppercase(),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontSize = 9.sp,
                                            color = tagColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Edge Engine Buffer Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    border = BorderStroke(1.dp, SurfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EDGE AUTONOMOUS ENGINE",
                                style = MaterialTheme.typography.labelLarge,
                                color = AppOutline
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PrimaryTealFixed.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PrimaryTeal,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Zero Telemetry Leakage Guarantee",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                        Text(
                            text = "Deterministic clinical evaluation executes natively inside your mobile client. Private local caching with AES-GCM-256 caregiver encryption.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        HorizontalDivider(color = SurfaceContainer)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Designated Caregiver:", style = MaterialTheme.typography.bodySmall, color = AppOutline)
                            Text(
                                text = "${profile.emergencyContactName} (${profile.emergencyContactPhone})",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
