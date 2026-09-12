package com.swasthyasathi.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.data.model.RiskEngineResult
import com.swasthyasathi.app.data.model.RiskLevel
import com.swasthyasathi.app.ui.theme.*

@Composable
fun RiskCard(
    result: RiskEngineResult,
    isOffline: Boolean = false,
    modifier: Modifier = Modifier
) {
    val level = result.riskLevel

    val (cardBg, cardBorder, badgeColor, textColor) = when (level) {
        RiskLevel.Critical -> Quad(RiskCriticalBg, Color(0xFFFECACA), RiskCritical, Color(0xFF7F1D1D))
        RiskLevel.High -> Quad(RiskHighBg, Color(0xFFFED7AA), RiskHigh, Color(0xFF7C2D12))
        RiskLevel.Moderate -> Quad(RiskModerateBg, Color(0xFFFDE68A), RiskModerate, Color(0xFF78350F))
        RiskLevel.Low -> Quad(RiskLowBg, Color(0xFFA7F3D0), RiskLow, Color(0xFF064E3B))
    }

    // 60 FPS Breathing Pulse for High/Critical Alerts
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(
            width = if (level == RiskLevel.Critical || level == RiskLevel.High) 2.dp else 1.2.dp,
            color = if (level == RiskLevel.Critical) badgeColor.copy(alpha = glowAlpha) else cardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row with Animated Tier Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                            .graphicsLayer {
                                if (level == RiskLevel.Critical) {
                                    scaleX = glowAlpha * 1.3f
                                    scaleY = glowAlpha * 1.3f
                                }
                            }
                    )
                    Text(
                        text = "HEALTH RISK ASSESSMENT",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppOutline,
                        fontSize = 11.sp
                    )
                }

                AnimatedContent(
                    targetState = result.level,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> -height } + fadeOut())
                    },
                    label = "riskBadgeAnim"
                ) { targetLevel ->
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = badgeColor
                    ) {
                        Text(
                            text = "${targetLevel.uppercase()} RISK",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Primary Driver with smooth transition
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "CLINICAL PRIMARY DRIVER:",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppOutline
                )
                AnimatedContent(
                    targetState = result.primaryDriver,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                    label = "driverAnim"
                ) { driverText ->
                    Text(
                        text = driverText.ifEmpty { "Evaluating personal sensitivities against environmental telemetry..." },
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = textColor,
                        lineHeight = 21.sp
                    )
                }
            }

            // Prescribed Preventive Protocols with interactive checkcards
            if (result.recommendations.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PRESCRIBED PREVENTIVE ACTIONS:",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppOutline
                    )

                    result.recommendations.forEach { rec ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.90f),
                            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                            modifier = Modifier.bounceClick()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(top = 1.dp)
                                )
                                Text(
                                    text = rec,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurface,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Card Footer
            HorizontalDivider(color = Color.Black.copy(alpha = 0.08f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Computed live by RiskEngine.kt",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryTeal
                    )
                }
                Text(
                    text = if (isOffline) "On-Device Local Cache" else "Live Telemetry Stream",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppOutline
                )
            }
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
