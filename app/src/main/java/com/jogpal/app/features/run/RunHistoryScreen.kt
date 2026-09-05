package com.jogpal.app.features.run

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jogpal.app.core.designsystem.components.NeonMeshCard

@Composable
fun RunHistoryScreen(
    onNavigateBack: () -> Unit,
    onRunClick: (String) -> Unit,
    viewModel: RunHistoryViewModel = viewModel(factory = RunHistoryViewModelFactory()),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val runs = uiState.runs

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
            // Top App Bar
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
                        text = "HISTORY",
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
                // Card 1: YOUR RUNNING JOURNEY
                NeonMeshCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFC8FF00),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "YOUR RUNNING JOURNEY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            JourneyMetricItem(value = "0.1", label = "KM")
                            JourneyMetricItem(value = "${if (runs.isNotEmpty()) runs.size else 6}", label = "RUNS")
                            JourneyMetricItem(value = "0:02", label = "TIME")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 1: TODAY
                Text(
                    text = "TODAY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFC8FF00),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Card 2: TODAY'S MORNING SESSION
                NeonMeshCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFC8FF00),
                    shape = RoundedCornerShape(24.dp),
                    onClick = { if (runs.isNotEmpty()) onRunClick(runs.first().id) else onRunClick("today_run") }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "SEP 1, 2026",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF333333),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "MORNING SESSION",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            JourneyMetricItem(value = "0.00", label = "KM")
                            JourneyMetricItem(value = "00:09", label = "TIME")
                            JourneyMetricItem(value = "--:--", label = "PACE")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 2: YESTERDAY
                Text(
                    text = "YESTERDAY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFC8FF00),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Card 3: YESTERDAY'S SOLO RUN
                NeonMeshCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFC8FF00),
                    shape = RoundedCornerShape(24.dp),
                    onClick = { if (runs.size > 1) onRunClick(runs[1].id) else onRunClick("yesterday_run") }
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
                            Text(
                                text = "AUG 31, 2026",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF333333),
                                letterSpacing = 0.5.sp
                            )

                            // SOLO Pill Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "SOLO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFC8FF00)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SOLO RUN",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            JourneyMetricItem(value = "0.01", label = "KM")
                            JourneyMetricItem(value = "00:40", label = "TIME")
                            JourneyMetricItem(value = "--:--", label = "PACE")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Bottom Action Button: VIEW DETAILS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(Color(0xFFC8FF00))
                        .clickable {
                            if (runs.isNotEmpty()) {
                                onRunClick(runs.first().id)
                            } else {
                                onRunClick("demo_run")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VIEW DETAILS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun JourneyMetricItem(value: String, label: String) {
    Column {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black,
            letterSpacing = 0.5.sp
        )
    }
}
