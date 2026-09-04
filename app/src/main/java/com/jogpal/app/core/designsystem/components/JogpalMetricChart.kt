package com.jogpal.app.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jogpal.app.ui.theme.*

@Composable
fun JogpalMetricChart(
    title: String,
    currentValue: String,
    unit: String,
    badgeText: String,
    badgeColor: Color,
    dataPoints: List<Float> = listOf(14f, 18f, 15.8f, 17.6f, 21.3f),
    labels: List<String> = listOf("Jan 25", "July 25", "Jan 26", "Jul 26"),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, JogpalCardBorderLight, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = JogpalOnBackgroundLight
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = currentValue,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = JogpalOnBackgroundLight
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = unit,
                    fontSize = 14.sp,
                    color = JogpalMutedTextLight,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Drawing Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val maxVal = (dataPoints.maxOrNull() ?: 25f) + 3f
                    val minVal = (dataPoints.minOrNull() ?: 10f) - 3f

                    val points = dataPoints.mapIndexed { index, value ->
                        val x = (index.toFloat() / (dataPoints.size - 1)) * width
                        val y = height - ((value - minVal) / (maxVal - minVal)) * height
                        Pair(x, y)
                    }

                    // Typical range background band
                    drawRect(
                        color = Color(0xFFF2F7F4),
                        topLeft = androidx.compose.ui.geometry.Offset(0f, height * 0.35f),
                        size = androidx.compose.ui.geometry.Size(width, height * 0.35f)
                    )

                    // Line path
                    val path = Path().apply {
                        if (points.isNotEmpty()) {
                            moveTo(points[0].first, points[0].second)
                            for (i in 1 until points.size) {
                                val p0 = points[i - 1]
                                val p1 = points[i]
                                val cx = (p0.first + p1.first) / 2
                                cubicTo(cx, p0.second, cx, p1.second, p1.first, p1.second)
                            }
                        }
                    }

                    drawPath(
                        path = path,
                        color = JogpalPrimary,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw data nodes
                    points.forEach { (x, y) ->
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                        drawCircle(
                            color = JogpalPrimary,
                            radius = 3.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = JogpalMutedTextLight
                    )
                }
            }
        }
    }
}
