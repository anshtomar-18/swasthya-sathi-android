package com.swasthyasathi.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.ui.components.bounceClick
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel

@Composable
fun ClimateScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()
    val scrollState = rememberScrollState()

    var selectedHorizon by remember { mutableStateOf("today") } // today, curve, advisory
    var isExportingPdf by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppSurface)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Horizon Selector (Today, 24h Curve, 7-Day Alert)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SurfaceContainer
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val horizons = listOf(
                    Triple("today", "Today", Icons.Default.WbSunny),
                    Triple("curve", "24h Curve", Icons.Default.ShowChart),
                    Triple("advisory", "7-Day Alert", Icons.Default.EventNote)
                )

                horizons.forEach { (id, label, icon) ->
                    val isSelected = selectedHorizon == id
                    Button(
                        onClick = { selectedHorizon = id },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) SurfaceContainerLowest else Color.Transparent,
                            contentColor = if (isSelected) PrimaryTeal else OnSurfaceVariant
                        ),
                        elevation = if (isSelected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        )
                    }
                }
            }
        }

        // Primary Atmospheric Hazard Hero Tile
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            border = BorderStroke(1.dp, SurfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = RiskHighBg
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = RiskHigh, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text("BIO-ATMOSPHERIC INDEX", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text("Severe Heat-Air Stagnation", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = RiskHigh)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = RiskCriticalBg
                    ) {
                        Text(
                            text = "TIER-3 ALERT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = RiskCritical,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Core Readouts: Ambient vs Wet-Bulb WBGT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("AMBIENT TEMP", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Icon(Icons.Default.Thermostat, contentDescription = null, tint = RiskHigh, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${telemetry.temperatureC}",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text("°C", style = MaterialTheme.typography.titleMedium, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
                            }
                            Text("Heat Index: ${telemetry.feelsLikeC}°C", style = MaterialTheme.typography.labelSmall, color = RiskHigh)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("WET-BULB WBGT", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = SecondaryCoral, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "31.8",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = RiskCritical
                                )
                                Text("°C", style = MaterialTheme.typography.titleMedium, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
                            }
                            Text("LIMIT EXCEEDED (29° MAX)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = RiskCritical)
                        }
                    }
                }

                // Thermal Strain Gauge
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("PHYSIOLOGICAL HEAT STRAIN", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text("88% Strain", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = RiskHigh)
                        }
                        LinearProgressIndicator(
                            progress = 0.88f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = RiskCritical,
                            trackColor = SurfaceContainerHighest
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Low 24°", style = MaterialTheme.typography.labelSmall, color = AppOutline)
                            Text("Mod 28°", style = MaterialTheme.typography.labelSmall, color = AppOutline)
                            Text("High 30°", style = MaterialTheme.typography.labelSmall, color = AppOutline)
                            Text("Critical 32°+", style = MaterialTheme.typography.labelSmall, color = RiskCritical)
                        }
                    }
                }
            }
        }

        // Hourly Diurnal Inversion Waveform Strip
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            border = BorderStroke(1.dp, SurfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ShowChart, contentDescription = null, tint = PrimaryTeal)
                        Text("24-Hour Diurnal Inversion", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                    }
                    Text("HOURLY FORECAST", style = MaterialTheme.typography.labelSmall, color = AppOutline)
                }

                // Safety Windows
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RiskLowBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TaskAlt, contentDescription = null, tint = RiskLow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Safe Field Commute: 06:00 – 09:30 AM", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = RiskLow)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RiskCriticalBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = RiskCritical, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mandatory Labor Stoppage: 12:00 – 16:00 PM", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = RiskCritical)
                        }
                    }
                }

                // Hourly Micro-Telemetry Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val slots = listOf(
                        Triple("06:00", "29°C", "AQI 115"),
                        Triple("09:00", "33°C", "AQI 160"),
                        Triple("12:00", "38.5°C", "AQI 210"),
                        Triple("14:00", "41°C", "AQI 235"),
                        Triple("17:00", "36°C", "AQI 190"),
                        Triple("21:00", "32°C", "AQI 175")
                    )

                    slots.forEach { (time, temp, aqi) ->
                        val isCurrent = time == "12:00"
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) RiskCriticalBg else SurfaceContainerLow
                            ),
                            border = if (isCurrent) BorderStroke(1.dp, RiskCritical.copy(alpha = 0.5f)) else null,
                            modifier = Modifier.width(75.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(time, style = MaterialTheme.typography.labelSmall, color = if (isCurrent) RiskCritical else OnSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(temp, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = if (isCurrent) RiskCritical else OnSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(aqi, style = MaterialTheme.typography.labelSmall, color = if (isCurrent) RiskCritical else PrimaryTeal)
                            }
                        }
                    }
                }
            }
        }

        // 7-Day IMD Heatwave Radar
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = PrimaryTeal)
                        Text("IMD Weekly Radar", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                    }
                    Surface(shape = RoundedCornerShape(100.dp), color = RiskCriticalBg) {
                        Text("RED NOTICE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = RiskCritical, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }

                val forecast = listOf(
                    Triple("TODAY", "39°C / 29°C", "RED ALERT • SEVERE WBGT"),
                    Triple("TUE", "41°C / 31°C", "HEATWAVE PEAK DAY"),
                    Triple("WED", "42°C / 30°C", "DUST INVERSION & HEAT"),
                    Triple("THU", "39°C / 27°C", "ISOLATED GUSTS"),
                    Triple("FRI", "38°C / 26°C", "HEAT RELIEF COMMENCES")
                )

                forecast.forEach { (day, temps, status) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (day == "TODAY") RiskCriticalBg.copy(alpha = 0.5f) else SurfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(day, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface, modifier = Modifier.width(44.dp))
                                Column {
                                    Text(temps, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                                    Text(status, style = MaterialTheme.typography.labelSmall, color = if (day == "TODAY") RiskCritical else OnSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppOutline, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Clinical Advisory & OSHA Export
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryTeal)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Medication, contentDescription = null, tint = PrimaryTealFixed)
                    Text("Clinical Heat Guidance", style = MaterialTheme.typography.labelSmall, color = PrimaryTealFixed)
                }
                Text(
                    text = "Electrolyte Pre-Dosing Required",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Ingest 500ml oral rehydration salts (ORS) before midday wet-bulb threshold crossing. Outdoor delivery and construction personnel must initiate hydration buddy protocols.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Button(
                    onClick = { isExportingPdf = !isExportingPdf },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLowest),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().bounceClick()
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = RiskCritical, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isExportingPdf) "Ready: OSHA-2025.pdf" else "Export OSHA Form",
                        color = PrimaryTeal,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
