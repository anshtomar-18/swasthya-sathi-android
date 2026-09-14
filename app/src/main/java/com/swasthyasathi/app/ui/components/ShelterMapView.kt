package com.swasthyasathi.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swasthyasathi.app.data.model.CoolingShelter
import com.swasthyasathi.app.ui.theme.*
import kotlin.math.sqrt

@Composable
fun ShelterMapView(
    shelters: List<CoolingShelter>,
    selectedShelter: CoolingShelter?,
    onShelterSelected: (CoolingShelter) -> Unit,
    modifier: Modifier = Modifier
) {
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    // Pulsing radar animation for user GPS location
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF2FBF6))
            .border(1.5.dp, SurfaceContainerHigh, RoundedCornerShape(20.dp))
    ) {
        // Interactive Map Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panX = (panX + dragAmount.x).coerceIn(-200f, 200f)
                        panY = (panY + dragAmount.y).coerceIn(-150f, 150f)
                    }
                }
                .pointerInput(shelters, zoomScale, panX, panY) {
                    detectTapGestures { tapOffset ->
                        // Check if tap hit any shelter marker
                        val w = size.width
                        val h = size.height
                        val centerX = w / 2f + panX
                        val centerY = h / 2f + panY

                        val tappedShelter = shelters.minByOrNull { shelter ->
                            val sx = centerX + (shelter.relX - 0.5f) * w * zoomScale
                            val sy = centerY + (shelter.relY - 0.5f) * h * zoomScale
                            val dist = sqrt((tapOffset.x - sx) * (tapOffset.x - sx) + (tapOffset.y - sy) * (tapOffset.y - sy))
                            dist
                        }

                        if (tappedShelter != null) {
                            val sx = centerX + (tappedShelter.relX - 0.5f) * w * zoomScale
                            val sy = centerY + (tappedShelter.relY - 0.5f) * h * zoomScale
                            val dist = sqrt((tapOffset.x - sx) * (tapOffset.x - sx) + (tapOffset.y - sy) * (tapOffset.y - sy))
                            if (dist < 50f) {
                                onShelterSelected(tappedShelter)
                            }
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f + panX
            val centerY = h / 2f + panY

            // 1. Draw Map Grid Roads
            val roadColor = Color(0xFFE2EBE5)
            val streetStroke = 8f * zoomScale

            // Horizontal primary avenues
            for (i in -2..2) {
                val y = centerY + i * 80f * zoomScale
                drawLine(
                    color = roadColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = streetStroke
                )
            }

            // Vertical primary avenues
            for (i in -2..2) {
                val x = centerX + i * 95f * zoomScale
                drawLine(
                    color = roadColor,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = streetStroke
                )
            }

            // Diagonal boulevard
            drawLine(
                color = roadColor.copy(alpha = 0.8f),
                start = Offset(centerX - 180f * zoomScale, centerY - 140f * zoomScale),
                end = Offset(centerX + 180f * zoomScale, centerY + 140f * zoomScale),
                strokeWidth = streetStroke * 1.3f
            )

            // 2. Green Shaded Tree Canopy Corridors (Zones >80% canopy)
            val canopyFill = Color(0xFFD1FAE5).copy(alpha = 0.7f)
            val canopyBorder = EmeraldPrimary.copy(alpha = 0.5f)

            // Canopy Park Block A (Central)
            drawRoundRect(
                color = canopyFill,
                topLeft = Offset(centerX - 60f * zoomScale, centerY - 50f * zoomScale),
                size = Size(120f * zoomScale, 90f * zoomScale),
                cornerRadius = CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = canopyBorder,
                topLeft = Offset(centerX - 60f * zoomScale, centerY - 50f * zoomScale),
                size = Size(120f * zoomScale, 90f * zoomScale),
                cornerRadius = CornerRadius(16f, 16f),
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
            )

            // Canopy Corridor Belt B (North-East shaded tree corridor)
            drawRoundRect(
                color = canopyFill.copy(alpha = 0.6f),
                topLeft = Offset(centerX + 40f * zoomScale, centerY - 95f * zoomScale),
                size = Size(100f * zoomScale, 45f * zoomScale),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Canopy Corridor Belt C (South-West shaded transit zone)
            drawRoundRect(
                color = canopyFill.copy(alpha = 0.6f),
                topLeft = Offset(centerX - 130f * zoomScale, centerY + 30f * zoomScale),
                size = Size(85f * zoomScale, 55f * zoomScale),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // 3. Shaded Walking Polyline to Selected Shelter
            val userOffset = Offset(centerX, centerY)
            if (selectedShelter != null) {
                val shelterX = centerX + (selectedShelter.relX - 0.5f) * w * zoomScale
                val shelterY = centerY + (selectedShelter.relY - 0.5f) * h * zoomScale
                val targetOffset = Offset(shelterX, shelterY)

                // Multi-segment shaded corridor path (walking through canopy)
                val midCorner = Offset(shelterX, centerY)
                val routePath = Path().apply {
                    moveTo(userOffset.x, userOffset.y)
                    lineTo(midCorner.x, midCorner.y)
                    lineTo(targetOffset.x, targetOffset.y)
                }

                // Shaded Glow backdrop
                drawPath(
                    path = routePath,
                    color = EmeraldPrimary.copy(alpha = 0.25f),
                    style = Stroke(width = 10f * zoomScale)
                )

                // Dashed high-vis route
                drawPath(
                    path = routePath,
                    color = EmeraldPrimary,
                    style = Stroke(
                        width = 4f * zoomScale,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f))
                    )
                )
            }

            // 4. Draw Shelter Pins
            shelters.forEach { shelter ->
                val isSelected = selectedShelter?.id == shelter.id
                val sx = centerX + (shelter.relX - 0.5f) * w * zoomScale
                val sy = centerY + (shelter.relY - 0.5f) * h * zoomScale

                val pinColor = when (shelter.category) {
                    "cooling" -> Color(0xFFEA580C) // Amber / Orange
                    "water" -> Color(0xFF0D9488)   // Teal
                    else -> Color(0xFFDC2626)      // Crimson
                }

                // If selected, draw highlight halo
                if (isSelected) {
                    drawCircle(
                        color = pinColor.copy(alpha = 0.25f),
                        radius = 24f * zoomScale,
                        center = Offset(sx, sy)
                    )
                    drawCircle(
                        color = pinColor,
                        radius = 18f * zoomScale,
                        center = Offset(sx, sy),
                        style = Stroke(width = 2.5f)
                    )
                }

                // Main Marker Circle
                drawCircle(
                    color = Color.White,
                    radius = (if (isSelected) 14f else 11f) * zoomScale,
                    center = Offset(sx, sy)
                )
                drawCircle(
                    color = pinColor,
                    radius = (if (isSelected) 11f else 8.5f) * zoomScale,
                    center = Offset(sx, sy)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.5f * zoomScale,
                    center = Offset(sx, sy)
                )
            }

            // 5. User GPS Position Marker (Center)
            // Animated Radar Pulse Ring
            drawCircle(
                color = Color(0xFF0284C7).copy(alpha = pulseAlpha),
                radius = pulseRadius * zoomScale,
                center = userOffset
            )
            // User Dot
            drawCircle(
                color = Color.White,
                radius = 9f * zoomScale,
                center = userOffset
            )
            drawCircle(
                color = Color(0xFF0284C7),
                radius = 6.5f * zoomScale,
                center = userOffset
            )
            drawCircle(
                color = Color.White,
                radius = 2f * zoomScale,
                center = userOffset
            )
        }

        // --- MAP HUD OVERLAYS ---

        // Top Left: Radar Live Badge
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            shape = RoundedCornerShape(100.dp),
            color = Color.White.copy(alpha = 0.92f),
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, SurfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(RiskLow)
                )
                Text(
                    text = "RADAR MAP ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = EmeraldPrimary
                )
            }
        }

        // Top Right: Pan / Zoom Controls
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Zoom In
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { zoomScale = (zoomScale + 0.25f).coerceAtMost(2.5f) },
                color = Color.White.copy(alpha = 0.92f),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, SurfaceContainerHigh)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Zoom Out
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.75f) },
                color = Color.White.copy(alpha = 0.92f),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, SurfaceContainerHigh)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Center on GPS
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        panX = 0f
                        panY = 0f
                        zoomScale = 1.0f
                    },
                color = Color.White.copy(alpha = 0.92f),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, SurfaceContainerHigh)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Center on GPS",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Bottom Banner: Selected Haven Floating Chip
        if (selectedShelter != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 3.dp,
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            color = when (selectedShelter.category) {
                                "cooling" -> Color(0xFFFFEDD5)
                                "water" -> Color(0xFFCCFBF1)
                                else -> Color(0xFFFEE2E2)
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (selectedShelter.category) {
                                        "cooling" -> Icons.Default.AcUnit
                                        "water" -> Icons.Default.WaterDrop
                                        else -> Icons.Default.LocalHospital
                                    },
                                    contentDescription = null,
                                    tint = when (selectedShelter.category) {
                                        "cooling" -> Color(0xFFEA580C)
                                        "water" -> Color(0xFF0D9488)
                                        else -> Color(0xFFDC2626)
                                    },
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = selectedShelter.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = OnSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "${selectedShelter.distanceMeters}m • ${selectedShelter.canopyPct}% Tree Canopy • -${selectedShelter.shadeReductionC}°C",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = EmeraldPrimary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = EmeraldPrimary,
                        modifier = Modifier.clickable { onShelterSelected(selectedShelter) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "ROUTE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
