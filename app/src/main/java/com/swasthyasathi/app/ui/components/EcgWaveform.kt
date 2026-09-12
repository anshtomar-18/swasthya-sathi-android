package com.swasthyasathi.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.swasthyasathi.app.ui.theme.PrimaryTeal
import com.swasthyasathi.app.ui.theme.PrimaryTealFixed

/**
 * 60 FPS Optimized Animated ECG Waveform
 * - Zero allocations inside draw loop for 60/120 FPS buttery smooth rendering
 * - Dual-layer neon glow effect + leading pulse dot
 * - Heart rate modulated velocity
 */
@Composable
fun EcgWaveform(
    modifier: Modifier = Modifier,
    lineColor: Color = PrimaryTeal,
    glowColor: Color = PrimaryTealFixed,
    bpm: Int = 74
) {
    // Dynamic cycle duration inversely proportional to BPM
    val durationMs = ((60_000f / bpm.coerceIn(50, 140)) * 1.8f).toInt()

    val infiniteTransition = rememberInfiniteTransition(label = "ecg_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ecg_phase"
    )

    // Reuse a single Path instance to prevent Garbage Collection frame drops
    val reusablePath = remember { Path() }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2f
        val cycleWidth = width / 2.2f

        reusablePath.reset()
        val offsetX = (phase * cycleWidth) % cycleWidth

        var currentX = -cycleWidth + offsetX
        reusablePath.moveTo(currentX, midY)

        var leadDotX = 0f
        var leadDotY = midY

        while (currentX < width + cycleWidth) {
            val x = currentX
            reusablePath.lineTo(x + cycleWidth * 0.15f, midY)
            // P wave
            reusablePath.lineTo(x + cycleWidth * 0.20f, midY - height * 0.22f)
            reusablePath.lineTo(x + cycleWidth * 0.25f, midY)
            // Q wave
            reusablePath.lineTo(x + cycleWidth * 0.28f, midY + height * 0.14f)
            // R spike
            reusablePath.lineTo(x + cycleWidth * 0.32f, midY - height * 0.48f)
            // S wave
            reusablePath.lineTo(x + cycleWidth * 0.36f, midY + height * 0.38f)
            // Base recovery
            reusablePath.lineTo(x + cycleWidth * 0.40f, midY)
            // T wave
            reusablePath.lineTo(x + cycleWidth * 0.55f, midY - height * 0.28f)
            reusablePath.lineTo(x + cycleWidth * 0.65f, midY)
            reusablePath.lineTo(x + cycleWidth, midY)

            // Track leading pulse dot position
            if (currentX + cycleWidth * 0.32f in 0f..width) {
                leadDotX = currentX + cycleWidth * 0.32f
                leadDotY = midY - height * 0.48f
            }

            currentX += cycleWidth
        }

        // 1. Outer Neon Glow Layer (60 FPS hardware accelerated)
        drawPath(
            path = reusablePath,
            color = glowColor.copy(alpha = 0.45f),
            style = Stroke(
                width = 5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 2. Core Crisp Trace
        drawPath(
            path = reusablePath,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 3. Leading Pulse Dot
        if (leadDotX > 0f) {
            drawCircle(
                color = glowColor,
                radius = 4.dp.toPx(),
                center = Offset(leadDotX, leadDotY)
            )
            drawCircle(
                color = lineColor,
                radius = 2.dp.toPx(),
                center = Offset(leadDotX, leadDotY)
            )
        }
    }
}
