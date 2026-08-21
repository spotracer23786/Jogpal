package com.jogpal.app.features.run

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    LaunchedEffect(runId) {
        viewModel.startTracking(runId)
    }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            onNavigateBack()
        }
    }

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
            val context = LocalContext.current
            val userLoc = uiState.userLocation
            val partnerLoc = uiState.partnerLocation
            val plan = uiState.plan
            
            var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
            val mapView = remember { MapView(context) }

            // Standard OSM Raster Style JSON
            val osmStyleJson = """
            {
              "version": 8,
              "sources": {
                "osm": {
                  "type": "raster",
                  "tiles": ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
                  "tileSize": 256,
                  "attribution": "© OpenStreetMap contributors"
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
                                plan?.let {
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.startLat ?: 0.0, it.startLng ?: 0.0), 15.0))
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
                            .title("ME")
                            .snippet("Current position"))
                        builder.include(point)
                        hasPoints = true
                    }

                    // Partner (Jogpal)
                    partnerLoc?.let {
                        val point = LatLng(it.latitude, it.longitude)
                        map.addMarker(MarkerOptions()
                            .position(point)
                            .title("PARTNER")
                            .snippet(uiState.partnerProfile?.name ?: "Runner"))
                        builder.include(point)
                        hasPoints = true
                    }

                    // Destination
                    plan?.let { p ->
                        if (p.endLat != null && p.endLng != null) {
                            val dest = LatLng(p.endLat, p.endLng)
                            map.addMarker(MarkerOptions()
                                .position(dest)
                                .title("FINISH")
                                .snippet(p.destinationName ?: "Goal"))
                            builder.include(dest)
                            hasPoints = true
                        }

                        p.encodedPolyline?.let { encoded ->
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

                    // Dynamic Camera to fit runners and destination
                    if (hasPoints) {
                        try {
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150))
                        } catch (e: Exception) {
                            // Single point
                        }
                    }
                }
            )

            // Attribution text
            Text(
                "© OpenStreetMap",
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 8.sp
            )

            // Stats Card
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(label = "DISTANCE", value = "${uiState.plan?.distanceKm ?: 0.0} km")
                        StatItem(label = "PACE", value = uiState.plan?.pace ?: "-")
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    JogpalButton(
                        text = "Finish Run",
                        onClick = { viewModel.finishRun() },
                        containerColor = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Status Overlay
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Live Sharing Active", color = Color.White, style = MaterialTheme.typography.labelSmall)
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
