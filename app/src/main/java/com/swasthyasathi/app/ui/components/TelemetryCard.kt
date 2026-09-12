package com.swasthyasathi.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.data.model.EnvironmentalTelemetry
import com.swasthyasathi.app.ui.theme.*

@Composable
fun TelemetryCard(
    telemetry: EnvironmentalTelemetry,
    modifier: Modifier = Modifier
) {
    val (aqiBg, aqiText) = when (telemetry.aqiCategory) {
        "Good" -> Pair(Color(0xFFCCFBF1), Color(0xFF115E59))
        "Moderate" -> Pair(Color(0xFFFEF3C7), Color(0xFF92400E))
        "Poor" -> Pair(Color(0xFFFFEDD5), Color(0xFF9A3412))
        else -> Pair(Color(0xFFFEE2E2), Color(0xFF991B1B))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        border = BorderStroke(1.dp, SurfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "LIVE ATMOSPHERIC TELEMETRY",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppOutline,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SurfaceContainer
                ) {
                    Text(
                        text = "Open-Meteo Verified",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryTeal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Temperature & AQI Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${telemetry.temperatureC}°C",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 42.sp
                            ),
                            color = OnSurface
                        )
                        Text(
                            text = "Feels ${telemetry.feelsLikeC}°C",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppOutline,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = telemetry.weatherDescription,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = OnSurfaceVariant
                    )
                }

                // AQI Metric Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "AIR QUALITY INDEX",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppOutline
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "${telemetry.aqi}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = OnSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = aqiBg
                        ) {
                            Text(
                                text = telemetry.aqiCategory.uppercase(),
                                color = aqiText,
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Metrics Grid (Humidity, Pressure, Wind)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricBox(label = "HUMIDITY", value = "${telemetry.humidityPct}%", modifier = Modifier.weight(1f))
                MetricBox(label = "PRESSURE", value = "${telemetry.pressureHpa} hPa", modifier = Modifier.weight(1f))
                MetricBox(label = "WIND", value = "${telemetry.windSpeedKmh} km/h", modifier = Modifier.weight(1f))
            }

            HorizontalDivider(color = SurfaceContainer)

            // Station Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Station: ${telemetry.locationName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppOutline
                )
                Text(
                    text = if (telemetry.isCached) "Cached Offline" else "Live Synchronized",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (telemetry.isCached) RiskModerate else PrimaryTeal
                )
            }
        }
    }
}

@Composable
private fun MetricBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainerLow,
        border = BorderStroke(1.dp, SurfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AppOutline
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnSurface
            )
        }
    }
}
