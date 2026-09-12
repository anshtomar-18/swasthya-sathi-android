package com.swasthyasathi.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.data.model.DisasterWarning
import com.swasthyasathi.app.ui.theme.*

@Composable
fun DisasterCard(
    warnings: List<DisasterWarning>,
    locationName: String,
    modifier: Modifier = Modifier
) {
    val hasCritical = warnings.any { it.severity.equals("critical", ignoreCase = true) }
    val hasHigh = warnings.any { it.severity.equals("high", ignoreCase = true) }

    val statusBadgeColor = when {
        hasCritical -> RiskCritical
        hasHigh -> RiskHigh
        warnings.any { it.severity.equals("moderate", ignoreCase = true) } -> RiskModerate
        else -> PrimaryTeal
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = statusBadgeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "DISASTER WARNINGS & ADVISORIES",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                        Text(
                            text = "IMD & CPCB threshold alerts for $locationName",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppOutline
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = statusBadgeColor
                ) {
                    val activeCount = warnings.count { !it.severity.equals("normal", ignoreCase = true) }
                    Text(
                        text = if (activeCount > 0) "$activeCount HAZARD ALERTS" else "BASELINE NORMAL",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Warnings List
            warnings.forEach { warning ->
                val (itemBg, itemBorder, badgeBg) = when (warning.severity.lowercase()) {
                    "critical" -> Triple(RiskCriticalBg, Color(0xFFFECACA), RiskCritical)
                    "high" -> Triple(RiskHighBg, Color(0xFFFED7AA), RiskHigh)
                    "moderate" -> Triple(RiskModerateBg, Color(0xFFFDE68A), RiskModerate)
                    else -> Triple(RiskLowBg, Color(0xFFA7F3D0), PrimaryTeal)
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = itemBg,
                    border = BorderStroke(1.dp, itemBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = warning.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = badgeBg
                            ) {
                                Text(
                                    text = warning.severity.uppercase(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = warning.shortExplanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            lineHeight = 16.sp
                        )

                        HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "ACTION:",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppOutline
                            )
                            Text(
                                text = warning.actionGuidance,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = OnSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
