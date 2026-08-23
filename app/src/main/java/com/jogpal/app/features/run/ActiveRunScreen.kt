package com.jogpal.app.features.run

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import com.jogpal.app.core.common.PolylineUtils
import com.jogpal.app.core.designsystem.components.JogpalButton
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRunScreen(
    runId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: ActiveRunViewModel = viewModel(factory = ActiveRunViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()

    var isMapFollowing by remember { mutableStateOf(true) }

    LaunchedEffect(runId) {
        viewModel.startTracking(runId)
    }

    if (uiState.finalSummary != null) {
        RunSummaryScreen(
            summary = uiState.finalSummary!!,
            onDone = onNavigateBack
        )
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ACTIVE RUN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
                            Text(uiState.partnerProfile?.name ?: "Partner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            Box(modifier = modifier.fillMaxSize().padding(innerPadding)) {
                // MAP
                val userLoc = uiState.userLocation
                val partnerLoc = uiState.partnerLocation
                val plan = uiState.plan
                
                var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
                val mapView = remember { MapView(context) }

                val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                val tileUrl = if (isDark) {
                    "https://a.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png"
                } else {
                    "https://a.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png"
                }
                val osmStyleJson = """
                {
                  "version": 8,
                  "sources": {
                    "osm": {
                      "type": "raster",
                      "tiles": ["$tileUrl"],
                      "tileSize": 256,
                      "attribution": "© CartoDB / © OpenStreetMap"
                    }
                  },
                  "layers": [
                    {
                      "id": "osm",
                      "type": "raster",
                      "source": "osm",
                      "minzoom": 0,
                      "maxzoom": 19
                    }
                  ]
                }
                """.trimIndent()

                // Lifecycle management for MapView
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        when (event) {
                            androidx.lifecycle.Lifecycle.Event.ON_START -> mapView.onStart()
                            androidx.lifecycle.Lifecycle.Event.ON_RESUME -> mapView.onResume()
                            androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                            androidx.lifecycle.Lifecycle.Event.ON_STOP -> mapView.onStop()
                            androidx.lifecycle.Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        mapView.onDestroy()
                    }
                }

                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        if (mapLibreMap == null) {
                            view.getMapAsync { map ->
                                mapLibreMap = map
                                map.setStyle(Style.Builder().fromJson(osmStyleJson)) {
                                val target = userLoc?.let { LatLng(it.latitude, it.longitude) }
                                    ?: plan?.let { LatLng(it.startLat ?: 0.0, it.startLng ?: 0.0) }
                                    ?: LatLng(0.0, 0.0)
                                
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 15.0))
                            }
                                map.addOnCameraMoveStartedListener { reason ->
                                    if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) {
                                        isMapFollowing = false
                                    }
                                }
                            }
                        }

                        val map = mapLibreMap ?: return@AndroidView
                        map.clear()

                        val builder = LatLngBounds.Builder()
                        var hasPoints = false

                        // Current User (Me)
                        userLoc?.let {
                            val point = LatLng(it.latitude, it.longitude)
                            map.addMarker(MarkerOptions()
                                .position(point)
                                .title("ME"))
                            builder.include(point)
                            hasPoints = true
                            
                            if (isMapFollowing) {
                                map.animateCamera(CameraUpdateFactory.newLatLng(point))
                            }
                        }

                        // Partner (Jogpal)
                        partnerLoc?.let {
                            val point = LatLng(it.latitude, it.longitude)
                            map.addMarker(MarkerOptions()
                                .position(point)
                                .title("PARTNER"))
                            builder.include(point)
                            hasPoints = true
                        }

                        // Destination
                        plan?.let { p ->
                            if (p.endLat != null && p.endLng != null) {
                                val dest = LatLng(p.endLat, p.endLng)
                                map.addMarker(MarkerOptions()
                                    .position(dest)
                                    .title("FINISH"))
                                builder.include(dest)
                                hasPoints = true
                            }

                            uiState.currentRoutePolyline?.let { encoded ->
                                val points = PolylineUtils.decodePolyline(encoded).toLatLngList()
                                if (points.isNotEmpty()) {
                                    map.addPolyline(PolylineOptions()
                                        .addAll(points)
                                        .color(android.graphics.Color.parseColor("#007AFF"))
                                        .width(6f))
                                    
                                    points.forEach { builder.include(it) }
                                    hasPoints = true
                                }
                            }
                        }
                    }
                )

                // Attribution text
                Text(
                    "© OpenStreetMap",
                    modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 200.dp, start = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 8.sp
                )

                // Re-routing Overlay
                if (uiState.isRerouting) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .zIndex(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    ) {
                        Text(
                            "Recalculating route...",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // Stats Card
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        // Time
                        val hours = uiState.elapsedTimeSeconds / 3600
                        val minutes = (uiState.elapsedTimeSeconds % 3600) / 60
                        val seconds = uiState.elapsedTimeSeconds % 60
                        val timeStr = if (hours > 0) {
                            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                        } else {
                            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                        }
                        
                        Text(text = timeStr, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
                        Text(text = "ELAPSED TIME", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(label = "DISTANCE", value = String.format(Locale.getDefault(), "%.2f km", uiState.traveledDistanceKm))
                            StatItem(label = "GOAL", value = String.format(Locale.getDefault(), "%.1f km", uiState.remainingDistanceKm ?: uiState.plan?.distanceKm ?: 0.0))
                            StatItem(label = "PACE", value = uiState.currentPace)
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (uiState.status == TrackingStatus.ACTIVE) {
                                ControlButton(
                                    icon = Icons.Default.Pause,
                                    onClick = { viewModel.pauseRun() },
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            } else if (uiState.status == TrackingStatus.PAUSED) {
                                ControlButton(
                                    icon = Icons.Default.PlayArrow,
                                    onClick = { viewModel.resumeRun() },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            JogpalButton(
                                text = "Finish",
                                onClick = { viewModel.finishRun() },
                                containerColor = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                // Resume Following Button
                if (!isMapFollowing) {
                    Button(
                        onClick = { isMapFollowing = true },
                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Resume Following", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = containerColor)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
    }
}
