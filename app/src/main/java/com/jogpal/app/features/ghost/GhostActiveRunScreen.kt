package com.jogpal.app.features.ghost

import android.graphics.Color as AndroidColor
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jogpal.app.data.ghost.GhostRepository
import com.jogpal.app.ui.theme.JogpalPrimary
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.annotations.MarkerOptions
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostActiveRunScreen(
    ghostId: String,
    onNavigateBack: () -> Unit,
    onFinishRun: (String, Long, Double, Boolean) -> Unit,
    ghostRepository: GhostRepository = remember { GhostRepository() }
) {
    val context = LocalContext.current
    val viewModel: GhostRunViewModel = viewModel(factory = GhostRunViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()

    val ghostRuns by ghostRepository.getEligibleGhostRuns().collectAsState(initial = GhostRepository.getMockGhostRuns())

    var mapInstance by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }

    LaunchedEffect(ghostId) {
        viewModel.initGhostRun(ghostId, ghostRuns)
        viewModel.startRace()
    }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished && uiState.ghostRun != null) {
            val ghost = uiState.ghostRun!!
            val isWinner = uiState.userDistanceKm >= ghost.distanceKm && uiState.elapsedTimeSeconds <= ghost.durationSeconds
            onFinishRun(ghost.id, uiState.elapsedTimeSeconds, uiState.userDistanceKm, isWinner)
        }
    }

    // Overtake pulse animation state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // MapLibre Visual Map Engine
            val ghostPoints = remember(uiState.ghostRun) {
                uiState.ghostRun?.waypoints?.map { LatLng(it.lat, it.lng) } ?: emptyList()
            }

            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        onCreate(null)
                        onStart()
                        getMapAsync { map ->
                            mapInstance = map
                            map.setStyle(Style.Builder().fromJson(DARK_MAP_STYLE)) { _ ->
                                if (ghostPoints.isNotEmpty()) {
                                    val polyline = PolylineOptions()
                                        .addAll(ghostPoints)
                                        .color(AndroidColor.parseColor("#4A90E2")) // Cyan-blue translucent route
                                        .width(7f)
                                    map.addPolyline(polyline)
                                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(ghostPoints.first(), 15.0))
                                }
                            }
                        }
                    }
                },
                update = { mapView ->
                    mapInstance?.let { map ->
                        map.clear()
                        if (ghostPoints.isNotEmpty()) {
                            // Ghost route
                            val polyline = PolylineOptions()
                                .addAll(ghostPoints)
                                .color(AndroidColor.parseColor("#4A90E2"))
                                .width(7f)
                            map.addPolyline(polyline)

                            // Ghost Runner position marker
                            uiState.ghostLocation?.let { ghostPos ->
                                map.addMarker(
                                    MarkerOptions()
                                        .position(ghostPos)
                                        .title("👻 Ghost Runner")
                                )
                            }

                            // User path & position marker
                            if (uiState.userPath.isNotEmpty()) {
                                val userPolyline = PolylineOptions()
                                    .addAll(uiState.userPath)
                                    .color(AndroidColor.parseColor("#32FF7E"))
                                    .width(9f)
                                map.addPolyline(userPolyline)
                                map.addMarker(
                                    MarkerOptions()
                                        .position(uiState.userPath.last())
                                        .title("🏃 YOU")
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Top Status Overlay & SOS Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (uiState.isUserAhead) Color(0xFF132E1E) else Color(0xFF281C26),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (uiState.isUserAhead) JogpalPrimary.copy(alpha = glowAlpha) else Color(0xFFFF4757).copy(alpha = glowAlpha)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.statusMessage,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (uiState.isUserAhead) JogpalPrimary else Color(0xFFFF6B6B)
                            )
                        }
                    }

                    // Emergency SOS Button
                    IconButton(
                        onClick = { /* SOS Action */ },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF3B30))
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "SOS",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Overtake Toast Notice
                AnimatedVisibility(
                    visible = uiState.isOvertakingEvent,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = JogpalPrimary
                    ) {
                        Text(
                            text = "🔥 YOU OVERTOOK YOUR SHADOW!",
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Bottom Race Progress & Comparison Panel (YOU vs GHOST)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0F141C).copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Dynamic Race Status Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uiState.isUserAhead) "WINNING POS" else "BEHIND SHADOW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (uiState.isUserAhead) JogpalPrimary else Color(0xFFFF5252),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "%02d:%02d",
                                    uiState.elapsedTimeSeconds / 60,
                                    uiState.elapsedTimeSeconds % 60
                                ),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // YOU VS GHOST Panel
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // YOU Column
                            Column(modifier = Modifier.weight(1f)) {
                                Text("YOU", fontWeight = FontWeight.Black, color = JogpalPrimary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    String.format(Locale.US, "%.2f km", uiState.userDistanceKm),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Pace: ${uiState.userCurrentPace}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E2634)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("VS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray)
                            }

                            // GHOST Column
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("SHADOW 👻", fontWeight = FontWeight.Black, color = Color(0xFF4A90E2), fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    String.format(Locale.US, "%.2f km", uiState.ghostDistanceKm),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Pace: ${uiState.ghostRun?.pace ?: "--"}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Race Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.togglePause() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                            ) {
                                Icon(
                                    if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (uiState.isPaused) "Resume" else "Pause",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.finishRace()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF3B30),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Finish", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
