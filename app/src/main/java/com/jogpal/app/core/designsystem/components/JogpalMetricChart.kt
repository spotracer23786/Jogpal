package com.jogpal.app.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    dataPoints: List<Float> = emptyList(),
    labels: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(JogpalSurfaceDark)
            .border(1.dp, JogpalCardBorderDark, RoundedCornerShape(24.dp))
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
                    fontSize = 15.sp,
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

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = currentValue,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = JogpalOnBackgroundLight
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = unit,
                    fontSize = 13.sp,
                    color = JogpalMutedTextLight,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (dataPoints.isEmpty() || dataPoints.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(JogpalPillBgLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Complete runs to unlock live pace analytics",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = JogpalMutedTextLight
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val maxVal = (dataPoints.maxOrNull() ?: 10f) + 1f
                        val minVal = (dataPoints.minOrNull() ?: 0f) - 1f

                        val points = dataPoints.mapIndexed { index, value ->
                            val x = (index.toFloat() / (dataPoints.size - 1)) * width
                            val y = height - ((value - minVal) / (maxVal - minVal).coerceAtLeast(1f)) * height
                            Pair(x, y)
                        }

                        drawRect(
                            color = Color(0xFF1E2805),
                            topLeft = androidx.compose.ui.geometry.Offset(0f, height * 0.3f),
                            size = androidx.compose.ui.geometry.Size(width, height * 0.4f)
                        )

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

                        points.forEach { (x, y) ->
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                            drawCircle(
                                color = JogpalPrimary,
                                radius = 2.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                        }
                    }
                }

                if (labels.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
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
    }
}

