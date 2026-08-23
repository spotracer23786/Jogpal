package com.jogpal.app.features.run

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jogpal.app.core.designsystem.components.JogpalButton
import com.jogpal.app.core.designsystem.components.SelectableCard

enum class SoloGoalType {
    FREE, DISTANCE, DURATION
}

@Composable
fun SoloSetupDialog(
    onDismiss: () -> Unit,
    onStartRun: (goalType: SoloGoalType, value: Double) -> Unit
) {
    var goalType by remember { mutableStateOf(SoloGoalType.FREE) }
    var selectedDistance by remember { mutableDoubleStateOf(5.0) }
    var selectedDuration by remember { mutableDoubleStateOf(30.0) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Start Solo Run",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Select your target goal type",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Goal selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoalChip(text = "Free", selected = goalType == SoloGoalType.FREE) { goalType = SoloGoalType.FREE }
                    GoalChip(text = "Distance", selected = goalType == SoloGoalType.DISTANCE) { goalType = SoloGoalType.DISTANCE }
                    GoalChip(text = "Time", selected = goalType == SoloGoalType.DURATION) { goalType = SoloGoalType.DURATION }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Detail settings
                when (goalType) {
                    SoloGoalType.FREE -> {
                        Text(
                            text = "Run as long and far as you want. Stats will track normally.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.height(80.dp)
                        )
                    }
                    SoloGoalType.DISTANCE -> {
                        Column(modifier = Modifier.height(80.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%.1f km Target", selectedDistance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Slider(
                                value = selectedDistance.toFloat(),
                                onValueChange = { selectedDistance = it.toDouble() },
                                valueRange = 1f..21f,
                                steps = 20,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    SoloGoalType.DURATION -> {
                        Column(modifier = Modifier.height(80.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%.0f minutes Target", selectedDuration),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Slider(
                                value = selectedDuration.toFloat(),
                                onValueChange = { selectedDuration = it.toDouble() },
                                valueRange = 5f..120f,
                                steps = 23,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                JogpalButton(
                    text = "Go Running",
                    onClick = {
                        val value = when (goalType) {
                            SoloGoalType.FREE -> 0.0
                            SoloGoalType.DISTANCE -> selectedDistance
                            SoloGoalType.DURATION -> selectedDuration
                        }
                        onStartRun(goalType, value)
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun RowScope.GoalChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.weight(1f).height(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
