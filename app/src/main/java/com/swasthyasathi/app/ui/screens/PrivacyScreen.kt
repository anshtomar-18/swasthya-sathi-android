package com.swasthyasathi.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.ui.theme.*

@Composable
fun PrivacyScreen(
    modifier: Modifier = Modifier
) {
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
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "PRIVACY ARCHITECTURE",
                style = MaterialTheme.typography.labelLarge,
                color = AppOutline
            )
            Text(
                text = "Zero Medical Data Stored",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = OnSurface
            )
            Text(
                text = "SwasthyaSathi AI is architected from the ground up for strict privacy preservation and data minimization under SIH 26181.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                lineHeight = 20.sp
            )
        }

        // 4 Core Guarantees
        PrivacyCard(
            icon = Icons.Default.Lock,
            title = "1. Zero Medical Records",
            desc = "No hospital records, diagnostic codes, prescription histories, or clinical files are ever requested, ingested, or stored on any server."
        )

        PrivacyCard(
            icon = Icons.Default.Storage,
            title = "2. Local-First Processing",
            desc = "Your biological parameters (age group, outdoor exposure, sensitivity flags) remain in local on-device sandbox storage. Calculations run locally on your phone."
        )

        PrivacyCard(
            icon = Icons.Default.Security,
            title = "3. Deterministic Safety",
            desc = "Risk tiers are computed via auditable mathematical formulas matching IMD and CPCB guidelines, eliminating non-deterministic black-box hallucinations."
        )

        PrivacyCard(
            icon = Icons.Default.Shield,
            title = "4. Emergency Simulation Safeguards",
            desc = "The rapid SOS flow clearly operates in a transparent demonstration sandbox, simulating coordinate broadcasts without unauthorized third-party billing."
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun PrivacyCard(icon: ImageVector, title: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        border = BorderStroke(1.dp, SurfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryTeal,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    lineHeight = 19.sp
                )
            }
        }
    }
}
