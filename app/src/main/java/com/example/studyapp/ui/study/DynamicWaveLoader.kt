package com.example.studyapp.ui.study

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun DynamicWaveLoader(modifier: Modifier = Modifier, text: String = "Processing...") {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "wave_loader")
        val wavePhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "wave_phase"
        )

        val waveColor = MaterialTheme.colorScheme.primary

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerY = canvasHeight / 2

            val points = 50
            val spacing = canvasWidth / (points - 1)

            for (i in 0 until points) {
                val x = i * spacing
                // Create a wave effect combined with a second wave for more dynamic feel
                val y1 = sin(wavePhase + (i * 0.2f)) * 15f
                val y2 = sin(wavePhase * 1.5f + (i * 0.1f)) * 10f
                val finalY = centerY + y1 + y2

                // Draw vertical lines (particles) that go up to the wave
                drawLine(
                    color = waveColor,
                    start = Offset(x, centerY),
                    end = Offset(x, finalY),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round,
                    alpha = 0.5f + (sin(wavePhase + i * 0.1f) + 1f) / 4f // Pulsing alpha
                )

                // Draw dots at the peak
                drawCircle(
                    color = waveColor,
                    radius = 3f,
                    center = Offset(x, finalY)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pulsing text
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "text_alpha"
        )

        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            style = MaterialTheme.typography.titleMedium
        )
    }
}
