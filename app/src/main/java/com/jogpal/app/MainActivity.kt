package com.jogpal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.maplibre.android.MapLibre
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jogpal.app.core.designsystem.components.JogpalButton
import com.jogpal.app.core.designsystem.components.JogpalLogo
import com.jogpal.app.data.auth.AuthRepositoryImpl
import com.jogpal.app.data.profile.ProfileRepositoryImpl
import com.jogpal.app.data.user.UserRepositoryImpl
import com.jogpal.app.domain.matching.Match
import com.jogpal.app.domain.matching.RunRequest
import com.jogpal.app.domain.run.RunPlan
import com.jogpal.app.domain.run.RunStatus
import com.jogpal.app.domain.user.UserProfile
import com.jogpal.app.features.auth.LoginScreen
import com.jogpal.app.features.auth.SignUpScreen
import com.jogpal.app.features.onboarding.OnboardingScreen
import com.jogpal.app.features.profile.ProfileSetupScreen
import com.jogpal.app.features.profile.ProfileUiState
import com.jogpal.app.features.profile.ProfileViewModel
import com.jogpal.app.features.profile.ProfileViewModelFactory
import com.jogpal.app.features.matching.NearbyRunnersScreen
import com.jogpal.app.features.matching.RunnerProfileScreen
import com.jogpal.app.features.matching.MatchingViewModel
import com.jogpal.app.features.matching.MatchingViewModelFactory
import com.jogpal.app.features.run.*
import com.jogpal.app.ui.theme.JogpalTheme

class MainActivity : ComponentActivity() {
    private val authRepository = AuthRepositoryImpl(userRepository = UserRepositoryImpl())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        enableEdgeToEdge()

        setContent {
            JogpalTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = viewModel(factory = MainViewModelFactory())
                val mainUiState by mainViewModel.uiState.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(mainUiState, navController)
                    }

                    composable("onboarding") {
                        OnboardingScreen(
                            onGetStarted = { navController.navigate("signup") },
                            onLogin = { navController.navigate("login") }
                        )
                    }
                    
