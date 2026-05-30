package com.example.schreibenaufdeutsch.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

data class ConfettiParticle(
    val id: Int,
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val speed: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 100,
    durationMillis: Int = 3000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    
    val colors = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFFFF6B6B), // Red-ish
        Color(0xFF4D96FF), // Blue-ish
        Color(0xFF6BCB77), // Green-ish
        Color(0xFF9B59B6)  // Purple
    )

    val particles = remember {
        List(particleCount) { i ->
            ConfettiParticle(
                id = i,
                x = Random.nextFloat(),
                y = -Random.nextFloat() * 2f, // Start above the screen
                size = Random.nextFloat() * 20f + 10f,
                color = colors.random(),
                rotation = Random.nextFloat() * 360f,
                speed = Random.nextFloat() * 0.5f + 0.2f,
                rotationSpeed = Random.nextFloat() * 5f + 2f
            )
        }
    }

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEach { particle ->
            val currentY = (particle.y + progress * 2.5f) * canvasHeight
            val currentRotation = particle.rotation + progress * 360f * particle.rotationSpeed
            
            // Only draw if within screen vertically (with some buffer)
            if (currentY > -50f && currentY < canvasHeight + 50f) {
                rotate(currentRotation, pivot = Offset(particle.x * canvasWidth, currentY)) {
                    drawRect(
                        color = particle.color,
                        topLeft = Offset(particle.x * canvasWidth - particle.size / 2, currentY),
                        size = Size(particle.size, particle.size / 2)
                    )
                }
            }
        }
    }
}
