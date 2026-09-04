package com.jogpal.app.features.ghost

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jogpal.app.data.ghost.GhostRepository
import com.jogpal.app.domain.ghost.GhostRun
import com.jogpal.app.ui.theme.JogpalPrimary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostRunFinishScreen(
    ghostId: String,
    userDurationSeconds: Long,
    userDistanceKm: Double,
    isWinner: Boolean,
    onRunAgain: () -> Unit,
    onDone: () -> Unit,
    ghostRepository: GhostRepository = remember { GhostRepository() }
) {
    val context = LocalContext.current
    val ghostRuns by ghostRepository.getEligibleGhostRuns().collectAsState(initial = GhostRepository.getMockGhostRuns())
    val ghost = remember(ghostId, ghostRuns) {
        ghostRepository.getGhostRunById(ghostId, ghostRuns)
    }

    var showShareDialog by remember { mutableStateOf(false) }

    val ghostDuration = ghost?.durationSeconds ?: 1961L
    val timeDiffSeconds = Math.abs(ghostDuration - userDurationSeconds)
    val timeDiffMinutes = timeDiffSeconds / 60
    val timeDiffSecs = timeDiffSeconds % 60
    val timeDiffStr = String.format(Locale.US, "%d:%02d", timeDiffMinutes, timeDiffSecs)

    val todayMinutes = userDurationSeconds / 60
    val todaySecs = userDurationSeconds % 60
    val todayTimeStr = String.format(Locale.US, "%02d:%02d", todayMinutes, todaySecs)

    val percentImprovement = if (ghostDuration > 0) {
        ((ghostDuration - userDurationSeconds).toDouble() / ghostDuration * 100.0).coerceAtLeast(0.0)
    } else 0.0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Result Header Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (isWinner) JogpalPrimary.copy(alpha = 0.2f) else Color(0xFFFF5252).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isWinner) "🏆" else "👻",
                    fontSize = 40.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isWinner) "YOU BEAT YOURSELF" else "Your past self wins this time.",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = if (isWinner) "$timeDiffStr faster than your previous run!" else "You were $timeDiffStr behind your target. Keep pushing!",
                fontSize = 14.sp,
                color = if (isWinner) JogpalPrimary else Color.Gray
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Comparison Summary Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF131A24),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Previous", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = ghost?.formattedTime ?: "32:41",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Today", fontSize = 12.sp, color = JogpalPrimary, fontWeight = FontWeight.Bold)
                            Text(
                                text = todayTimeStr,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = JogpalPrimary
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.1f))

                    // Detailed Metrics Grid
                    MetricDetailRow("Distance", String.format(Locale.US, "%.2f km", userDistanceKm))
                    MetricDetailRow("Time Difference", "$timeDiffStr ${if (isWinner) "faster" else "slower"}")
                    MetricDetailRow("Pace Boost", String.format(Locale.US, "%.1f%%", percentImprovement))
                    if (isWinner) {
                        MetricDetailRow("Personal Best", "🏆 NEW RECORD!")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preview Share Card
            GhostShareCard(
                distanceKm = userDistanceKm,
                todayTimeStr = todayTimeStr,
                timeDiffStr = timeDiffStr,
                isWinner = isWinner
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRunAgain,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Run Again", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        shareGhostCardResult(context, userDistanceKm, todayTimeStr, timeDiffStr)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JogpalPrimary,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Result", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Done", color = Color.Gray, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MetricDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
