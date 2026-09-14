package com.swasthyasathi.app.ui.components

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.swasthyasathi.app.data.model.SIH_DEMO_PRESETS
import com.swasthyasathi.app.data.model.UserProfile
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel

@Composable
fun ConsumerProfileDialog(
    viewModel: HealthViewModel,
    onDismiss: () -> Unit,
    onNavigateToFullProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isEditing by remember { mutableStateOf(false) }

    // Editable state initialized from currentProfile
    var editName by remember(currentProfile) { mutableStateOf(currentProfile.firstName) }
    var editAge by remember(currentProfile) { mutableStateOf(currentProfile.exactAge) }
    var editGender by remember(currentProfile) { mutableStateOf(currentProfile.gender) }
    var editAbhaId by remember(currentProfile) { mutableStateOf(currentProfile.abhaId) }
    var editOutdoorHours by remember(currentProfile) { mutableStateOf(currentProfile.outdoorHours) }
    var editDiseases by remember(currentProfile) { mutableStateOf(currentProfile.diseases.toSet()) }
    var editContactName by remember(currentProfile) { mutableStateOf(currentProfile.emergencyContactName) }
    var editContactPhone by remember(currentProfile) { mutableStateOf(currentProfile.emergencyContactPhone) }

    val allDiseaseOptions = listOf(
        "Asthma / COPD",
        "Cardiovascular / Hypertension",
        "Type-2 Diabetes",
        "Heat Intolerance",
        "Kidney / Renal Disorder",
        "Chronic Allergies",
        "None"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp)),
            color = SurfaceContainerLowest,
            border = BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.3f)),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with Avatar and Close Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = CircleShape,
                            color = EmeraldContainer,
                            border = BorderStroke(2.dp, EmeraldPrimary)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "${currentProfile.firstName} ${currentProfile.lastName}".trim(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = RiskLow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "ABHA: ${currentProfile.abhaId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RiskLow,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { isEditing = !isEditing },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Visibility else Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = EmeraldPrimary
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = OnSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(color = SurfaceContainerHigh)

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (!isEditing) {
                        // ==================== VIEW MODE ====================
                        // Demographic Summary Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                                border = BorderStroke(1.dp, SurfaceContainerHigh)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "EXACT AGE & GENDER",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${currentProfile.exactAge} yrs • ${currentProfile.gender}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = OnSurface
                                    )
                                    Text(
                                        text = "Bracket: ${currentProfile.ageGroup}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldPrimary
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                                border = BorderStroke(1.dp, SurfaceContainerHigh)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "DAILY OUTDOOR TIME",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${currentProfile.outdoorHours.toInt()} Hours / Day",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = OnSurface
                                    )
                                    Text(
                                        text = "${currentProfile.outdoorActivityLevel.replaceFirstChar { it.uppercase() }} Exertion",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldPrimary
                                    )
                                }
                            }
                        }

                        // Work Environment Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                            border = BorderStroke(1.dp, SurfaceContainerHigh)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.WbSunny, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(
                                        text = "WORK ENVIRONMENT & TIMINGS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant
                                    )
                                    Text(
                                        text = currentProfile.workEnvironment,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = OnSurface
                                    )
                                    Text(
                                        text = currentProfile.shiftTimings,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Active Diagnosed Diseases & Vulnerabilities
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                            border = BorderStroke(1.dp, SurfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "DIAGNOSED CONDITIONS & SENSITIVITIES",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant
                                    )
                                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    currentProfile.diseases.forEach { d ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = EmeraldContainer,
                                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = d,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = EmeraldPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Emergency Kin & Mesh Dispatch
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                            border = BorderStroke(1.dp, SurfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "EMERGENCY KIN FOR DISTRESS SOS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant
                                )
                                Text(
                                    text = "${currentProfile.emergencyContactName} (${currentProfile.emergencyRelation})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = currentProfile.emergencyContactPhone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 1-Tap Presets
                        Text(
                            text = "QUICK LOAD ARCHETYPE (1-TAP)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SIH_DEMO_PRESETS.take(2).forEach { preset ->
                                OutlinedButton(
                                    onClick = {
                                        viewModel.applyPreset(preset)
                                        Toast.makeText(context, "Loaded: ${preset.label}", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = preset.label.split("(")[0].trim(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldPrimary
                                    )
                                }
                            }
                        }

                    } else {
                        // ==================== EDIT MODE ====================
                        Text(
                            text = "EDIT PERSONAL PARAMETERS",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary
                        )

                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("First Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Exact Age:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            IconButton(onClick = { if (editAge > 5) editAge-- }) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease Age", tint = EmeraldPrimary)
                            }
                            Text(
                                text = "$editAge yrs",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary
                            )
                            IconButton(onClick = { if (editAge < 105) editAge++ }) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase Age", tint = EmeraldPrimary)
                            }
                        }

                        // Outdoor Hours Slider
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Outdoor Work Hours:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                Text("${editOutdoorHours.toInt()} hrs/day", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = editOutdoorHours,
                                onValueChange = { editOutdoorHours = it },
                                valueRange = 0f..14f,
                                steps = 13,
                                colors = SliderDefaults.colors(
                                    thumbColor = EmeraldPrimary,
                                    activeTrackColor = EmeraldPrimary
                                )
                            )
                        }

                        // Disease Selection
                        Text(
                            text = "Medical Conditions & Sensitivities:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        allDiseaseOptions.forEach { disease ->
                            val isSelected = editDiseases.contains(disease)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) EmeraldContainer else SurfaceContainerLow,
                                border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else SurfaceContainerHigh),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        editDiseases = if (disease == "None") {
                                            setOf("None")
                                        } else {
                                            val cur = editDiseases.filter { it != "None" }.toMutableSet()
                                            if (cur.contains(disease)) cur.remove(disease) else cur.add(disease)
                                            if (cur.isEmpty()) setOf("None") else cur
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = disease,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                        color = OnSurface
                                    )
                                }
                            }
                        }

                        // Emergency Kin
                        OutlinedTextField(
                            value = editContactName,
                            onValueChange = { editContactName = it },
                            label = { Text("Caregiver Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = editContactPhone,
                            onValueChange = { editContactPhone = it },
                            label = { Text("Caregiver Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Save Modifications Button
                        Button(
                            onClick = {
                                val computedAgeGroup = when {
                                    editAge < 18 -> "under18"
                                    editAge in 18..40 -> "18-40"
                                    editAge in 41..60 -> "41-60"
                                    else -> "60+"
                                }
                                val computedExertion = when {
                                    editOutdoorHours >= 6f -> "high"
                                    editOutdoorHours >= 3f -> "moderate"
                                    else -> "low"
                                }
                                val clinicalSensitivities = mutableListOf<String>()
                                if (editDiseases.any { it.contains("Heat", ignoreCase = true) }) clinicalSensitivities.add("heat")
                                if (editDiseases.any { it.contains("Asthma", ignoreCase = true) || it.contains("Allergies", ignoreCase = true) }) clinicalSensitivities.add("respiratory")
                                if (editDiseases.any { it.contains("Cardiovascular", ignoreCase = true) }) clinicalSensitivities.add("cardiovascular")
                                if (clinicalSensitivities.isEmpty()) clinicalSensitivities.add("none")

                                val updated = currentProfile.copy(
                                    firstName = editName.trim().ifEmpty { "Aarav" },
                                    exactAge = editAge,
                                    ageGroup = computedAgeGroup,
                                    outdoorHours = editOutdoorHours,
                                    outdoorActivityLevel = computedExertion,
                                    diseases = editDiseases.toList(),
                                    sensitivities = clinicalSensitivities,
                                    emergencyContactName = editContactName.trim().ifEmpty { "Caregiver" },
                                    emergencyContactPhone = editContactPhone.trim().ifEmpty { "+91 98765 43210" }
                                )

                                viewModel.saveProfile(updated)
                                Toast.makeText(context, "Personal profile saved & risk recomputed!", Toast.LENGTH_SHORT).show()
                                isEditing = false
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Modifications Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Footer Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onNavigateToFullProfile()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Detailed Calibration", color = EmeraldPrimary)
                    }

                    if (!isEditing) {
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile")
                        }
                    }
                }
            }
        }
    }
}
