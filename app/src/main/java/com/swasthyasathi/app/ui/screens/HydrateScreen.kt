package com.swasthyasathi.app.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.ui.components.bounceClick
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel

@Composable
fun HydrateScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val waterDrank by viewModel.waterDrankMl.collectAsState()
    val orsDrank by viewModel.orsDrankMl.collectAsState()
    val targetWater by viewModel.targetWaterMl.collectAsState()
    val targetOrs by viewModel.targetOrsMl.collectAsState()

    val totalDrank = waterDrank + orsDrank
    val totalTarget = targetWater + targetOrs
    val percentage = ((totalDrank.toFloat() / totalTarget.toFloat()) * 100).toInt().coerceIn(0, 100)

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppSurface)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hydration Target Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            border = BorderStroke(1.dp, SurfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            modifier = Modifier.size(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = TertiaryBlueContainer.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = TertiaryBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text("Hydration & ORS Telemetry", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                            Text("THERMAL CALIBRATED TARGET", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (percentage >= 60) RiskLowBg else RiskHighBg
                    ) {
                        Text(
                            text = if (percentage >= 60) "ON TRACK" else "DRINK SOON",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (percentage >= 60) RiskLow else RiskHigh,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Progress Block
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("TOTAL FLUID INTAKE", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "$totalDrank",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = OnSurface
                                    )
                                    Text(" / $totalTarget mL", style = MaterialTheme.typography.titleSmall, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp, start = 4.dp))
                                }
                            }
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = TertiaryBlue
                            )
                        }

                        LinearProgressIndicator(
                            progress = (totalDrank.toFloat() / totalTarget.toFloat()).coerceIn(0f, 1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = TertiaryBlue,
                            trackColor = SurfaceContainerHighest
                        )

                        // Dual Breakdown: Pure Water vs ORS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(TertiaryBlue, CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pure Water", style = MaterialTheme.typography.labelSmall, color = OnSurface)
                                }
                                Text("$waterDrank / $targetWater mL", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = OnSurfaceVariant)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(SecondaryCoral, CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ORS Electrolyte", style = MaterialTheme.typography.labelSmall, color = OnSurface)
                                }
                                Text("$orsDrank / $targetOrs mL", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = OnSurfaceVariant)
                            }
                        }
                    }
                }

                // Quick Log Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("QUICK LOG FLUID INTAKE", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.logWater(250) },
                            modifier = Modifier.weight(1f).bounceClick(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLow, contentColor = OnSurface),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = TertiaryBlue, modifier = Modifier.size(18.dp))
                                Text("+250ml Water", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Button(
                            onClick = { viewModel.logWater(500) },
                            modifier = Modifier.weight(1f).bounceClick(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLow, contentColor = OnSurface),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.LocalDrink, contentDescription = null, tint = TertiaryBlue, modifier = Modifier.size(18.dp))
                                Text("+500ml Water", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Button(
                            onClick = { viewModel.logOrs(250) },
                            modifier = Modifier.weight(1f).bounceClick(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryCoralFixed.copy(alpha = 0.4f), contentColor = SecondaryCoral),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Medication, contentDescription = null, tint = SecondaryCoral, modifier = Modifier.size(18.dp))
                                Text("+250ml ORS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }

                        Button(
                            onClick = { viewModel.logOrs(500) },
                            modifier = Modifier.weight(1f).bounceClick(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryCoralFixed.copy(alpha = 0.4f), contentColor = SecondaryCoral),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Science, contentDescription = null, tint = SecondaryCoral, modifier = Modifier.size(18.dp))
                                Text("+500ml ORS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { viewModel.undoHydration(250, 0) }) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Undo Last Entry", style = MaterialTheme.typography.labelSmall)
                        }

                        TextButton(onClick = { viewModel.resetHydration() }) {
                            Text("Reset Today", style = MaterialTheme.typography.labelSmall, color = AppOutline)
                        }
                    }
                }
            }
        }

        // Proactive Fluid Schedule
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
                    Text("Proactive Fluid Schedule", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                    Text("NEXT DOSE IN 25 MIN", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryTeal)
                }

                Surface(shape = RoundedCornerShape(10.dp), color = SurfaceContainerLow) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("10:00 AM", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface, modifier = Modifier.width(60.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pre-Shift Hydration Anchor", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                            Text("350ml Water before departure", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        }
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RiskLow, modifier = Modifier.size(18.dp))
                    }
                }

                Surface(shape = RoundedCornerShape(10.dp), color = SecondaryCoralFixed.copy(alpha = 0.3f)) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("11:30 AM", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SecondaryCoral, modifier = Modifier.width(60.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mandatory ORS Window", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = SecondaryCoral)
                            Text("500ml ORS packet to balance sodium", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        }
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = SecondaryCoral, modifier = Modifier.size(18.dp))
                    }
                }

                Surface(shape = RoundedCornerShape(10.dp), color = SurfaceContainerLow) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("02:00 PM", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = OnSurfaceVariant, modifier = Modifier.width(60.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Post-Noon Chilled Water", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                            Text("500ml Water in shaded haven", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        }
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = AppOutline, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
