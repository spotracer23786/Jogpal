package com.jogpal.app.features.matching

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jogpal.app.core.designsystem.components.JogpalButton
import com.jogpal.app.core.designsystem.components.JogpalLogo
import com.jogpal.app.domain.matching.RequestStatus
import com.jogpal.app.features.onboarding.components.HeroVisual

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunnerProfileScreen(
    targetUid: String,
    onNavigateBack: () -> Unit,
    onPlanRun: (String) -> Unit,
    onChat: (String) -> Unit,
    viewModel: RunnerProfileViewModel = viewModel(factory = RunnerProfileViewModelFactory()),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(targetUid) {
        viewModel.loadRunnerProfile(targetUid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Runner Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Back", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            val profile = uiState.targetProfile
            if (profile != null) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.name.take(1).uppercase(),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = profile.experienceLevel ?: "Runner",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Compatibility Score
                    CompatibilityScoreCard(uiState.compatibilityScore)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Running Details
                    ProfileSection("Running Goals", profile.runningGoal ?: "-")
                    ProfileSection("Preferred Distance", profile.preferredDistance ?: "-")
                    ProfileSection("Preferred Pace", profile.preferredPace ?: "-")
                    ProfileSection("Preferred Days", profile.runningDays?.joinToString(", ") ?: "-")
                    
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // Action Button
                    val request = uiState.existingRequest
                    val isMatched = request?.status == RequestStatus.ACCEPTED
                    val buttonText = when {
                        uiState.requestSent -> "Request Sent"
                        request?.status == RequestStatus.PENDING -> "Pending Request"
                        isMatched -> "Plan a Run"
                        else -> "Send Run Request"
                    }
                    val isEnabled = !uiState.requestSent && (request == null || isMatched)
                    
                    if (isMatched) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onChat(profile.uid) },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Chat", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onPlanRun(profile.uid) },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Plan a Run", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        JogpalButton(
                            text = buttonText,
                            onClick = { 
                                viewModel.sendRunRequest()
                            },
                            enabled = isEnabled
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun CompatibilityScoreCard(score: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Compatibility Match",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
private fun ProfileSection(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
    }
}
