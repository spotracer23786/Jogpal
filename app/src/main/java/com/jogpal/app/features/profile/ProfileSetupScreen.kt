package com.jogpal.app.features.profile

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jogpal.app.core.designsystem.components.JogpalButton
import com.jogpal.app.core.designsystem.components.JogpalLogo
import com.jogpal.app.core.designsystem.components.SelectableCard
import com.jogpal.app.domain.user.UserProfile
import com.jogpal.app.features.onboarding.components.HeroVisual

@Composable
fun ProfileSetupScreen(
    uid: String,
    email: String,
    initialName: String,
    onSetupComplete: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory()),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 4
    
    // Form state
    var name by remember { mutableStateOf(initialName) }
    var experienceLevel by remember { mutableStateOf<String?>(null) }
    var runningGoal by remember { mutableStateOf<String?>(null) }
    var preferredDistance by remember { mutableStateOf<String?>(null) }
    var preferredPace by remember { mutableStateOf<String?>(null) }
    val selectedDays = remember { mutableStateListOf<String>() }

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.SaveSuccess) {
            onSetupComplete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HeroVisual(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.TopCenter)
                .alpha(0.3f)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            JogpalLogo()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress Indicator
            ProgressIndicator(currentStep, totalSteps)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Let's build your running profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Tell us a little about how you run.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    label = "StepTransition"
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        when (step) {
                            1 -> StepOne(
                                name = name,
                                onNameChange = { name = it },
                                selectedExperience = experienceLevel,
                                onExperienceSelect = { experienceLevel = it }
                            )
                            2 -> StepTwo(
                                selectedGoal = runningGoal,
                                onGoalSelect = { runningGoal = it }
                            )
                            3 -> StepThree(
                                selectedDistance = preferredDistance,
                                onDistanceSelect = { preferredDistance = it },
                                selectedPace = preferredPace,
                                onPaceSelect = { preferredPace = it }
                            )
                            4 -> StepFour(
                                selectedDays = selectedDays,
                                onDayToggle = { day ->
                                    if (selectedDays.contains(day)) selectedDays.remove(day)
                                    else selectedDays.add(day)
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = uiState !is ProfileUiState.Loading
                    ) {
                        Text("Back")
                    }
                }
                
                JogpalButton(
                    text = if (currentStep == totalSteps) "Complete" else "Continue",
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep++
                        } else {
                            val profile = UserProfile(
                                uid = uid,
                                name = name,
                                email = email,
                                runningGoal = runningGoal,
                                experienceLevel = experienceLevel,
                                preferredDistance = preferredDistance,
                                preferredPace = preferredPace,
                                runningDays = selectedDays.toList()
                            )
                            viewModel.saveProfile(profile)
                        }
                    },
                    modifier = Modifier.weight(2f),
                    enabled = isStepValid(currentStep, name, experienceLevel, runningGoal, preferredDistance, preferredPace, selectedDays) &&
                            uiState !is ProfileUiState.Loading
                )
            }
        }
        
        if (uiState is ProfileUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ProgressIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val stepNumber = index + 1
            val isActive = stepNumber <= currentStep
            val isCurrent = stepNumber == currentStep
            
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 12.dp else 8.dp)
                    .background(
                        color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
            
            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            color = if (stepNumber < currentStep) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@Composable
private fun StepOne(
    name: String,
    onNameChange: (String) -> Unit,
    selectedExperience: String?,
    onExperienceSelect: (String) -> Unit
) {
    Text("What's your name?", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    Text("Experience Level", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(12.dp))
    val levels = listOf("Beginner", "Intermediate", "Advanced")
    levels.forEach { level ->
        SelectableCard(
            text = level,
            selected = selectedExperience == level,
            onClick = { onExperienceSelect(level) },
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun StepTwo(
    selectedGoal: String?,
    onGoalSelect: (String) -> Unit
) {
    Text("What's your primary goal?", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(12.dp))
    val goals = listOf("Get fit", "Build endurance", "Train for a race", "Run socially", "Improve pace")
    goals.forEach { goal ->
        SelectableCard(
            text = goal,
            selected = selectedGoal == goal,
            onClick = { onGoalSelect(goal) },
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun StepThree(
    selectedDistance: String?,
    onDistanceSelect: (String) -> Unit,
    selectedPace: String?,
    onPaceSelect: (String) -> Unit
) {
    Text("Preferred Distance", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(12.dp))
    val distances = listOf("2–3 km", "5 km", "10 km", "10+ km")
    distances.forEach { distance ->
        SelectableCard(
            text = distance,
            selected = selectedDistance == distance,
            onClick = { onDistanceSelect(distance) },
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Text("Preferred Pace", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(12.dp))
    val paces = listOf("Easy", "Moderate", "Fast", "Competitive")
    paces.forEach { pace ->
        SelectableCard(
            text = pace,
            selected = selectedPace == pace,
            onClick = { onPaceSelect(pace) },
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun StepFour(
    selectedDays: List<String>,
    onDayToggle: (String) -> Unit
) {
    Text("Preferred Running Days", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Text("Select all that apply", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    Spacer(modifier = Modifier.height(16.dp))
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    days.forEach { day ->
        SelectableCard(
            text = day,
            selected = selectedDays.contains(day),
            onClick = { onDayToggle(day) },
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

private fun isStepValid(
    step: Int,
    name: String,
    experience: String?,
    goal: String?,
    distance: String?,
    pace: String?,
    days: List<String>
): Boolean {
    return when (step) {
        1 -> name.isNotBlank() && experience != null
        2 -> goal != null
        3 -> distance != null && pace != null
        4 -> days.isNotEmpty()
        else -> false
    }
}