                    composable("signup") {
                        SignUpScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToLogin = { 
                                navController.navigate("login") {
                                    popUpTo("onboarding")
                                }
                            },
                            onSignUpSuccess = { uid ->
                                navController.navigate("profile_setup/$uid") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable("login") {
                        LoginScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToSignUp = { 
                                navController.navigate("signup") {
                                    popUpTo("onboarding")
                                }
                            },
                            onLoginSuccess = { uid ->
                                mainViewModel.checkAuthState()
                            }
                        )
                    }

                    composable("profile_setup/{uid}") { backStackEntry ->
                        val uid = backStackEntry.arguments?.getString("uid") ?: ""
                        ProfileSetupScreen(
                            uid = uid,
                            email = "",
                            initialName = "",
                            onSetupComplete = {
                                mainViewModel.checkAuthState()
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            onLogout = {
                                authRepository.logout()
                                navController.navigate("onboarding") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onFindPartner = {
                                navController.navigate("nearby_runners")
                            },
                            onPlanRun = { partnerUid ->
                                navController.navigate("plan_run/$partnerUid")
                            },
                            onViewRun = { runId ->
                                navController.navigate("run_details/$runId")
                            },
                            onViewHistory = {
                                navController.navigate("run_history")
                            }
                        )
                    }

                    composable("run_history") {
                        RunHistoryScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onRunClick = { runId ->
                                navController.navigate("run_history_detail/$runId")
                            }
                        )
                    }

                    composable("run_history_detail/{runId}") { backStackEntry ->
                        val runId = backStackEntry.arguments?.getString("runId") ?: ""
                        RunHistoryDetailScreen(
                            runId = runId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("nearby_runners") {
                        val uid = authRepository.currentUserUid ?: ""
                        NearbyRunnersScreen(
                            uid = uid,
                            onNavigateBack = { navController.popBackStack() },
                            onViewProfile = { runnerUid ->
                                navController.navigate("runner_profile/$runnerUid")
                            }
                        )
                    }

                    composable("runner_profile/{uid}") { backStackEntry ->
                        val uid = backStackEntry.arguments?.getString("uid") ?: ""
                        RunnerProfileScreen(
                            targetUid = uid,
                            onNavigateBack = { navController.popBackStack() },
                            onPlanRun = { partnerUid ->
                                navController.navigate("plan_run/$partnerUid")
                            }
                        )
                    }

                    composable("plan_run/{partnerUid}") { backStackEntry ->
                        val partnerUid = backStackEntry.arguments?.getString("partnerUid") ?: ""
                        PlanRunScreen(
                            partnerUid = partnerUid,
                            onNavigateBack = { navController.popBackStack() },
                            onSuccess = { navController.popBackStack() }
                        )
                    }

                    composable("run_details/{runId}") { backStackEntry ->
                        val runId = backStackEntry.arguments?.getString("runId") ?: ""
                        RunDetailScreen(
                            runId = runId,
                            onNavigateBack = { navController.popBackStack() },
                            onStartRun = { id ->
                                navController.navigate("active_run/$id")
                            }
                        )
                    }

                    composable("active_run/{runId}") { backStackEntry ->
                        val runId = backStackEntry.arguments?.getString("runId") ?: ""
                        ActiveRunScreen(
                            runId = runId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                LaunchedEffect(mainUiState) {
                    when (val state = mainUiState) {
                        is MainUiState.Unauthenticated -> {
                            if (navController.currentDestination?.route != "onboarding" && 
                                navController.currentDestination?.route != "login" &&
                                navController.currentDestination?.route != "signup" &&
                                navController.currentDestination?.route != "splash") {
                                navController.navigate("onboarding") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                        is MainUiState.Authenticated -> {
                            if (!state.isProfileLoading) {
                                if (state.profileCompleted) {
                                    navController.navigate("home") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("profile_setup/${state.uid}") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        }
                        MainUiState.Loading -> { /* Do nothing */ }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(state: MainUiState, navController: androidx.navigation.NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            JogpalLogo()
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
    
    LaunchedEffect(state) {
        when (state) {
            is MainUiState.Unauthenticated -> {
                navController.navigate("onboarding") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is MainUiState.Authenticated -> {
                if (!state.isProfileLoading) {
                    if (state.profileCompleted) {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("profile_setup/${state.uid}") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            }
            MainUiState.Loading -> { /* Stay on splash */ }
        }
    }
}

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onFindPartner: () -> Unit,
    onPlanRun: (String) -> Unit,
    onViewRun: (String) -> Unit,
    onViewHistory: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory()),
    matchingViewModel: MatchingViewModel = viewModel(factory = MatchingViewModelFactory())
) {
    val authRepository = AuthRepositoryImpl(userRepository = UserRepositoryImpl())
    val uid = authRepository.currentUserUid ?: ""
    val profileState by profileViewModel.uiState.collectAsState()
    val receivedRequests by matchingViewModel.receivedRequests.collectAsState()
    val matches by matchingViewModel.matches.collectAsState()
    val matchingError by matchingViewModel.error.collectAsState()

    // Real-time run data
    val runRepository = com.jogpal.app.data.run.RunRepositoryImpl()
    val upcomingRuns by runRepository.getUpcomingRuns().collectAsState(initial = emptyList())
    val invitations by runRepository.getIncomingInvitations().collectAsState(initial = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(matchingError) {
        matchingError?.let {
            snackbarHostState.showSnackbar(it)
            matchingViewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        if (uid.isNotEmpty()) {
            profileViewModel.loadProfile(uid)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                JogpalLogo()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onViewHistory) {
                        Icon(Icons.Default.History, contentDescription = "History", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onLogout) {
                        Text("Logout", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            when (val state = profileState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                is ProfileUiState.Success -> {
                    val profile = state.profile
                    Text(
                        text = "Hello, ${profile?.name ?: "Runner"}!",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Ready for your next run?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    JogpalButton(
                        text = "Find a Running Partner",
                        onClick = onFindPartner
                    )

                    // 1. Run Invitations
                    if (invitations.isNotEmpty()) {
                        SectionHeader("Run Invitations")
                        invitations.forEach { plan ->
                            RunInvitationCard(plan, onView = { onViewRun(plan.id) })
                        }
                    }

                    // 2. Incoming Match Requests
                    if (receivedRequests.isNotEmpty()) {
                        SectionHeader("Pending Invites")
                        receivedRequests.forEach { request ->
                            RequestCard(request, onAccept = { matchingViewModel.acceptRequest(request.id) }, onDecline = { matchingViewModel.declineRequest(request.id) })
                        }
                    }

                    // 3. Upcoming Runs
                    if (upcomingRuns.isNotEmpty()) {
                        SectionHeader("Upcoming Runs")
                        upcomingRuns.forEach { run ->
                            UpcomingRunCard(run, onView = { onViewRun(run.id) })
                        }
                    }

                    // 4. Running Partners
                    if (matches.isNotEmpty()) {
                        SectionHeader("My Running Partners")
                        matches.forEach { displayModel ->
                            MatchCard(displayModel, onPlanRun = { onPlanRun(displayModel.partnerProfile?.uid ?: "") })
                        }
                    }

                    SectionHeader("Your Running Profile")

                    profile?.let {
                        ProfileDetailItem("Goal", it.runningGoal ?: "-")
                        ProfileDetailItem("Experience", it.experienceLevel ?: "-")
                        ProfileDetailItem("Distance", it.preferredDistance ?: "-")
                        ProfileDetailItem("Pace", it.preferredPace ?: "-")
                    }
                }
                is ProfileUiState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { profileViewModel.loadProfile(uid) }) {
                        Text("Retry")
                    }
                }
                else -> {}
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Spacer(modifier = Modifier.height(48.dp))
    Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun RunInvitationCard(plan: RunPlan, onView: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        onClick = onView
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("NEW RUN INVITATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${plan.date} • ${plan.startTime}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text("${plan.distanceKm} km • ${plan.pace}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun UpcomingRunCard(run: RunPlan, onView: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        onClick = onView
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${run.date} • ${run.startTime}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(text = run.title.ifBlank { "Planned Run" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = "${run.distanceKm} km", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun RequestCard(request: RunRequest, onAccept: () -> Unit, onDecline: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "MATCH REQUEST", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Match Score: ${request.compatibilityScore}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(onClick = onAccept, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("Accept")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onDecline, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("Decline")
                }
            }
        }
    }
}

@Composable
fun MatchCard(displayModel: com.jogpal.app.features.matching.MatchDisplayModel, onPlanRun: () -> Unit) {
    val partner = displayModel.partnerProfile
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                Text(
                    text = partner?.name?.take(1)?.uppercase() ?: "?",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = partner?.name ?: "Running Partner",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = partner?.experienceLevel ?: "Confirmed",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            TextButton(
                onClick = onPlanRun,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Plan Run", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color.Gray)
            Text(text = value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
