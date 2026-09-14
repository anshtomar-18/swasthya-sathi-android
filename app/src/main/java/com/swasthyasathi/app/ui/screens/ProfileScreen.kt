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
import androidx.compose.ui.draw.clip
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
    var exactAge by remember(currentProfile) { mutableStateOf(currentProfile.exactAge) }
    var gender by remember(currentProfile) { mutableStateOf(currentProfile.gender) }
    var abhaId by remember(currentProfile) { mutableStateOf(currentProfile.abhaId) }
    var outdoorHours by remember(currentProfile) { mutableStateOf(currentProfile.outdoorHours) }
    var workEnv by remember(currentProfile) { mutableStateOf(currentProfile.workEnvironment) }
    var selectedDiseases by remember(currentProfile) { mutableStateOf(currentProfile.diseases.toSet()) }
    var contactName by remember(currentProfile) { mutableStateOf(currentProfile.emergencyContactName) }
    var contactPhone by remember(currentProfile) { mutableStateOf(currentProfile.emergencyContactPhone) }

    val allDiseaseOptions = listOf(
        "Asthma / COPD",
        "Cardiovascular / Hypertension",
        "Type-2 Diabetes",
        "Heat Intolerance",
        "Kidney / Renal Disorder",
        "Chronic Allergies",
        "None"
    )

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppSurface,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Profile & Health Personalization",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                        Text(
                            text = "Biological & clinical telemetry calibration",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = EmeraldPrimary)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Archetype Presets
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainerHigh)
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
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary
                        )
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
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
                                color = if (isSelected) EmeraldPrimary else SurfaceContainerLow,
                                border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else SurfaceContainerHigh),
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
                                        color = if (isSelected) EmeraldFixed else OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Personal Demographics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "PERSONAL DEMOGRAPHICS",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Exact Age", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(onClick = { if (exactAge > 5) exactAge-- }) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = EmeraldPrimary)
                                }
                                Text(
                                    text = "$exactAge yrs",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary
                                )
                                IconButton(onClick = { if (exactAge < 105) exactAge++ }) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = EmeraldPrimary)
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Gender", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("Male", "Female", "Other").forEach { g ->
                                    val sel = gender == g
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (sel) EmeraldPrimary else SurfaceContainerLow,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { gender = g }
                                    ) {
                                        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                            Text(g, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = if (sel) Color.White else OnSurface)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = abhaId,
                        onValueChange = { abhaId = it },
                        label = { Text("ABHA ID (Ayushman Bharat)") },
                        leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = RiskLow) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Outdoor Exertion & Work Environment
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "OUTDOOR EXERTION & WORK TIME",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Daily Outdoor Work Exposure:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Surface(shape = RoundedCornerShape(6.dp), color = EmeraldContainer) {
                            Text(
                                text = "${outdoorHours.toInt()} Hours / Day",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Slider(
                        value = outdoorHours,
                        onValueChange = { outdoorHours = it },
                        valueRange = 0f..14f,
                        steps = 13,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldPrimary,
                            activeTrackColor = EmeraldPrimary
                        )
                    )

                    Text("Primary Work Environment:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Outdoor Field / Direct Sun",
                            "Construction / Labor",
                            "Delivery / Transit",
                            "Indoor / AC Office",
                            "Mixed Exposure"
                        ).forEach { env ->
                            val isSel = workEnv == env
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) EmeraldPrimary else SurfaceContainerLow,
                                modifier = Modifier.clickable { workEnv = env }
                            ) {
                                Text(
                                    text = env,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSel) Color.White else OnSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Diagnosed Conditions & Vulnerabilities Multi-Select
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "DIAGNOSED CONDITIONS & SENSITIVITIES",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary
                    )

                    allDiseaseOptions.forEach { disease ->
                        val isChecked = selectedDiseases.contains(disease)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isChecked) EmeraldContainer else SurfaceContainerLow,
                            border = BorderStroke(1.dp, if (isChecked) EmeraldPrimary else SurfaceContainerHigh),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedDiseases = if (disease == "None") {
                                        setOf("None")
                                    } else {
                                        val cur = selectedDiseases.filter { it != "None" }.toMutableSet()
                                        if (cur.contains(disease)) cur.remove(disease) else cur.add(disease)
                                        if (cur.isEmpty()) setOf("None") else cur
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                                )
                                Text(
                                    text = disease,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal),
                                    color = OnSurface
                                )
                            }
                        }
                    }
                }
            }

            // Designated Caregiver & Emergency Kin
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "EMERGENCY CAREGIVER / KIN",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary
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

            // Save and Reset Buttons
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
                    Text("Reset")
                }

                Button(
                    onClick = {
                        val computedAgeGroup = when {
                            exactAge < 18 -> "under18"
                            exactAge in 18..40 -> "18-40"
                            exactAge in 41..60 -> "41-60"
                            else -> "60+"
                        }
                        val computedExertion = when {
                            outdoorHours >= 6f -> "high"
                            outdoorHours >= 3f -> "moderate"
                            else -> "low"
                        }
                        val clinicalSensitivities = mutableListOf<String>()
                        if (selectedDiseases.any { it.contains("Heat", ignoreCase = true) }) clinicalSensitivities.add("heat")
                        if (selectedDiseases.any { it.contains("Asthma", ignoreCase = true) || it.contains("Allergies", ignoreCase = true) }) clinicalSensitivities.add("respiratory")
                        if (selectedDiseases.any { it.contains("Cardiovascular", ignoreCase = true) }) clinicalSensitivities.add("cardiovascular")
                        if (clinicalSensitivities.isEmpty()) clinicalSensitivities.add("none")

                        val updated = currentProfile.copy(
                            firstName = name.trim().ifEmpty { "Aarav" },
                            exactAge = exactAge,
                            ageGroup = computedAgeGroup,
                            gender = gender,
                            abhaId = abhaId.trim().ifEmpty { "91-4521-8832-1092" },
                            outdoorHours = outdoorHours,
                            outdoorActivityLevel = computedExertion,
                            workEnvironment = workEnv,
                            diseases = selectedDiseases.toList(),
                            sensitivities = clinicalSensitivities,
                            emergencyContactName = contactName.trim().ifEmpty { "Caregiver" },
                            emergencyContactPhone = contactPhone.trim().ifEmpty { "+91 98765 43210" }
                        )

                        viewModel.saveProfile(updated)
                        Toast.makeText(context, "Profile saved & risk recomputed immediately!", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    },
                    modifier = Modifier.weight(1.8f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save & Apply Immediately", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
