package com.jogpal.app.features.sos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jogpal.app.features.sos.LiveLocationData
import java.util.Locale

@Composable
fun SOSNotificationPreviewDialog(
    locationData: LiveLocationData,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF18181B),
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF38BDF8), androidx.compose.foundation.shape.CircleShape)
                    )
                    Text(
                        text = "SMS Notification Preview",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = "Simulated view of what trusted contacts receive",
                    color = Color(0xFFA1A1AA),
                    fontSize = 11.sp
                )
            }
        },
        text = {
            // Simulated SMS Message Box
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F46)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📱 SMS Message",
                        color = Color(0xFF71717A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🚨 Jogpal Safety Alert",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "“Your trusted contact has activated SOS during a Jogpal session.”",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("📍 Location: ${locationData.latitude}° N, ${locationData.longitude}° W", color = Color(0xFFE4E4E7), fontSize = 11.sp)
                        Text("🏃 Jog status: Active", color = Color(0xFFE4E4E7), fontSize = 11.sp)
                        val mins = locationData.durationSeconds / 60
                        val secs = locationData.durationSeconds % 60
                        Text(String.format(Locale.getDefault(), "⏱️ Duration: %02d:%02d", mins, secs), color = Color(0xFFE4E4E7), fontSize = 11.sp)
                        Text(String.format(Locale.getDefault(), "📏 Distance: %.2f km", locationData.distanceKm), color = Color(0xFFE4E4E7), fontSize = 11.sp)
                        Text("🕐 Last update: ${locationData.lastUpdatedSecondsAgo} sec ago", color = Color(0xFFE4E4E7), fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                            .padding( vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("View Live Location", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F3F46)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close Preview", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}
