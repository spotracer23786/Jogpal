package com.jogpal.app.features.matching.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jogpal.app.core.designsystem.components.JogpalButton
import com.jogpal.app.features.matching.RunnerDisplayModel
import com.jogpal.app.ui.theme.*

@Composable
fun RunnerCard(
    runner: RunnerDisplayModel,
    onViewProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = JogpalPrimary.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(24.dp))
            .background(JogpalSurfaceDark)
            .border(1.dp, JogpalCardBorderDark, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(JogpalPillBgLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = runner.profile.name.take(1).uppercase(),
                            fontSize = 20.sp,
                            color = JogpalPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = runner.profile.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = JogpalOnBackgroundLight
                        )
                        Text(
                            text = runner.profile.experienceLevel ?: "Runner",
                            fontSize = 12.sp,
                            color = JogpalMutedTextLight
                        )
                    }
                }

                // Glowing Neon Distance Tag (from Ref UI 2)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(JogpalTertiary.copy(alpha = 0.2f))
                        .border(1.dp, JogpalPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${String.format("%.1f", runner.distanceKm)} Km",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = JogpalPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoTag(label = runner.profile.runningGoal ?: "Goal")
                InfoTag(label = runner.profile.preferredDistance ?: "Distance")
                InfoTag(label = runner.profile.preferredPace ?: "Pace")
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(JogpalPrimary)
                    .clickable { onViewProfile() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View Profile",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.NorthEast,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoTag(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(JogpalPillBgLight)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = JogpalOnBackgroundLight
        )
    }
}

