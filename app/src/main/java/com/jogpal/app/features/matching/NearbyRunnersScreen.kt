package com.jogpal.app.features.matching

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jogpal.app.core.designsystem.components.JogpalButton
import com.jogpal.app.core.designsystem.components.JogpalLogo
import com.jogpal.app.features.matching.components.RunnerCard
import com.jogpal.app.features.tracking.LocationUiState
import com.jogpal.app.features.tracking.LocationViewModel
import com.jogpal.app.features.tracking.LocationViewModelFactory
import com.jogpal.app.features.tracking.LocationPermissionScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyRunnersScreen(
    uid: String,
    onNavigateBack: () -> Unit,
    onViewProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationViewModel: LocationViewModel = viewModel(factory = LocationViewModelFactory(context))
    val discoveryViewModel: DiscoveryViewModel = viewModel(factory = DiscoveryViewModelFactory())
    
    val locationState by locationViewModel.uiState.collectAsState()
    val discoveryState by discoveryViewModel.uiState.collectAsState()

    var experienceFilter by remember { mutableStateOf<String?>(null) }

    // Initial check
    LaunchedEffect(Unit) {
        locationViewModel.checkPermissionAndFetchLocation(uid)
    }

    // When location becomes available, load runners
    LaunchedEffect(locationState) {
        if (locationState is LocationUiState.LocationAvailable) {
            val loc = (locationState as LocationUiState.LocationAvailable).location
            discoveryViewModel.loadNearbyRunners(loc.latitude, loc.longitude)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { JogpalLogo(modifier = Modifier.padding(start = 8.dp)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Back", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (val state = locationState) {
                is LocationUiState.PermissionRequired -> {
                    LocationPermissionScreen(
                        onPermissionGranted = { locationViewModel.updateLocation(uid) },
                        onPermissionDenied = { locationViewModel.onPermissionDenied() }
                    )
                }
                is LocationUiState.PermissionDenied -> {
                    PermissionDeniedView(
                        onRetry = { locationViewModel.checkPermissionAndFetchLocation(uid) },
                        onBack = onNavigateBack
                    )
                }
                is LocationUiState.LocationDisabled -> {
                    LocationDisabledView(
                        onRetry = { locationViewModel.checkPermissionAndFetchLocation(uid) }
                    )
                }
                is LocationUiState.Loading -> {
                    LoadingView("Getting your location...")
                }
                is LocationUiState.Error -> {
                    ErrorView(state.message) {
                        locationViewModel.checkPermissionAndFetchLocation(uid)
                    }
                }
                is LocationUiState.LocationAvailable -> {
                    // Location is ready, now show discovery state
                    when (val discState = discoveryState) {
                        is DiscoveryUiState.Loading -> {
                            LoadingView("Finding runners nearby...")
                        }
                        is DiscoveryUiState.Empty -> {
                            EmptyView {
                                discoveryViewModel.loadNearbyRunners(state.location.latitude, state.location.longitude)
                            }
                        }
                        is DiscoveryUiState.Success -> {
                            val filteredRunners = if (experienceFilter == null) {
                                discState.runners
                            } else {
                                discState.runners.filter { it.profile.experienceLevel == experienceFilter }
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    Text(
                                        text = "Runners near you",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Find people who match your running style.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    FilterSection(
                                        selectedExperience = experienceFilter,
                                        onExperienceSelect = { experienceFilter = if (experienceFilter == it) null else it }
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                
                                items(filteredRunners) { runner ->
                                    RunnerCard(
                                        runner = runner,
                                        onViewProfile = { onViewProfile(runner.profile.uid) }
                                    )
                                }
                            }
                        }
                        is DiscoveryUiState.Error -> {
                            ErrorView(discState.message) {
                                discoveryViewModel.loadNearbyRunners(state.location.latitude, state.location.longitude)
                            }
                        }
                        else -> {}
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun FilterSection(
    selectedExperience: String?,
    onExperienceSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("Beginner", "Intermediate", "Advanced").forEach { level ->
            val isSelected = selectedExperience == level
            Surface(
                modifier = Modifier.clickable { onExperienceSelect(level) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                border = if (isSelected) null else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
            ) {
                Text(
                    text = level,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun LoadingView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PermissionDeniedView(onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Permission Denied",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Location permission is required to find runners near you.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        JogpalButton(text = "Try Again", onClick = onRetry)
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onBack) {
            Text("Not Now", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun LocationDisabledView(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Location is off",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Jogpal needs your device location to discover runners nearby. Please enable GPS.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        JogpalButton(text = "Enable Location & Retry", onClick = onRetry)
    }
}

@Composable
private fun EmptyView(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No runners nearby yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try expanding your search radius or check again later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        JogpalButton(text = "Refresh", onClick = onRetry)
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Something went wrong", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        JogpalButton(text = "Retry", onClick = onRetry)
    }
}
