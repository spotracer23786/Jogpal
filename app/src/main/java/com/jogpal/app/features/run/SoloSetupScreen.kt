package com.jogpal.app.features.run

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jogpal.app.core.common.RouteLoopGenerator
import com.jogpal.app.core.designsystem.components.JogpalButton
import com.jogpal.app.data.location.LocationRepositoryImpl
import com.jogpal.app.data.run.GeocodingRepositoryImpl
import com.jogpal.app.data.run.RouteRepositoryImpl
import com.jogpal.app.domain.location.LocationRepository
import com.jogpal.app.domain.run.GeocodingRepository
import com.jogpal.app.domain.run.RouteRepository
import com.jogpal.app.domain.run.SearchResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import java.util.Locale

// UI State
data class SoloSetupUiState(
    val userLocation: com.jogpal.app.domain.user.UserLocation? = null,
    val isLoading: Boolean = false,
    val searchResults: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val computedRoute: List<LatLng> = emptyList(),
    val estimatedDistanceKm: Double = 5.0,
    val estimatedDurationMinutes: Int = 30
)

// View Model
class SoloSetupViewModel(
    private val locationRepository: LocationRepository,
    private val geocodingRepository: GeocodingRepository,
    private val routeRepository: RouteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SoloSetupUiState())
    val uiState: StateFlow<SoloSetupUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        fetchUserLocation()
    }

    fun fetchUserLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            locationRepository.getCurrentLocation().fold(
                onSuccess = { loc ->
                    _uiState.value = _uiState.value.copy(isLoading = false, userLocation = loc)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            )
        }
    }

    fun searchDestination(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            _uiState.value = _uiState.value.copy(isSearching = true)
            geocodingRepository.searchPlaces(query).fold(
                onSuccess = { results ->
                    _uiState.value = _uiState.value.copy(isSearching = false, searchResults = results)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isSearching = false, searchResults = emptyList())
                }
            )
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchResults = emptyList())
    }

    fun calculateCustomRoute(startLat: Double, startLng: Double, endLat: Double, endLng: Double) {
        viewModelScope.launch {
            routeRepository.calculateRoute(startLat, startLng, endLat, endLng, false).fold(
                onSuccess = { routes ->
                    if (routes.isNotEmpty()) {
                        val route = routes.first()
                        val points = com.jogpal.app.core.common.PolylineUtils.decodePolyline(route.encodedPolyline).toLatLngList()
                        _uiState.value = _uiState.value.copy(
                            computedRoute = points,
                            estimatedDistanceKm = route.distanceKm,
                            estimatedDurationMinutes = route.durationMinutes
                        )
                    }
                },
                onFailure = {}
            )
        }
    }

    fun setComputedRoute(points: List<LatLng>, dist: Double, pace: Double) {
        val duration = if (pace > 0) (dist * pace).toInt() else 30
        _uiState.value = _uiState.value.copy(
            computedRoute = points,
            estimatedDistanceKm = dist,
            estimatedDurationMinutes = duration
        )
    }
}

class SoloSetupViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SoloSetupViewModel(
            LocationRepositoryImpl(context),
            GeocodingRepositoryImpl(),
            RouteRepositoryImpl()
        ) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoloSetupScreen(
    onNavigateBack: () -> Unit,
    onStartSoloRun: (
        goalType: String,
        goalValue: Double,
        routeShape: String,
        paceMode: String,
        targetPace: Double,
        weather: String,
        theme: String,
        ghostEnabled: Boolean,
        startLat: String,
        startLng: String,
        endLat: String,
        endLng: String
    ) -> Unit
) {
    val context = LocalContext.current
    val viewModel: SoloSetupViewModel = viewModel(factory = SoloSetupViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    
    // Configurations
    var goalType by remember { mutableStateOf(SoloGoalType.DISTANCE) }
    var selectedDistance by remember { mutableDoubleStateOf(5.0) }
    var selectedDuration by remember { mutableDoubleStateOf(30.0) }
    
    var routeShape by remember { mutableStateOf("LOOP") } // "LOOP", "OUT_AND_BACK", "RANDOM_TRAIL", "CUSTOM"
    var paceMode by remember { mutableStateOf("MODERATE") } // "EASY", "MODERATE", "HARD", "CUSTOM"
    var customPaceSlider by remember { mutableFloatStateOf(6.0f) } // min/km
    
    var weatherSimulation by remember { mutableStateOf("SUNNY") } // "SUNNY", "RAINY"
    var themeSimulation by remember { mutableStateOf("LIGHT") } // "LIGHT", "NIGHT"
    var ghostEnabled by remember { mutableStateOf(true) }

    var isFullScreen by remember { mutableStateOf(false) }
    var currentDeviceBearing by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    // Coordinates & Map state
    var startLocation by remember { mutableStateOf<LatLng?>(null) }
    var destinationLocation by remember { mutableStateOf<LatLng?>(null) }
    var currentLoadedStyleUrl by remember { mutableStateOf("") }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

    // Gyroscope / Compass Sensor integration for 3D Map tilt & bearing orientation
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_ROTATION_VECTOR)
        
        val sensorListener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == android.hardware.Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    android.hardware.SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientationValues = FloatArray(3)
                    android.hardware.SensorManager.getOrientation(rotationMatrix, orientationValues)
                    val azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                    currentDeviceBearing = (azimuth + 360) % 360
                }
            }
            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }
        
        rotationSensor?.let {
            sensorManager.registerListener(sensorListener, it, android.hardware.SensorManager.SENSOR_DELAY_UI)
        }
        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    val actualPace = when (paceMode) {
        "EASY" -> 7.0
        "MODERATE" -> 5.5
        "HARD" -> 4.5
        else -> customPaceSlider.toDouble()
    }

    // Permission launcher for location
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.fetchUserLocation()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchUserLocation()
    }

    // Centering Map on user location when updated
    LaunchedEffect(uiState.userLocation, mapLibreMap) {
        uiState.userLocation?.let {
            val userLatLng = LatLng(it.latitude, it.longitude)
            startLocation = userLatLng
            mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15.0))
        }
    }

    // Regerenate generated routes automatically when selections change
    LaunchedEffect(startLocation, destinationLocation, routeShape, selectedDistance, selectedDuration, goalType, actualPace) {
        val start = startLocation ?: return@LaunchedEffect
        val targetDist = if (goalType == SoloGoalType.DISTANCE) selectedDistance else {
            selectedDuration / actualPace
        }

        if (routeShape != "CUSTOM") {
            val points = when (routeShape) {
                "LOOP" -> RouteLoopGenerator.generateLoop(start.latitude, start.longitude, targetDist)
                "OUT_AND_BACK" -> RouteLoopGenerator.generateOutAndBack(start.latitude, start.longitude, targetDist)
                else -> RouteLoopGenerator.generateRandomTrail(start.latitude, start.longitude, targetDist)
            }
            viewModel.setComputedRoute(points, targetDist, actualPace)
        } else {
            destinationLocation?.let { dest ->
                viewModel.calculateCustomRoute(start.latitude, start.longitude, dest.latitude, dest.longitude)
            }
        }
    }

    // Map style JSON (OSM base map styles)
    val mapStyleUrl = remember(themeSimulation) {
        val tileUrl = if (themeSimulation == "NIGHT") {
            "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json"
        } else {
            "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
        }
        """
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
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Map View
        val mapView = remember { MapView(context) }

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

        // Render Map
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                if (mapLibreMap == null) {
                    view.getMapAsync { map ->
                        mapLibreMap = map
                        map.setStyle(Style.Builder().fromJson(mapStyleUrl)) {
                            currentLoadedStyleUrl = mapStyleUrl
                            val loc = startLocation ?: LatLng(28.6139, 77.2090) // Fallback default
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.0))
                        }

                        // Tapping on map sets custom destination
                        map.addOnMapClickListener { point ->
                            destinationLocation = point
                            routeShape = "CUSTOM"
                            true
                        }
                    }
                } else {
                    val map = mapLibreMap!!
                    if (currentLoadedStyleUrl != mapStyleUrl) {
                        map.setStyle(Style.Builder().fromJson(mapStyleUrl)) {
                            currentLoadedStyleUrl = mapStyleUrl
                        }
                    }
                }

                val map = mapLibreMap ?: return@AndroidView
                map.clear()

                // Draw route line
                if (uiState.computedRoute.isNotEmpty()) {
                    map.addPolyline(
                        PolylineOptions()
                            .addAll(uiState.computedRoute)
                            .color(android.graphics.Color.parseColor("#3B82F6"))
                            .width(6f)
                    )
                }

                // Start Marker
                startLocation?.let {
                    map.addMarker(MarkerOptions().position(it).title("START"))
                }

                // Destination Marker
                destinationLocation?.let {
                    map.addMarker(MarkerOptions().position(it).title("DESTINATION"))
                }
            }
        )

        // Floating Header: Search and Back
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Back Button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Airbnb/Uber Style Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchDestination(it)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search run destination...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.95f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            // Search results popup
            if (uiState.searchResults.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .heightIn(max = 240.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    LazyColumn {
                        items(uiState.searchResults) { result ->
                            ListItem(
                                headlineContent = { Text(result.name, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                supportingContent = { Text(result.address, fontSize = 12.sp, maxLines = 1) },
                                modifier = Modifier
                                    .clickable {
                                        destinationLocation = LatLng(result.location.lat, result.location.lng)
                                        routeShape = "CUSTOM"
                                        searchQuery = result.name
                                        viewModel.clearSearch()
                                        mapLibreMap?.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(result.location.lat, result.location.lng),
                                                15.0
                                            )
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }

        // Floating Controls Column (Fullscreen toggle & My Location)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = if (isFullScreen) 32.dp else 360.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gyro Bearing orient / Fullscreen Toggle
            IconButton(
                onClick = { isFullScreen = !isFullScreen },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isFullScreen) com.jogpal.app.ui.theme.JogpalPrimary else Color.White.copy(alpha = 0.9f))
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Toggle Fullscreen Map",
                    tint = Color.Black
                )
            }

            // Floating Center Location button
            IconButton(
                onClick = {
                    if (uiState.userLocation != null) {
                        val p = LatLng(uiState.userLocation!!.latitude, uiState.userLocation!!.longitude)
                        startLocation = p
                        val cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                            .target(p)
                            .zoom(16.0)
                            .bearing(currentDeviceBearing.toDouble())
                            .tilt(30.0)
                            .build()
                        mapLibreMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                        viewModel.fetchUserLocation()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location", tint = Color.Black)
            }
        }

        // Airbnb/Uber Style Slide-up Sheet Panel
        if (!isFullScreen) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(340.dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Drag handle bar
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Heading with Quick Estimates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Solo Run Planning",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    
                    Text(
                        String.format(Locale.getDefault(), "Est: %.1f km (approx %d min)", uiState.estimatedDistanceKm, uiState.estimatedDurationMinutes),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 1: Route Shapes
                Text("Route Shape Layout", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShapeChip(label = "Loop", selected = routeShape == "LOOP") { routeShape = "LOOP" }
                    ShapeChip(label = "Out & Back", selected = routeShape == "OUT_AND_BACK") { routeShape = "OUT_AND_BACK" }
                    ShapeChip(label = "Trail", selected = routeShape == "RANDOM_TRAIL") { routeShape = "RANDOM_TRAIL" }
                    ShapeChip(label = "Custom Direct", selected = routeShape == "CUSTOM") { routeShape = "CUSTOM" }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 2: Goals (Distance or Duration sliders)
                Text("Target Goal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = goalType == SoloGoalType.DISTANCE, onClick = { goalType = SoloGoalType.DISTANCE })
                        Text("Distance", fontSize = 14.sp, color = Color.Black)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = goalType == SoloGoalType.DURATION, onClick = { goalType = SoloGoalType.DURATION })
                        Text("Duration", fontSize = 14.sp, color = Color.Black)
                    }
                }

                if (goalType == SoloGoalType.DISTANCE) {
                    Column {
                        Text(String.format(Locale.getDefault(), "Target Distance: %.1f km", selectedDistance), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Slider(
                            value = selectedDistance.toFloat(),
                            onValueChange = { selectedDistance = it.toDouble() },
                            valueRange = 1.0f..25.0f,
                            steps = 48
                        )
                    }
                } else {
                    Column {
                        Text(String.format(Locale.getDefault(), "Target Time: %.0f minutes", selectedDuration), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Slider(
                            value = selectedDuration.toFloat(),
                            onValueChange = { selectedDuration = it.toDouble() },
                            valueRange = 5.0f..180.0f,
                            steps = 35
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 3: Target Pace & Pacing Ghost
                Text("Target Pace & Virtual Partner", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShapeChip(label = "Easy", selected = paceMode == "EASY") { paceMode = "EASY" }
                    ShapeChip(label = "Moderate", selected = paceMode == "MODERATE") { paceMode = "MODERATE" }
                    ShapeChip(label = "Hard", selected = paceMode == "HARD") { paceMode = "HARD" }
                    ShapeChip(label = "Custom", selected = paceMode == "CUSTOM") { paceMode = "CUSTOM" }
                }

                if (paceMode == "CUSTOM") {
                    Column {
                        Text(String.format(Locale.getDefault(), "Custom Pace: %.1f min/km", customPaceSlider), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Slider(
                            value = customPaceSlider,
                            onValueChange = { customPaceSlider = it },
                            valueRange = 3.5f..10.0f,
                            steps = 13
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Virtual Partner (Ghost Runner)", fontSize = 14.sp, color = Color.Black)
                    Switch(checked = ghostEnabled, onCheckedChange = { ghostEnabled = it })
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 4: Weather & Theme Customizer
                Text("Environment Simulator", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Weather: ", fontSize = 14.sp, color = Color.Black)
                        ShapeChip(label = "Sunny", selected = weatherSimulation == "SUNNY") { weatherSimulation = "SUNNY" }
                        Spacer(modifier = Modifier.width(6.dp))
                        ShapeChip(label = "Rainy", selected = weatherSimulation == "RAINY") { weatherSimulation = "RAINY" }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Map: ", fontSize = 14.sp, color = Color.Black)
                        ShapeChip(label = "Light", selected = themeSimulation == "LIGHT") { themeSimulation = "LIGHT" }
                        Spacer(modifier = Modifier.width(6.dp))
                        ShapeChip(label = "Night", selected = themeSimulation == "NIGHT") { themeSimulation = "NIGHT" }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // CTA Launch Button
                JogpalButton(
                    text = "Go Solo Running",
                    onClick = {
                        val start = startLocation ?: LatLng(0.0, 0.0)
                        val end = destinationLocation ?: start
                        
                        val targetGoalValue = if (goalType == SoloGoalType.DISTANCE) selectedDistance else selectedDuration
                        
                        onStartSoloRun(
                            goalType.name,
                            targetGoalValue,
                            routeShape,
                            paceMode,
                            actualPace,
                            weatherSimulation,
                            themeSimulation,
                            ghostEnabled,
                            start.latitude.toString(),
                            start.longitude.toString(),
                            end.latitude.toString(),
                            end.longitude.toString()
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                )
            }
        }
    }
}
}

@Composable
private fun ShapeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFF3F4F6),
        contentColor = if (selected) Color.Black else Color.DarkGray,
        modifier = Modifier.height(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp)) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
