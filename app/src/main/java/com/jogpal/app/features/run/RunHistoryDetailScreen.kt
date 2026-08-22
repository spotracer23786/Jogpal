package com.jogpal.app.features.run

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jogpal.app.core.common.PolylineUtils
import com.jogpal.app.features.run.toLatLngList
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunHistoryDetailScreen(
    runId: String,
    onNavigateBack: () -> Unit,
    viewModel: RunHistoryDetailViewModel = viewModel(factory = RunHistoryDetailViewModelFactory()),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(runId) {
        viewModel.loadRun(runId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Run Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            val run = uiState.run
            if (uiState.isLoading || run == null) {
                Box(modifier = Modifier.fillMaxSize().height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Map Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    val mapView = remember { MapView(context) }
                    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
                    
                    // Lifecycle management
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

                    AndroidView(
                        factory = { mapView },
                        modifier = Modifier.fillMaxSize(),
                        update = { view ->
                            if (mapLibreMap == null) {
                                view.getMapAsync { map ->
                                    mapLibreMap = map
                                    map.setStyle(Style.Builder().fromJson(osmStyleJson)) {
                                        val polyline = run.actualPolyline ?: run.encodedPolyline
                                        polyline?.let { encoded ->
                                            val points = PolylineUtils.decodePolyline(encoded).toLatLngList()
                                            if (points.isNotEmpty()) {
                                                map.addPolyline(PolylineOptions()
                                                    .addAll(points)
                                                    .color(android.graphics.Color.parseColor("#007AFF"))
                                                    .width(6f))
                                                
                                                // Start Marker
                                                map.addMarker(MarkerOptions()
                                                    .position(points.first())
                                                    .title("START"))
                                                
                                                // Finish Marker
                                                map.addMarker(MarkerOptions()
                                                    .position(points.last())
                                                    .title("FINISH"))

                                                val builder = LatLngBounds.Builder()
                                                points.forEach { builder.include(it) }
                                                map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }

                // Stats Section
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = run.title.ifBlank { "Planned Run" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val dateFormatter = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
                    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val dateStr = run.completedAt?.let { dateFormatter.format(Date(it)) } ?: run.date
                    
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    DetailStatsGrid(run)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (run.startTime.isNotBlank()) {
                        DetailItem("Scheduled Time", run.startTime)
                    }
                    if (run.completedAt != null) {
                        DetailItem("Finished At", timeFormatter.format(Date(run.completedAt)))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailStatsGrid(run: com.jogpal.app.domain.run.RunPlan) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStatItem("DISTANCE", String.format("%.2f km", run.actualDistanceKm ?: 0.0), Modifier.weight(1f))
                SummaryStatItem("TIME", formatDuration(run.actualDurationSeconds ?: 0), Modifier.weight(1f))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.Gray.copy(alpha = 0.1f))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStatItem("CALORIES", "${run.calories ?: 0} kcal", Modifier.weight(1f))
                SummaryStatItem("AVG PACE", calculateAveragePace(run), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryStatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }
}

private fun calculateAveragePace(run: com.jogpal.app.domain.run.RunPlan): String {
    val distance = run.actualDistanceKm ?: 0.0
    val seconds = run.actualDurationSeconds ?: 0L
    if (distance < 0.01 || seconds < 1) return "--:--"
    val totalMinutes = seconds / 60.0
    val paceDecimal = totalMinutes / distance
    val paceMins = paceDecimal.toInt()
    val paceSecs = ((paceDecimal - paceMins) * 60).toInt()
    return String.format("%d:%02d", paceMins, paceSecs)
}
