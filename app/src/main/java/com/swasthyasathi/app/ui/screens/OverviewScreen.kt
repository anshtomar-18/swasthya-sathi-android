package com.swasthyasathi.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.ui.components.bounceClick
import com.swasthyasathi.app.ui.theme.*

@Composable
fun OverviewScreen(
    onLaunchDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppSurface)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryTeal),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = PrimaryTealFixed.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "SMART INDIA HACKATHON 2026 • PS 26181",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryTealFixed,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = "SwasthyaSathi AI",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = Color.White
                )

                Text(
                    text = "Personalized Environmental Health Monitoring & Early-Warning System.",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.95f),
                    lineHeight = 22.sp
                )

                Text(
                    text = "Translates generic weather metrics into individual health risk levels (Low, Moderate, High, Critical) tailored to your age, outdoor exertion, and physiological sensitivities.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )

                Button(
                    onClick = onLaunchDashboard,
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryCoral),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = "Launch Live Dashboard",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }

        // The Problem & Indian Hazard Scenarios
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            border = BorderStroke(1.dp, SurfaceContainer)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "THE PROBLEM WE SOLVE",
                    style = MaterialTheme.typography.labelLarge,
                    color = AppOutline
                )
                Text(
                    text = "Why Generic Forecasts Fail Indian Citizens",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                Text(
                    text = "A report of '39°C, AQI 185' carries vastly different medical risks for a 65-year-old cardiac patient versus an indoor office worker. Without personalization, citizens fail to take preventive action until crisis hits.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    lineHeight = 20.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HazardChip("Heatwave", Icons.Default.Thermostat, RiskHigh, Modifier.weight(1f))
                    HazardChip("Toxic Smog", Icons.Default.Air, TertiaryNavy, Modifier.weight(1f))
                    HazardChip("Monsoon", Icons.Default.WaterDrop, PrimaryTeal, Modifier.weight(1f))
                }
            }
        }

        // Core Product Logic Chain (6-Stage Stepper)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            border = BorderStroke(1.dp, SurfaceContainer)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "CORE LOGIC CHAIN",
                    style = MaterialTheme.typography.labelLarge,
                    color = AppOutline
                )
                Text(
                    text = "How Risk is Deterministically Evaluated",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )

                StepRow(1, "Atmospheric Telemetry", "Open-Meteo Weather & CPCB AQI feeds")
                StepRow(2, "Hyper-Local Coordinates", "GPS & Indian district coordinate matching")
                StepRow(3, "Personal Health Profile", "Age bracket, exertion, respiratory & cardio sensitivities")
                StepRow(4, "Deterministic Risk Engine", "Auditable, rule-based formula without black-box drift")
                StepRow(5, "Personal Risk Tier", "Low, Moderate, High, or Critical classification")
                StepRow(6, "Preventive Action Protocols", "Hydration dosing, pacing, and 2-step distress SOS")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HazardChip(label: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainerLow,
        border = BorderStroke(1.dp, SurfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurface)
        }
    }
}

@Composable
private fun StepRow(step: Int, title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = PrimaryTeal,
            modifier = Modifier.size(26.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("$step", color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }
    }
}
