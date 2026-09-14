package com.swasthyasathi.app.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.data.model.SIH_DEMO_PRESETS
import com.swasthyasathi.app.data.model.UserProfile
import com.swasthyasathi.app.ui.components.SwasthyaLogo
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel

@Composable
fun LoginScreen(
    viewModel: HealthViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Login, 1: Create Account

    // Login Form State
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Create Account Form State
    var newName by remember { mutableStateOf("") }
    var newAgeText by remember { mutableStateOf("28") }
    var newGender by remember { mutableStateOf("Male") }
    var newAbhaId by remember { mutableStateOf("91-4820-9912-3401") }
    var outdoorHours by remember { mutableStateOf(6f) }
    var workEnv by remember { mutableStateOf("Outdoor Field / Direct Sun") }
    var selectedDiseases by remember { mutableStateOf(setOf("Heat Intolerance")) }
    var emergencyName by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var emergencyRelation by remember { mutableStateOf("Family / Spouse") }

    val availableDiseases = listOf(
        "Asthma / COPD",
        "Cardiovascular / Hypertension",
        "Type-2 Diabetes",
        "Heat Intolerance",
        "Kidney / Renal Disorder",
        "Chronic Allergies",
        "None"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppSurface)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Official App Logo & Branding Header
        Surface(
            modifier = Modifier.size(76.dp),
            shape = CircleShape,
            color = EmeraldContainer,
            border = BorderStroke(2.dp, EmeraldPrimary)
        ) {
            Box(contentAlignment = Alignment.Center) {
                SwasthyaLogo(size = 56.dp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "SWASTHYASATHI",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = EmeraldPrimary
            )
            Text(
                text = "-AI",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = SecondaryCoral
            )
        }

        Text(
            text = "Personal Environmental Health & Climate Telemetry",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tab Selector: [ Log In ] | [ Create Account ]
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceContainerLow,
            border = BorderStroke(1.dp, SurfaceContainerHigh),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedTab == 0) EmeraldPrimary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 0 }
                ) {
                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Log In",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTab == 0) Color.White else OnSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedTab == 1) EmeraldPrimary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 1 }
                ) {
                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTab == 1) Color.White else OnSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tab Content
        if (selectedTab == 0) {
            // ==================== LOG IN TAB ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Sign in with your ABHA ID or Mobile",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )

                    OutlinedTextField(
                        value = loginIdentifier,
                        onValueChange = { loginIdentifier = it },
                        label = { Text("ABHA ID / Mobile Number / Email") },
                        placeholder = { Text("e.g. 91-4521-8832-1092") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Password or OTP") },
                        placeholder = { Text("Enter password or 6-digit OTP") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Button(
                        onClick = {
                            Toast.makeText(context, "Welcome to SwasthyaSathi-AI!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log In to Health Space", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick 1-Tap Demo Sign-in for Evaluators
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
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
                            text = "INSTANT DEMO PERSONAS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary
                        )
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                    }

                    SIH_DEMO_PRESETS.forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceContainerLowest,
                            border = BorderStroke(1.dp, SurfaceContainerHigh),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.applyPreset(preset)
                                    Toast.makeText(context, "Logged in as ${preset.profile.firstName} (${preset.label})", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${preset.profile.firstName} ${preset.profile.lastName}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = OnSurface
                                    )
                                    Text(
                                        text = preset.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant
                                    )
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onLoginSuccess) {
                Text("Continue as Guest / Explore Demo", color = OnSurfaceVariant, fontSize = 13.sp)
            }

        } else {
            // ==================== CREATE ACCOUNT TAB ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, SurfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Create Your Environmental Health Profile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )

                    // 1. Full Name
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Aarav Sharma") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 2. Exact Age & Gender Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newAgeText,
                            onValueChange = { newAgeText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Exact Age") },
                            placeholder = { Text("e.g. 28") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Column(modifier = Modifier.weight(1.5f)) {
                            Text("Gender", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Male", "Female", "Other").forEach { g ->
                                    val isSelected = newGender == g
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) EmeraldPrimary else SurfaceContainerLow,
                                        border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else SurfaceContainerHigh),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { newGender = g }
                                    ) {
                                        Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                            Text(
                                                text = g,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) Color.White else OnSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. ABHA Health ID
                    OutlinedTextField(
                        value = newAbhaId,
                        onValueChange = { newAbhaId = it },
                        label = { Text("ABHA ID (Ayushman Bharat)") },
                        leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = RiskLow) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 4. Pre-Existing Diseases / Vulnerabilities Multi-Select
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Pre-Existing Diseases & Medical Conditions",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                        Text(
                            text = "Select all that apply for calibrated real-time risk alerts:",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )

                        availableDiseases.chunked(2).forEach { rowDiseases ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowDiseases.forEach { disease ->
                                    val isChecked = selectedDiseases.contains(disease)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isChecked) EmeraldFixed.copy(alpha = 0.35f) else SurfaceContainerLow,
                                        border = BorderStroke(1.dp, if (isChecked) EmeraldPrimary else SurfaceContainerHigh),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedDiseases = if (disease == "None") {
                                                    setOf("None")
                                                } else {
                                                    val current = selectedDiseases.filter { it != "None" }.toMutableSet()
                                                    if (current.contains(disease)) current.remove(disease) else current.add(disease)
                                                    if (current.isEmpty()) setOf("None") else current
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = null,
                                                colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = disease,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = OnSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Daily Outdoor Work Exposure Hours Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Outdoor Work Time",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
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
                                activeTrackColor = EmeraldPrimary,
                                inactiveTrackColor = SurfaceContainerHigh
                            )
                        )
                    }

                    // 6. Work Environment Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Primary Daily Environment",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
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
                                val isSelected = workEnv == env
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) EmeraldPrimary else SurfaceContainerLow,
                                    border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else SurfaceContainerHigh),
                                    modifier = Modifier.clickable { workEnv = env }
                                ) {
                                    Text(
                                        text = env,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.White else OnSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 7. Emergency Kin Contact
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Emergency Kin (for SOS Dispatch)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )

                        OutlinedTextField(
                            value = emergencyName,
                            onValueChange = { emergencyName = it },
                            label = { Text("Emergency Contact Name") },
                            placeholder = { Text("e.g. Rajesh Sharma") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = emergencyPhone,
                            onValueChange = { emergencyPhone = it },
                            label = { Text("Emergency Contact Phone") },
                            placeholder = { Text("e.g. +91 98765 43210") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Create & Launch Button
                    Button(
                        onClick = {
                            val parsedAge = newAgeText.toIntOrNull() ?: 28
                            val computedAgeGroup = when {
                                parsedAge < 18 -> "under18"
                                parsedAge in 18..40 -> "18-40"
                                parsedAge in 41..60 -> "41-60"
                                else -> "60+"
                            }
                            val computedExertion = when {
                                outdoorHours >= 6.0f -> "high"
                                outdoorHours >= 3.0f -> "moderate"
                                else -> "low"
                            }
                            val clinicalSensitivities = mutableListOf<String>()
                            if (selectedDiseases.any { it.contains("Heat", ignoreCase = true) }) clinicalSensitivities.add("heat")
                            if (selectedDiseases.any { it.contains("Asthma", ignoreCase = true) || it.contains("Allergies", ignoreCase = true) }) clinicalSensitivities.add("respiratory")
                            if (selectedDiseases.any { it.contains("Cardiovascular", ignoreCase = true) }) clinicalSensitivities.add("cardiovascular")
                            if (clinicalSensitivities.isEmpty()) clinicalSensitivities.add("none")

                            val createdProfile = UserProfile(
                                firstName = newName.trim().ifEmpty { "Aarav" },
                                exactAge = parsedAge,
                                ageGroup = computedAgeGroup,
                                gender = newGender,
                                abhaId = newAbhaId.trim().ifEmpty { "91-4521-8832-1092" },
                                outdoorHours = outdoorHours,
                                outdoorActivityLevel = computedExertion,
                                workEnvironment = workEnv,
                                diseases = selectedDiseases.toList(),
                                sensitivities = clinicalSensitivities,
                                emergencyContactName = emergencyName.trim().ifEmpty { "Rajesh Sharma" },
                                emergencyContactPhone = emergencyPhone.trim().ifEmpty { "+91 98765 43210" },
                                emergencyRelation = emergencyRelation
                            )

                            viewModel.saveProfile(createdProfile)
                            Toast.makeText(context, "Account created & health profile calibrated!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Account & Access App", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
