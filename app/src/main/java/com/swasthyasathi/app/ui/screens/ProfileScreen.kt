package com.swasthyasathi.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.data.model.SIH_DEMO_PRESETS
import com.swasthyasathi.app.data.model.UserProfile
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: HealthViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    var name by remember(currentProfile) { mutableStateOf(currentProfile.firstName) }
    var ageGroup by remember(currentProfile) { mutableStateOf(currentProfile.ageGroup) }
    var exertion by remember(currentProfile) { mutableStateOf(currentProfile.outdoorActivityLevel) }
    var sensitivities by remember(currentProfile) { mutableStateOf(currentProfile.sensitivities.toSet()) }
    var contactName by remember(currentProfile) { mutableStateOf(currentProfile.emergencyContactName) }
    var contactPhone by remember(currentProfile) { mutableStateOf(currentProfile.emergencyContactPhone) }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppSurface,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Profile & Sensitivities",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                        Text(
                            text = "Personal biological parameters for risk calculations",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppOutline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceContainerLowest)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // SIH Demographic Demo Presets
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DEMO ARCHETYPES (1-CLICK LOAD)",
                            style = MaterialTheme.typography.labelLarge,
                            color = AppOutline
                        )
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SIH_DEMO_PRESETS.forEach { preset ->
                            val isSelected = currentProfile.firstName == preset.profile.firstName
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PrimaryTeal else SurfaceContainerLow,
                                border = BorderStroke(1.dp, if (isSelected) PrimaryTeal else SurfaceContainer),
                                modifier = Modifier.clickable {
                                    viewModel.applyPreset(preset)
                                    Toast.makeText(context, "Loaded preset: ${preset.label}", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Text(
                                        text = preset.profile.firstName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.White else OnSurface
                                    )
                                    Text(
                                        text = preset.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) PrimaryTealFixed else AppOutline
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Name & Age Bracket
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
                        text = "PERSONAL PARAMETERS",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppOutline
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Age Bracket", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("under18", "18-40", "41-60", "60+").forEach { bracket ->
                                val selected = ageGroup == bracket
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) PrimaryTeal else SurfaceContainerLow,
                                    border = BorderStroke(1.dp, if (selected) PrimaryTeal else SurfaceContainer),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { ageGroup = bracket }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = bracket,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) Color.White else OnSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Outdoor Exertion / Exposure", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("low", "moderate", "high").forEach { lvl ->
                                val selected = exertion.equals(lvl, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) PrimaryTeal else SurfaceContainerLow,
                                    border = BorderStroke(1.dp, if (selected) PrimaryTeal else SurfaceContainer),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { exertion = lvl }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = lvl.replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) Color.White else OnSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Clinical Sensitivity Flags
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
                        text = "CLINICAL SENSITIVITY FLAGS",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppOutline
                    )

                    listOf(
                        Triple("heat", "Heat Sensitivity", "Accelerated dehydration, heat cramps, or low heat tolerance"),
                        Triple("respiratory", "Respiratory / Asthma", "Airway hypersensitivity, chronic bronchitis, reactive cough"),
                        Triple("cardiovascular", "Cardiovascular Sensitivity", "Hypertension, resting tachycardia, vascular strain"),
                        Triple("none", "None / Standard Baseline", "Standard physiological tolerance without known sensitivities")
                    ).forEach { (flag, title, desc) ->
                        val isChecked = sensitivities.contains(flag)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isChecked) PrimaryTealFixed.copy(alpha = 0.25f) else SurfaceContainerLow,
                            border = BorderStroke(1.dp, if (isChecked) PrimaryTeal else SurfaceContainer),
                            modifier = Modifier.clickable {
                                sensitivities = if (flag == "none") {
                                    setOf("none")
                                } else {
                                    val current = sensitivities.filter { it != "none" }.toMutableSet()
                                    if (current.contains(flag)) current.remove(flag) else current.add(flag)
                                    if (current.isEmpty()) setOf("none") else current
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryTeal)
                                )
                                Column {
                                    Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                                    Text(desc, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Emergency Caregiver Details
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
                        text = "DESIGNATED EMERGENCY CAREGIVER",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppOutline
                    )
                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        label = { Text("Caregiver Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Caregiver Phone / SMS") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Action Buttons: Save & Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.resetProfile()
                        Toast.makeText(context, "Reset to default profile", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Reset Defaults")
                }

                Button(
                    onClick = {
                        val updated = currentProfile.copy(
                            firstName = name.trim().ifEmpty { "Aarav" },
                            ageGroup = ageGroup,
                            outdoorActivityLevel = exertion,
                            sensitivities = sensitivities.toList(),
                            emergencyContactName = contactName.trim().ifEmpty { "Emergency Caregiver" },
                            emergencyContactPhone = contactPhone.trim().ifEmpty { "+91 98765 43210" }
                        )
                        viewModel.saveProfile(updated)
                        Toast.makeText(context, "Profile updated & risk recomputed!", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    },
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save & Apply", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
