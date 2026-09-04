package com.jogpal.app.features.sos.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jogpal.app.features.sos.LiveLocationData

@Composable
fun LiveLocationStatusView(
    locationData: LiveLocationData,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .alpha(pulseAlpha)
                            .background(Color(0xFF38BDF8), CircleShape)
                    )
                    Text(
                        text = "LIVE LOCATION",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Updated ${locationData.lastUpdatedSecondsAgo}s ago",
                    color = Color(0xFFA1A1AA),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!locationData.isGpsAvailable) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF451A1A), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF991B1B), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠️ Unable to obtain precise GPS location",
                        color = Color(0xFFFCA5A5),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Current Position", color = Color(0xFFA1A1AA), fontSize = 12.sp)
                        Text("${locationData.latitude}° N, ${locationData.longitude}° W", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Accuracy Radius", color = Color(0xFFA1A1AA), fontSize = 12.sp)
                        Text("±${locationData.accuracyMeters} meters", color = Color(0xFF4ADE80), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Movement Status", color = Color(0xFFA1A1AA), fontSize = 12.sp)
                        Text(locationData.movementStatus, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
