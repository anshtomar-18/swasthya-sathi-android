package com.swasthyasathi.app.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.swasthyasathi.app.data.model.CityPreset
import com.swasthyasathi.app.data.model.searchCities
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel

@Composable
fun LocationSearchDialog(
    viewModel: HealthViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentCity by viewModel.currentCity.collectAsState()
    val isLocatingGps by viewModel.isLocatingGps.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedMetroChip by remember { mutableStateOf("All") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.detectRealLocation(context)
            onDismiss()
        }
    }

    val filteredCities = remember(searchQuery, selectedMetroChip) {
        val baseList = searchCities(searchQuery)
        if (selectedMetroChip == "All") {
            baseList
        } else {
            baseList.filter {
                it.name.contains(selectedMetroChip, ignoreCase = true) ||
                it.state.contains(selectedMetroChip, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp)),
            color = SurfaceContainerLowest,
            border = BorderStroke(1.dp, SurfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
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
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldContainer,
                            border = BorderStroke(1.5.dp, EmeraldPrimary)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocationSearching,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Select Location",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = OnSurface
                            )
                            Text(
                                text = "Live Microclimate Telemetry",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = OnSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // GPS Auto-detect banner button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                            viewModel.detectRealLocation(context) {
                                // Fallback handled inside viewModel
                            }
                            onDismiss()
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = RiskLowBg,
                    border = BorderStroke(1.5.dp, RiskLow.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = RiskLow
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isLocatingGps) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Auto-Detect Current GPS",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = EmeraldPrimary
                                ) {
                                    Text(
                                        text = "HIGH PRECISION",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Acquire live coordinates via on-device satellite fix",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = OnSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = RiskLow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search city or zone (e.g. Jaipur, Bengaluru)...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = EmeraldPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = OnSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = SurfaceContainerHigh,
                        focusedContainerColor = SurfaceContainerLowest,
                        unfocusedContainerColor = SurfaceContainerLowest
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Metro Quick Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val metros = listOf("All", "Delhi", "Mumbai", "Bengaluru", "Kolkata", "Chennai", "Hyderabad", "Jaipur", "Pune")
                    metros.forEach { metro ->
                        val isSelected = selectedMetroChip == metro
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMetroChip = metro },
                            label = {
                                Text(
                                    text = metro,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            shape = RoundedCornerShape(100.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = SurfaceContainerHigh,
                                labelColor = OnSurfaceVariant
                            ),
                            border = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Location option if user typed query
                if (searchQuery.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable {
                                viewModel.selectCustomLocation(searchQuery)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainerLow,
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddLocationAlt,
                                contentDescription = null,
                                tint = EmeraldPrimary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Use Custom Search: \"$searchQuery\"",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = "Pull live Open-Meteo environmental telemetry for this region",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = OnSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // City List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredCities) { city ->
                        val isCurrent = currentCity.name == city.name
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectCity(city)
                                    onDismiss()
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isCurrent) EmeraldContainer.copy(alpha = 0.6f) else SurfaceContainerLowest,
                            border = BorderStroke(
                                if (isCurrent) 1.5.dp else 1.dp,
                                if (isCurrent) EmeraldPrimary else SurfaceContainerHigh.copy(alpha = 0.8f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(34.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isCurrent) EmeraldPrimary else SurfaceContainerHigh
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isCurrent) Icons.Default.Check else Icons.Default.LocationCity,
                                                contentDescription = null,
                                                tint = if (isCurrent) Color.White else EmeraldPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = city.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = OnSurface
                                        )
                                        Text(
                                            text = "${city.state} • ${city.zoneDescription}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = OnSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SurfaceContainerHigh.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "${String.format(java.util.Locale.US, "%.2f", city.lat)}°N",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = OnSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
