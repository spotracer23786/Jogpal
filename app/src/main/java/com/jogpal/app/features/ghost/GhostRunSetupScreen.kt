package com.jogpal.app.features.ghost

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.jogpal.app.data.ghost.GhostRepository
import com.jogpal.app.domain.ghost.GhostRun
import com.jogpal.app.ui.theme.JogpalPrimary
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.PolylineOptions
import java.util.Locale

const val DARK_MAP_STYLE = """
{
  "version": 8,
  "sources": {
    "osm": {
      "type": "raster",
      "tiles": ["https://a.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png"],
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
      "maxzoom": 20
    }
  ]
}
"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostRunSetupScreen(
    ghostId: String,
    onNavigateBack: () -> Unit,
    onStartGhostRun: (String) -> Unit,
    ghostRepository: GhostRepository = remember { GhostRepository() }
) {
    val ghostRuns by ghostRepository.getEligibleGhostRuns().collectAsState(initial = GhostRepository.getMockGhostRuns())
    val ghost = remember(ghostId, ghostRuns) {
        ghostRepository.getGhostRunById(ghostId, ghostRuns)
    }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shadow Run Challenge", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (ghost == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = JogpalPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Ghost Header Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF141A24),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JogpalPrimary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(JogpalPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👻", fontSize = 32.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Ready to race your shadow?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Text(
                            text = ghost.title,
                            fontSize = 14.sp,
                            color = JogpalPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Previous Run Metrics
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1B222D)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "PREVIOUS PERFORMANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JogpalPrimary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        MetricRow(
                            label = "Target Distance",
                            value = String.format(Locale.US, "%.2f km", ghost.distanceKm)
                        )
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))
                        MetricRow(
                            label = "Target Time",
                            value = ghost.formattedTime
                        )
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))
                        MetricRow(
                            label = "Target Pace",
                            value = ghost.pace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Route Preview Map
                Text(
                    text = "ROUTE PREVIEW",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF10141C))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                ) {
                    val routePoints = ghost.waypoints.map { LatLng(it.lat, it.lng) }

                    AndroidView(
                        factory = { context ->
                            MapView(context).apply {
                                onCreate(null)
                                onStart()
                                getMapAsync { map ->
                                    map.setStyle(Style.Builder().fromJson(DARK_MAP_STYLE)) { _ ->
                                        if (routePoints.isNotEmpty()) {
                                            val polylineOptions = PolylineOptions()
                                                .addAll(routePoints)
                                                .color(android.graphics.Color.parseColor("#32FF7E"))
                                                .width(6f)
                                            map.addPolyline(polylineOptions)

                                            // Center camera on route start
                                            val center = routePoints.first()
                                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 14.0))
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Primary Start Button
                Button(
                    onClick = { onStartGhostRun(ghost.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JogpalPrimary,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        Icons.Default.DirectionsRun,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "START SHADOW RUN",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
