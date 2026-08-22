package com.jogpal.app.features.run

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jogpal.app.core.designsystem.components.JogpalButton
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunSummaryScreen(
    summary: RunSummary,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Run Complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Great job on finishing your run.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Main Stats
        Text(
            text = String.format(Locale.getDefault(), "%.2f", summary.totalDistance),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "KILOMETERS",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray,
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Grid Stats
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    SummaryStatItem("TIME", formatDuration(summary.elapsedTimeSeconds), Modifier.weight(1f))
                    SummaryStatItem("AVG PACE", summary.averagePace, Modifier.weight(1f))
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.Gray.copy(alpha = 0.1f))
                Row(modifier = Modifier.fillMaxWidth()) {
                    SummaryStatItem("CALORIES", "${summary.calories} kcal", Modifier.weight(1f))
                    SummaryStatItem("START", timeFormatter.format(Date(summary.startTime)), Modifier.weight(1f))
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.Gray.copy(alpha = 0.1f))
                Row(modifier = Modifier.fillMaxWidth()) {
                    SummaryStatItem("FINISH", timeFormatter.format(Date(summary.finishTime)), Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        JogpalButton(
            text = "Done",
            onClick = onDone
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryStatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }
}
