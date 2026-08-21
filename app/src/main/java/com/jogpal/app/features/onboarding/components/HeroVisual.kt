package com.jogpal.app.features.onboarding.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun HeroVisual(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2

            // 1. Draw Subtle Grid / Connection Nodes
            for (i in 0..5) {
                val x = (centerX - 200) + (i * 80)
                for (j in 0..5) {
                    val y = (centerY - 200) + (j * 80)
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.05f * pulse),
                        radius = 2.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }

            // 2. Draw Connected Paths
            val path = Path().apply {
                moveTo(centerX - 150f, centerY + 100f)
                quadraticBezierTo(
                    centerX - 50f, centerY - 150f,
                    centerX + 150f, centerY - 50f
                )
                quadraticBezierTo(
                    centerX + 50f, centerY + 150f,
                    centerX - 100f, centerY + 50f
                )
            }

            // Path Glow
            drawPath(
                path = path,
                color = primaryColor.copy(alpha = 0.1f),
                style = Stroke(
                    width = 8.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 40f), phase)
                )
            )

            // Main Path
            drawPath(
                path = path,
                color = primaryColor.copy(alpha = 0.3f),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 20f), phase)
                )
            )

            // 3. Draw Moving Location Indicators (Joggers)
            // Node 1
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor, Color.Transparent),
                    center = Offset(centerX + 100f, centerY - 80f),
                    radius = 20.dp.toPx() * pulse
                ),
                radius = 20.dp.toPx() * pulse,
                center = Offset(centerX + 100f, centerY - 80f)
            )
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = Offset(centerX + 100f, centerY - 80f)
            )

            // Node 2
            drawCircle(
                color = primaryColor.copy(alpha = 0.2f),
                radius = 6.dp.toPx() * pulse,
                center = Offset(centerX - 80f, centerY + 40f)
            )
            drawCircle(
                color = primaryColor,
                radius = 3.dp.toPx(),
                center = Offset(centerX - 80f, centerY + 40f)
            )
        }
    }
}
