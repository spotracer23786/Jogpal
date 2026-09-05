package com.jogpal.app.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * High-contrast Neon Lime Green Card with Hexagonal/Dot Mesh Texture Pattern
 * as featured in reference UI screenshots.
 */
@Composable
fun NeonMeshCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFC8FF00), // Vibrant Neon Lime
    dotColor: Color = Color(0x22000000),         // Subtle dark mesh overlay
    shape: Shape = RoundedCornerShape(26.dp),
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    val borderModifier = if (borderWidth > 0.dp) {
        Modifier.border(borderWidth, borderColor, shape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .then(borderModifier)
            .then(clickableModifier)
    ) {
        // Hexagonal Mesh Texture Canvas Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = 1.8f
            val spacingX = 14f
            val spacingY = 12f
            var row = 0
            var y = spacingY / 2f

            while (y < size.height) {
                val offsetX = if (row % 2 == 1) spacingX / 2f else 0f
                var x = offsetX + spacingX / 2f
                while (x < size.width) {
                    drawCircle(
                        color = dotColor,
                        radius = radius,
                        center = androidx.compose.ui.geometry.Offset(x, y)
                    )
                    x += spacingX
                }
                y += spacingY
                row++
            }
        }

        // Inner Composable Content
        content()
    }
}
