package com.jogpal.app.features.sos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun SOSActiveStateScreen(
    locationData: LiveLocationData,
    trustedContactsCount: Int,
    isTestMode: Boolean = false,
    onDeactivate: () -> Unit,
    onViewPreview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B)) // Ultra dark background for maximum emergency contrast
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Banner Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF7F1D1D), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isTestMode) "🚨 SOS TEST ACTIVE" else "🚨 SOS ACTIVATED",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isTestMode) "Simulated Emergency State — No real messages sent" else "Emergency signal active & location broadcast live",
                    color = Color(0xFFFCA5A5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
            modifier = Modifier.fillMaxWidth()
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
                                .background(Color(0xFF22C55E), androidx.compose.foundation.shape.CircleShape)
                        )
                        Text(
                            text = "Location sharing ON",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "$trustedContactsCount trusted contacts notified",
                        color = Color(0xFFA1A1AA),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFF27272A))
                Spacer(modifier = Modifier.height(12.dp))

                // Grid stats
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("DISTANCE", color = Color(0xFF71717A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(String.format(Locale.getDefault(), "%.2f km", locationData.distanceKm), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("DURATION", color = Color(0xFF71717A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        val mins = locationData.durationSeconds / 60
                        val secs = locationData.durationSeconds % 60
                        Text(String.format(Locale.getDefault(), "%02d:%02d", mins, secs), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("BATTERY", color = Color(0xFF71717A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${locationData.batteryPercentage}%", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("GPS ACCURACY", color = Color(0xFF71717A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(if (locationData.isGpsAvailable) "±${locationData.accuracyMeters}m" else "N/A", color = if (locationData.isGpsAvailable) Color(0xFF22C55E) else Color(0xFFEF4444), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Location Status Box
        LiveLocationStatusView(locationData = locationData)

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onViewPreview,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F46)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("View SMS Alert Preview", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onDeactivate,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27272A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Deactivate SOS", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
