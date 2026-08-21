package com.jogpal.app.features.run

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jogpal.app.core.designsystem.components.JogpalButton
import com.jogpal.app.domain.run.RunStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunDetailScreen(
    runId: String,
    onNavigateBack: () -> Unit,
    onStartRun: (String) -> Unit,
    viewModel: RunDetailViewModel = viewModel(factory = RunDetailViewModelFactory()),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(runId) {
        viewModel.loadRunDetails(runId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Run Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Back", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading && uiState.plan == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null && uiState.plan == null) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadRunDetails(runId) }) {
                        Text("Retry")
                    }
                }
            }
        } else {
            val plan = uiState.plan
            val partner = uiState.partnerProfile
            
            if (plan != null) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val headerText = when(plan.status) {
                        RunStatus.PENDING -> "RUN INVITATION"
                        RunStatus.ACCEPTED -> "RUN CONFIRMED ✓"
                        RunStatus.ACTIVE -> "RUN IN PROGRESS ●"
                        RunStatus.COMPLETED -> "RUN COMPLETED ✓"
                        RunStatus.CANCELLED -> "RUN CANCELLED"
                        RunStatus.DECLINED -> "INVITATION DECLINED"
                    }
                    
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (plan.status == RunStatus.ACCEPTED || plan.status == RunStatus.COMPLETED) 
                            MaterialTheme.colorScheme.primary else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = plan.title.ifBlank { "Planned Run" },
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "with ${partner?.name ?: "Partner"}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    DetailCard(
                        date = plan.date,
                        time = plan.startTime,
                        distance = "${plan.distanceKm} km",
                        pace = plan.pace
                    )
                    
                    if (plan.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("NOTES", style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp
                        ) {
                            Text(
                                text = plan.notes,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    uiState.successMessage?.let {
                        Text(
                            text = it,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    uiState.error?.let {
                        Text(
                            text = it,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    val isReceiver = plan.partnerUid == uiState.currentUid
                    
                    if (uiState.isActionLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        when (plan.status) {
                            RunStatus.PENDING -> {
                                if (isReceiver) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Button(
                                            onClick = { viewModel.updateStatus(RunStatus.ACCEPTED) },
                                            modifier = Modifier.weight(1f).height(56.dp),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text("Accept")
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        OutlinedButton(
                                            onClick = { viewModel.updateStatus(RunStatus.DECLINED) },
                                            modifier = Modifier.weight(1f).height(56.dp),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text("Decline")
                                        }
                                    }
                                } else {
                                    JogpalButton(
                                        text = "Cancel Invitation",
                                        onClick = { viewModel.updateStatus(RunStatus.CANCELLED) }
                                    )
                                }
                            }
                            RunStatus.ACCEPTED -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    JogpalButton(
                                        text = "Start Run",
                                        onClick = { 
                                            viewModel.updateStatus(RunStatus.ACTIVE)
                                            onStartRun(plan.id)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Button(
                                            onClick = { viewModel.updateStatus(RunStatus.COMPLETED) },
                                            modifier = Modifier.weight(1f).height(56.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.White)
                                        ) {
                                            Text("Mark Completed")
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        OutlinedButton(
                                            onClick = { viewModel.updateStatus(RunStatus.CANCELLED) },
                                            modifier = Modifier.weight(1f).height(56.dp),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text("Cancel")
                                        }
                                    }
                                }
                            }
                            RunStatus.ACTIVE -> {
                                JogpalButton(
                                    text = "Return to Live Map",
                                    onClick = { onStartRun(plan.id) }
                                )
                            }
                            else -> {
                                JogpalButton(text = "Back to Home", onClick = onNavigateBack)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailCard(date: String, time: String, distance: String, pace: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            DetailItem("Date", date)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.1f))
            DetailItem("Time", time)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.1f))
            DetailItem("Distance", distance)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.1f))
            DetailItem("Pace", pace)
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
