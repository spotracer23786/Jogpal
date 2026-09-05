package com.jogpal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.maplibre.android.MapLibre
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Brush
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jogpal.app.core.designsystem.components.*
import com.jogpal.app.ui.theme.*
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.border
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
import com.jogpal.app.features.chat.ChatScreen


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

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: ""
                val mainTabRoutes = listOf("home", "nearby_runners", "ghost_select", "run_history", "passport")
                val showBottomBar = currentRoute in mainTabRoutes

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            JogpalBottomNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo("home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding)
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
                            },
                            onChat = { partnerUid ->
                                navController.navigate("chat/$partnerUid")
                            },
                            onStartSoloRun = {
                                navController.navigate("solo_setup")
                            },
                            onStartGhostMode = {
                                navController.navigate("ghost_select")
                            },
                            onOpenPassport = {
                                navController.navigate("passport")
                            },
                            onOpenSafety = {
                                navController.navigate("safety_settings")
                            }
                        )
                    }

                    composable("safety_settings") {
                        com.jogpal.app.features.sos.SafetySettingsScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToHistory = { navController.navigate("sos_history") },
                            onStartTestMode = { navController.navigate("sos_test_mode") }
                        )
                    }

                    composable("sos_history") {
                        val repo = remember { com.jogpal.app.features.sos.SOSRepository.getInstance() }
                        val events by repo.sosHistory.collectAsState()
                        com.jogpal.app.features.sos.components.SOSHistoryScreen(
                            events = events,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("sos_test_mode") {
                        var sosStatus by remember { mutableStateOf(com.jogpal.app.features.sos.SOSStatus.COUNTDOWN) }
                        var showPreview by remember { mutableStateOf(false) }
                        val mockLocation = remember { com.jogpal.app.features.sos.LiveLocationData() }

                        if (sosStatus == com.jogpal.app.features.sos.SOSStatus.COUNTDOWN) {
                            com.jogpal.app.features.sos.components.SOSCountdownOverlay(
                                isTestMode = true,
                                onCountdownFinished = {
                                    sosStatus = com.jogpal.app.features.sos.SOSStatus.ACTIVE
                                    com.jogpal.app.features.sos.SOSRepository.getInstance().logSOSEvent(
                                        com.jogpal.app.features.sos.SOSEvent(
                                            title = "SOS Test Mode",
                                            dateString = "Just Now",
                                            type = com.jogpal.app.features.sos.SOSEventType.TEST_MODE,
                                            contactsNotifiedCount = 0
                                        )
                                    )
                                },
                                onCancel = { navController.popBackStack() }
                            )
                        }

                        if (sosStatus == com.jogpal.app.features.sos.SOSStatus.ACTIVE) {
                            com.jogpal.app.features.sos.components.SOSActiveStateScreen(
                                locationData = mockLocation,
                                trustedContactsCount = 0,
                                isTestMode = true,
                                onDeactivate = { navController.popBackStack() },
                                onViewPreview = { showPreview = true }
                            )
                        }

                        if (showPreview) {
                            com.jogpal.app.features.sos.components.SOSNotificationPreviewDialog(
                                locationData = mockLocation,
                                onDismiss = { showPreview = false }
                            )
                        }
                    }

                    composable("passport") {
                        com.jogpal.app.features.passport.PassportScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("ghost_select") {
                        com.jogpal.app.features.ghost.GhostRunSelectScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onSelectGhostRun = { ghostId ->
                                navController.navigate("ghost_setup/$ghostId")
                            }
                        )
                    }

                    composable("ghost_setup/{ghostId}") { backStackEntry ->
                        val ghostId = backStackEntry.arguments?.getString("ghostId") ?: ""
                        com.jogpal.app.features.ghost.GhostRunSetupScreen(
                            ghostId = ghostId,
                            onNavigateBack = { navController.popBackStack() },
                            onStartGhostRun = { id ->
                                navController.navigate("ghost_active/$id")
                            }
                        )
                    }

                    composable("ghost_active/{ghostId}") { backStackEntry ->
                        val ghostId = backStackEntry.arguments?.getString("ghostId") ?: ""
                        com.jogpal.app.features.ghost.GhostActiveRunScreen(
                            ghostId = ghostId,
                            onNavigateBack = { navController.popBackStack() },
                            onFinishRun = { id, dur, dist, isWinner ->
                                navController.navigate("ghost_finish/$id/$dur/$dist/$isWinner") {
                                    popUpTo("home")
                                }
                            }
                        )
                    }

                    composable("ghost_finish/{ghostId}/{userDuration}/{userDistance}/{isWinner}") { backStackEntry ->
                        val ghostId = backStackEntry.arguments?.getString("ghostId") ?: ""
                        val userDuration = backStackEntry.arguments?.getString("userDuration")?.toLongOrNull() ?: 0L
                        val userDistance = backStackEntry.arguments?.getString("userDistance")?.toDoubleOrNull() ?: 0.0
                        val isWinner = backStackEntry.arguments?.getString("isWinner")?.toBoolean() ?: false

                        com.jogpal.app.features.ghost.GhostRunFinishScreen(
                            ghostId = ghostId,
                            userDurationSeconds = userDuration,
                            userDistanceKm = userDistance,
                            isWinner = isWinner,
                            onRunAgain = {
                                navController.navigate("ghost_active/$ghostId") {
                                    popUpTo("home")
                                }
                            },
                            onDone = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("chat/{partnerUid}") { backStackEntry ->
                        val partnerUid = backStackEntry.arguments?.getString("partnerUid") ?: ""
                        ChatScreen(
                            partnerUid = partnerUid,
                            onNavigateBack = { navController.popBackStack() }
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
                            },
                            onChat = { partnerUid ->
                                navController.navigate("chat/$partnerUid")
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

                    composable("solo_setup") {
                        com.jogpal.app.features.run.SoloSetupScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onStartSoloRun = { goalType, goalValue, routeShape, paceMode, targetPace, weather, theme, ghostEnabled, startLat, startLng, endLat, endLng ->
                                navController.navigate("solo_active_run/$goalType/$goalValue/$routeShape/$paceMode/$targetPace/$weather/$theme/$ghostEnabled/$startLat/$startLng/$endLat/$endLng")
                            }
                        )
                    }

                    composable(
                        "solo_active_run/{goalType}/{goalValue}/{routeShape}/{paceMode}/{targetPace}/{weather}/{theme}/{ghostEnabled}/{startLat}/{startLng}/{endLat}/{endLng}"
                    ) { backStackEntry ->
                        val goalType = backStackEntry.arguments?.getString("goalType") ?: "FREE"
                        val goalValue = backStackEntry.arguments?.getString("goalValue")?.toDoubleOrNull() ?: 0.0
                        val routeShape = backStackEntry.arguments?.getString("routeShape") ?: "LOOP"
                        val paceMode = backStackEntry.arguments?.getString("paceMode") ?: "MODERATE"
                        val targetPace = backStackEntry.arguments?.getString("targetPace")?.toDoubleOrNull() ?: 5.5
                        val weather = backStackEntry.arguments?.getString("weather") ?: "SUNNY"
                        val theme = backStackEntry.arguments?.getString("theme") ?: "LIGHT"
                        val ghostEnabled = backStackEntry.arguments?.getString("ghostEnabled")?.toBoolean() ?: false
                        val startLat = backStackEntry.arguments?.getString("startLat")?.toDoubleOrNull()
                        val startLng = backStackEntry.arguments?.getString("startLng")?.toDoubleOrNull()
                        val endLat = backStackEntry.arguments?.getString("endLat")?.toDoubleOrNull()
                        val endLng = backStackEntry.arguments?.getString("endLng")?.toDoubleOrNull()

                        com.jogpal.app.features.run.SoloActiveRunScreen(
                            goalTypeName = goalType,
                            goalValue = goalValue,
                            routeShapeName = routeShape,
                            paceModeName = paceMode,
                            targetPace = targetPace,
                            weatherName = weather,
                            themeName = theme,
                            ghostEnabled = ghostEnabled,
                            customStartLat = startLat,
                            customStartLng = startLng,
                            customEndLat = endLat,
                            customEndLng = endLng,
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
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            }
                        }
                        is MainUiState.Authenticated -> {
                            if (!state.isProfileLoading) {
                                if (state.profileCompleted) {
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("profile_setup/${state.uid}") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
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
    onChat: (String) -> Unit,
    onStartSoloRun: () -> Unit,
    onStartGhostMode: () -> Unit = {},
    onOpenPassport: () -> Unit = {},
    onOpenSafety: () -> Unit = {},
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
    var selectedFilter by remember { mutableStateOf("All") }

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

    val profileObj = (profileState as? ProfileUiState.Success)?.profile

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = JogpalBackgroundDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(JogpalBackgroundDark)
                .padding(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        ) {

            // Clean Corporate Top App Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val profileName = profileObj?.name ?: "Runner"
                    Text(
                        text = "Good Morning,",
                        fontSize = 13.sp,
                        color = JogpalMutedTextDark,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = profileName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = JogpalOnBackgroundDark
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(JogpalSurfaceDark)
                            .border(1.dp, JogpalCardBorderDark, CircleShape)
                            .clickable { onOpenSafety() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Safety",
                            tint = JogpalPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(JogpalPrimary)
                            .clickable { onOpenPassport() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profileObj?.name?.take(1)?.uppercase() ?: "J",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Hero Dashboard Card (Inspired by reference UI 1: "Your Body Is Ready")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF14241B),
                                Color(0xFF090F0D)
                            )
                        )
                    )
                    .border(1.dp, JogpalPrimary.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                    .padding(22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Today's Readiness",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = JogpalPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your Body Is\nReady",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = JogpalOnBackgroundDark,
                                lineHeight = 28.sp
                            )
                        }

                        // Circular Progress Indicator (93% Quality Score)
                        Box(
                            modifier = Modifier.size(70.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { 0.93f },
                                modifier = Modifier.fillMaxSize(),
                                color = JogpalPrimary,
                                strokeWidth = 6.dp,
                                trackColor = JogpalCardBorderDark,
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "93%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = JogpalOnBackgroundDark
                                )
                                Text(
                                    text = "Quality",
                                    fontSize = 9.sp,
                                    color = JogpalMutedTextDark
                                )
                            }
                        }
                    }

                    // Lower Quick Action Bar inside Hero
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = null,
                                tint = JogpalMutedTextDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Daily Goal: 5.0 Km Target",
                                fontSize = 12.sp,
                                color = JogpalMutedTextDark
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(JogpalPrimary)
                                .clickable { onStartGhostMode() }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Start Run",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dedicated Dual Action Cards: SOLO RUN vs PARTNER RUN (Separate Core Modes)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Solo Run Mode Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(JogpalSurfaceDark)
                        .border(1.dp, JogpalCardBorderDark, RoundedCornerShape(24.dp))
                        .clickable { onStartGhostMode() }
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(JogpalPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = null,
                                    tint = JogpalPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = JogpalMutedTextDark
                            )
                        }
                        Column {
                            Text(
                                text = "Solo Run",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = JogpalOnBackgroundDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Race Ghost / Solo Pace",
                                fontSize = 11.sp,
                                color = JogpalMutedTextDark
                            )
                        }
                    }
                }

                // Partner Run Mode Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(JogpalSurfaceDark)
                        .border(1.dp, JogpalCardBorderDark, RoundedCornerShape(24.dp))
                        .clickable { onFindPartner() }
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(JogpalSecondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = JogpalSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = JogpalMutedTextDark
                            )
                        }
                        Column {
                            Text(
                                text = "Partner Run",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = JogpalOnBackgroundDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Match Nearby Runners",
                                fontSize = 11.sp,
                                color = JogpalMutedTextDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Horizontally Scrollable Filter Chips
            JogpalFilterChips(
                options = listOf("All", "Overview", "Solo Mode", "Partners", "Safety"),
                selectedOption = selectedFilter,
                onOptionSelected = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // Activity Summary Progress Segment Card (Inspired by reference UI 2: Workout Progress 68%)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
                            Text(
                                text = "Weekly Training Progress",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JogpalOnBackgroundDark
                            )
                            Text(
                                text = "89% Achieved",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = JogpalPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Smooth Segment Progress Bar
                        LinearProgressIndicator(
                            progress = { 0.89f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = JogpalPrimary,
                            trackColor = JogpalCardBorderDark,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${upcomingRuns.size} Scheduled Runs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = JogpalOptimalGreen)
                            Text("${matches.size} Connected Partners", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = JogpalPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2x2 Feature Action Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    JogpalStatCard(
                        title = "Pace &\nTelemetry",
                        value = profileObj?.preferredPace ?: "--",
                        subtitle = "Target Pace",
                        icon = Icons.Default.DirectionsRun,
                        onClick = onFindPartner,
                        modifier = Modifier.weight(1f)
                    )
                    JogpalStatCard(
                        title = "Run\nHistory",
                        value = "${upcomingRuns.size}",
                        subtitle = "Recorded Runs",
                        icon = Icons.Default.History,
                        onClick = onViewHistory,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    JogpalStatCard(
                        title = "AI Running\nAssistant",
                        value = "Active",
                        badgeText = "AI",
                        badgeColor = JogpalPrimary,
                        icon = Icons.Default.Psychology,
                        onClick = { onChat(uid) },
                        modifier = Modifier.weight(1f)
                    )
                    JogpalStatCard(
                        title = "Safety &\nSOS Check",
                        value = "Protected",
                        badgeText = "SOS",
                        badgeColor = JogpalSecondary,
                        icon = Icons.Default.Shield,
                        onClick = onOpenSafety,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                        RequestCard(
                            request = request,
                            onAccept = { matchingViewModel.acceptRequest(request.id) },
                            onDecline = { matchingViewModel.declineRequest(request.id) }
                        )
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
                        MatchCard(
                            displayModel = displayModel,
                            onPlanRun = { onPlanRun(displayModel.partnerProfile?.uid ?: "") },
                            onChat = { onChat(displayModel.partnerProfile?.uid ?: "") }
                        )
                    }
                }

                // Safe Mobile Bottom Spacing for Floating Navigation Dock
                Spacer(modifier = Modifier.height(100.dp))
            }
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
fun MatchCard(
    displayModel: com.jogpal.app.features.matching.MatchDisplayModel,
    onPlanRun: () -> Unit,
    onChat: () -> Unit
) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = onChat,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Chat", fontWeight = FontWeight.Bold)
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
