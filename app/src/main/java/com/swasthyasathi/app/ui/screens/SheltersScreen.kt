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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.data.model.CoolingShelter
import com.swasthyasathi.app.ui.components.ShelterMapView
import com.swasthyasathi.app.ui.components.bounceClick
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel

@Composable
fun SheltersScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val shelters by viewModel.shelters.collectAsState()
    val city by viewModel.currentCity.collectAsState()
    val selectedShelterOnMap by viewModel.selectedShelterOnMap.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") } // all, cooling, water, clinic
    var navTargetShelter by remember { mutableStateOf<CoolingShelter?>(null) }

    val filteredShelters = remember(shelters, searchQuery, selectedCategory) {
        shelters.filter { s ->
            val matchCategory = selectedCategory == "all" || s.category == selectedCategory
            val matchQuery = searchQuery.isBlank() || s.name.contains(searchQuery, ignoreCase = true) || s.address.contains(searchQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
    }

    val activeHaven = selectedShelterOnMap ?: filteredShelters.firstOrNull() ?: shelters.firstOrNull()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppSurface)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = RiskLowBg
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Park, contentDescription = null, tint = RiskLow, modifier = Modifier.size(20.dp))
                    }
                }
                Column {
                    Text("Relief & Cool Haven Radar", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryTeal)
                    Text("${city.name} • Live GPS Active", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                }
            }

            Surface(shape = RoundedCornerShape(100.dp), color = RiskLowBg) {
                Text(
                    text = "GOVT SYNC",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = RiskLow,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search nearby shelters, piaos, clinics...", style = MaterialTheme.typography.bodySmall) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppOutline) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Cancel, contentDescription = "Clear", tint = AppOutline)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceContainerLowest,
                unfocusedContainerColor = SurfaceContainerLowest,
                focusedBorderColor = PrimaryTeal,
                unfocusedBorderColor = SurfaceContainerHigh
            ),
            singleLine = true
        )

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chips = listOf(
                Pair("all", "All (${shelters.size})"),
                Pair("cooling", "Cooling Centers (2)"),
                Pair("water", "Free Water Piaos (1)"),
                Pair("clinic", "Mohalla Clinics (1)")
            )

            chips.forEach { (cat, label) ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)) },
                    shape = RoundedCornerShape(100.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryTeal,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceContainerHigh,
                        labelColor = OnSurfaceVariant
                    ),
                    border = null
                )
            }
        }

        // === INTERACTIVE VECTOR CANVAS SHELTERS MAP ===
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                    Text("Interactive Relief Haven Radar", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = OnSurfaceVariant)
                }
                Text("Tap markers to route", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = PrimaryTeal)
            }

            ShelterMapView(
                shelters = filteredShelters,
                selectedShelter = activeHaven,
                onShelterSelected = { shelter ->
                    viewModel.selectShelterOnMap(shelter)
                }
            )
        }

        // Stylized Map HUD & Shaded Route Preview Card
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.NearMe, contentDescription = null, tint = RiskLow, modifier = Modifier.size(18.dp))
                        Text("Safe Shaded Path Matrix", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryTeal)
                    }
                    Surface(shape = RoundedCornerShape(100.dp), color = RiskLowBg) {
                        Text("Canopy > 80%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = RiskLow, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }

                // Shaded Corridor Card (Bound to selected Haven)
                Surface(shape = RoundedCornerShape(14.dp), color = RiskLowBg, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("TOP RECOMMENDED SHADED CORRIDOR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = RiskLow)
                        Text(
                            activeHaven?.let { "Shaded Corridor to ${it.name}" } ?: "Radial Corridors to Central Haven Oasis",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryTeal
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), color = SurfaceContainerLowest) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    val dist = activeHaven?.distanceMeters ?: 350
                                    Text("${dist}m", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryTeal)
                                    Text("${maxOf(1, dist / 70)} min walk", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                }
                            }
                            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), color = SurfaceContainerLowest) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    val canopy = activeHaven?.canopyPct ?: 84
                                    Text("$canopy%", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = RiskLow)
                                    Text("Dense Canopy", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                }
                            }
                            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), color = SurfaceContainerLowest) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    val red = activeHaven?.shadeReductionC ?: 5.2f
                                    Text("-${String.format(java.util.Locale.US, "%.1f", red)}°C", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = SecondaryCoral)
                                    Text("Microclimate", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                navTargetShelter = activeHaven
                            },
                            modifier = Modifier.fillMaxWidth().bounceClick(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Shaded Route Navigation")
                        }
                    }
                }
            }
        }

        // Verified Havens List
        Text(
            text = "VERIFIED HAVENS NEARBY (${filteredShelters.size})",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = OnSurfaceVariant
        )

        filteredShelters.forEach { shelter ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = when (shelter.category) {
                                    "cooling" -> RiskHighBg
                                    "water" -> TertiaryBlueContainer.copy(alpha = 0.2f)
                                    else -> RiskCriticalBg
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (shelter.category) {
                                            "cooling" -> Icons.Default.AcUnit
                                            "water" -> Icons.Default.WaterDrop
                                            else -> Icons.Default.LocalHospital
                                        },
                                        contentDescription = null,
                                        tint = when (shelter.category) {
                                            "cooling" -> RiskHigh
                                            "water" -> TertiaryBlue
                                            else -> RiskCritical
                                        },
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(shelter.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                                    Surface(shape = RoundedCornerShape(100.dp), color = RiskLowBg) {
                                        Text(shelter.status, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = RiskLow, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Text("${shelter.distanceMeters}m away • ${shelter.address}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("${shelter.tempC.toInt()}°C", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = RiskLow)
                            Text("Cool Temp", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        }
                    }

                    // Amenities chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        shelter.amenities.forEach { a ->
                            Surface(shape = RoundedCornerShape(8.dp), color = SurfaceContainer) {
                                Text(a, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }

                    // Card Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Verified ${shelter.verifiedTime}", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Button(
                            onClick = {
                                viewModel.selectShelterOnMap(shelter)
                                navTargetShelter = shelter
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.TurnRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Navigate")
                        }
                    }
                }
            }
        }
    }

    // Route Navigation Dialog
    if (navTargetShelter != null) {
        val shelter = navTargetShelter!!
        AlertDialog(
            onDismissRequest = { navTargetShelter = null },
            title = {
                Text(shelter.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Continuous tree canopy walking corridor active.", style = MaterialTheme.typography.bodyMedium)
                    Text("Distance: ${shelter.distanceMeters} meters (~${shelter.distanceMeters / 75} mins walk)", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    Text("Microclimate Temp: ${shelter.tempC}°C (Reduced UV load)", style = MaterialTheme.typography.bodySmall, color = RiskLow)
                }
            },
            confirmButton = {
                Button(
                    onClick = { navTargetShelter = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Start Turn-By-Turn")
                }
            },
            dismissButton = {
                TextButton(onClick = { navTargetShelter = null }) {
                    Text("Close")
                }
            }
        )
    }
}
