package com.jogpal.app.features.matching

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jogpal.app.core.designsystem.components.NeonMeshCard

@Composable
fun NearbyRunnersScreen(
    uid: String,
    onNavigateBack: () -> Unit,
    onViewProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAnalyzeDialog by remember { mutableStateOf(false) }
    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LEADERBOARD",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // Top Hero Card: Streak & Analyze
                NeonMeshCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFC8FF00),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🔥 ", fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = "0 DAY STREAK",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "Start a new streak today",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF333333)
                                    )
                                }
                            }

                            // Top Right ANALYZE Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black)
                                    .clickable { showAnalyzeDialog = true }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "ANALYZE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Weekly Day Circles (M, T, W, T, F, S, S)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            daysOfWeek.forEach { day ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, Color.Black, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = day,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Large Bottom ANALYZE Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .background(Color.Black)
                                .clickable { showAnalyzeDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ANALYZE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFC8FF00),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section: YOUR RANKING
                Text(
                    text = "YOUR RANKING",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                NeonMeshCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFC8FF00),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Complete a verified run to appear on the leaderboard.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Section: GLOBAL - SEPTEMBER 2026
                Text(
                    text = "GLOBAL - SEPTEMBER 2026",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No rankings found for this month yet.",
                        fontSize = 14.sp,
                        color = Color(0xFF888888),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // Streak Analysis Dialog
    if (showAnalyzeDialog) {
        AlertDialog(
            onDismissRequest = { showAnalyzeDialog = false },
            containerColor = Color(0xFF181818),
            title = {
                Text(
                    text = "Streak Analysis",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC8FF00)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Current Streak: 0 Days",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Run at least 1.0 km daily to build and maintain your streak. Verified GPS tracking automatically unlocks your spot on the global leaderboards!",
                        color = Color(0xFFA0A0A0),
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAnalyzeDialog = false }) {
                    Text("Got it", color = Color(0xFFC8FF00), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
