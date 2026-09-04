package com.jogpal.app.features.ghost

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.jogpal.app.data.ghost.GhostRepository
import com.jogpal.app.domain.ghost.GhostRun
import com.jogpal.app.ui.theme.JogpalPrimary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostRunSelectScreen(
    onNavigateBack: () -> Unit,
    onSelectGhostRun: (String) -> Unit,
    ghostRepository: GhostRepository = remember { GhostRepository() }
) {
    val ghostRuns by ghostRepository.getEligibleGhostRuns().collectAsState(initial = GhostRepository.getMockGhostRuns())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👻 ", fontSize = 20.sp)
                            Text(
                                "Ghost Mode",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            "Race against your past self.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF161C24)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(JogpalPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = JogpalPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            "Select a Previous Run",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            "Choose a past record to unleash your ghost opponent.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(ghostRuns) { ghost ->
                    GhostRunCard(
                        ghost = ghost,
                        onRaceClick = { onSelectGhostRun(ghost.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun GhostRunCard(
    ghost: GhostRun,
    onRaceClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.dp,
                if (ghost.isPersonalBest) JogpalPrimary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(24.dp)
            )
            .clickable { onRaceClick() },
        color = Color(0xFF12161E)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ghost.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        if (ghost.isPersonalBest) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = JogpalPrimary.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = JogpalPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "PB",
                                        color = JogpalPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = ghost.dateFormatted,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Route Thumbnail Canvas
                Box(
                    modifier = Modifier
                        .size(60.dp, 50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E2634)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        val path = Path().apply {
                            val w = size.width
                            val h = size.height
                            moveTo(w * 0.1f, h * 0.8f)
                            cubicTo(w * 0.3f, h * 0.2f, w * 0.7f, h * 0.9f, w * 0.9f, h * 0.3f)
                        }
                        drawPath(
                            path = path,
                            color = JogpalPrimary,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stat Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatPill(
                    label = "Distance",
                    value = String.format(Locale.US, "%.2f km", ghost.distanceKm)
                )
                StatPill(
                    label = "Duration",
                    value = ghost.formattedTime
                )
                StatPill(
                    label = "Avg Pace",
                    value = ghost.pace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRaceClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JogpalPrimary,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    Icons.Default.DirectionsRun,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Race This Run",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun StatPill(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
