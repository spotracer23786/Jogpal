package com.jogpal.app.features.run

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.jogpal.app.core.designsystem.components.NeonMeshCard
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
    var selectedDistance by remember { mutableDoubleStateOf(2.4) }
    var selectedDuration by remember { mutableDoubleStateOf(20.0) }
    
    var routeShape by remember { mutableStateOf("LOOP") } // "LOOP", "OUT_AND_BACK", "RANDOM_TRAIL", "CUSTOM"
    var paceMode by remember { mutableStateOf("MODERATE") } // "EASY", "MODERATE", "HARD", "CUSTOM"
    var customPaceSlider by remember { mutableFloatStateOf(5.5f) } // min/km
    
    var weatherSimulation by remember { mutableStateOf("SUNNY") } // "SUNNY", "RAINY"
    var themeSimulation by remember { mutableStateOf("NIGHT") } // "LIGHT", "NIGHT"
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

    // Regenerate generated routes automatically when selections change
    LaunchedEffect(startLocation, destinationLocation, routeShape, selectedDistance, selectedDuration, goalType, actualPace) {
        val start = startLocation ?: return@LaunchedEffect
        val targetDist = when (goalType) {
            SoloGoalType.DISTANCE -> selectedDistance
            SoloGoalType.DURATION -> selectedDuration / actualPace
            SoloGoalType.FREE -> selectedDistance
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

    // OSM Map Style
    val mapStyleUrl = remember(themeSimulation) {
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
              "maxzoom": 19,
              "paint": {
                "raster-opacity": 0.85,
                "raster-brightness-max": 0.65,
                "raster-contrast": 0.3,
                "raster-saturation": -0.7
              }
            }
          ]
        }
        """.trimIndent()
    }

    val neonLime = Color(0xFFC8FF00)

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E1E))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "START RUN",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(42.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- SECTION 1: RUN MODE ---
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "RUN MODE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray,
                    letterSpacing = 1.5.sp
                )

                // High Impact SOLO RUN Neon Hero Card (Matching reference design)
                NeonMeshCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = neonLime,
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // Left Black Inner Card with Neon Runner Icon
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFF0D0D0D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = "Solo Runner",
                                tint = neonLime,
                                modifier = Modifier.size(64.dp)
                            )
                        }

                        // Middle Content Text
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SOLO RUN",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "FOCUS & ENDURANCE.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = "Run at your own pace.",
                                fontSize = 12.sp,
                                color = Color(0xFF333333)
                            )
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Checkmark Indicator
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = neonLime,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 2: GOAL TYPE CARD (Matching reference screenshot) ---
            NeonMeshCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = neonLime,
                shape = RoundedCornerShape(30.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Row with Flag Icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF141414)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Goal",
                                tint = neonLime,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "GOAL TYPE: ${goalType.name}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Goal Type Options List
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // 1. DISTANCE Option
                        GoalSelectOptionRow(
                            title = "DISTANCE",
                            valueText = String.format(Locale.getDefault(), "%.1f KM", selectedDistance),
                            isSelected = goalType == SoloGoalType.DISTANCE,
                            onClick = { goalType = SoloGoalType.DISTANCE }
                        )

                        if (goalType == SoloGoalType.DISTANCE) {
                            Slider(
                                value = selectedDistance.toFloat(),
                                onValueChange = { selectedDistance = it.toDouble() },
                                valueRange = 0.5f..25.0f,
                                steps = 48,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.Black,
                                    activeTrackColor = Color.Black,
                                    inactiveTrackColor = Color.Black.copy(alpha = 0.2f)
                                )
                            )
                        }

                        // 2. TIME Option
                        GoalSelectOptionRow(
                            title = "TIME",
                            valueText = String.format(Locale.getDefault(), "%.0f min", selectedDuration),
                            isSelected = goalType == SoloGoalType.DURATION,
                            onClick = { goalType = SoloGoalType.DURATION }
                        )

                        if (goalType == SoloGoalType.DURATION) {
                            Slider(
                                value = selectedDuration.toFloat(),
                                onValueChange = { selectedDuration = it.toDouble() },
                                valueRange = 5.0f..180.0f,
                                steps = 35,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.Black,
                                    activeTrackColor = Color.Black,
                                    inactiveTrackColor = Color.Black.copy(alpha = 0.2f)
                                )
                            )
                        }

                        // 3. FREESTYLE Option
                        GoalSelectOptionRow(
                            title = "FREESTYLE",
                            valueText = "Open Run",
                            isSelected = goalType == SoloGoalType.FREE,
                            onClick = { goalType = SoloGoalType.FREE }
                        )
                    }
                }
            }

            // --- SECTION 3: ROUTE PREVIEW & EXPANDABLE MAP ---
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ROUTE & ENVIRONMENT",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray,
                    letterSpacing = 1.5.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Route Shape Chips
                        Text("Route Shape", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DarkShapeChip("Loop", routeShape == "LOOP") { routeShape = "LOOP" }
                            DarkShapeChip("Out & Back", routeShape == "OUT_AND_BACK") { routeShape = "OUT_AND_BACK" }
                            DarkShapeChip("Trail", routeShape == "RANDOM_TRAIL") { routeShape = "RANDOM_TRAIL" }
                            DarkShapeChip("Custom", routeShape == "CUSTOM") { routeShape = "CUSTOM" }
                        }

                        // Search Destination Box
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.searchDestination(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search location destination...", fontSize = 13.sp, color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = neonLime) },
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1E1E1E),
                                unfocusedContainerColor = Color(0xFF1E1E1E),
                                focusedBorderColor = neonLime,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        // Search results popup
                        if (uiState.searchResults.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 180.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                            ) {
                                LazyColumn {
                                    items(uiState.searchResults) { result ->
                                        ListItem(
                                            headlineContent = { Text(result.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White) },
                                            supportingContent = { Text(result.address, fontSize = 11.sp, color = Color.Gray, maxLines = 1) },
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                            modifier = Modifier.clickable {
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

                        // Map Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black)
                        ) {
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

                            AndroidView(
                                factory = { mapView },
                                modifier = Modifier.fillMaxSize(),
                                update = { view ->
                                    if (mapLibreMap == null) {
                                        view.getMapAsync { map ->
                                            mapLibreMap = map
                                            map.setStyle(Style.Builder().fromJson(mapStyleUrl)) {
                                                currentLoadedStyleUrl = mapStyleUrl
                                                val loc = startLocation ?: LatLng(28.6139, 77.2090)
                                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.0))
                                            }
                                            map.addOnMapClickListener { point ->
                                                destinationLocation = point
                                                routeShape = "CUSTOM"
                                                true
                                            }
                                        }
                                    }
                                    val map = mapLibreMap ?: return@AndroidView
                                    map.clear()

                                    if (uiState.computedRoute.isNotEmpty()) {
                                        map.addPolyline(
                                            PolylineOptions()
                                                .addAll(uiState.computedRoute)
                                                .color(android.graphics.Color.parseColor("#C8FF00"))
                                                .width(7f)
                                        )
                                    }

                                    startLocation?.let {
                                        map.addMarker(MarkerOptions().position(it).title("START"))
                                    }
                                    destinationLocation?.let {
                                        map.addMarker(MarkerOptions().position(it).title("DESTINATION"))
                                    }
                                }
                            )

                            // My Location Floating Button
                            IconButton(
                                onClick = {
                                    if (uiState.userLocation != null) {
                                        val p = LatLng(uiState.userLocation!!.latitude, uiState.userLocation!!.longitude)
                                        startLocation = p
                                        mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(p, 16.0))
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
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E1E1E))
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = "My Location", tint = neonLime)
                            }
                        }

                        // Virtual Partner & Weather row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Virtual Partner (Ghost Runner)", fontSize = 13.sp, color = Color.White)
                            Switch(
                                checked = ghostEnabled,
                                onCheckedChange = { ghostEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = neonLime
                                )
                            )
                        }
                    }
                }
            }

            // --- SECTION 4: BIG NEON GO CTA BUTTON (Matching reference screenshot) ---
            NeonMeshCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                backgroundColor = neonLime,
                shape = RoundedCornerShape(34.dp),
                onClick = {
                    val start = startLocation ?: LatLng(0.0, 0.0)
                    val end = destinationLocation ?: start

                    val targetGoalValue = when (goalType) {
                        SoloGoalType.DISTANCE -> selectedDistance
                        SoloGoalType.DURATION -> selectedDuration
                        SoloGoalType.FREE -> selectedDistance
                    }

                    val startLatStr = String.format(Locale.US, "%.5f", start.latitude)
                    val startLngStr = String.format(Locale.US, "%.5f", start.longitude)
                    val endLatStr = String.format(Locale.US, "%.5f", end.latitude)
                    val endLngStr = String.format(Locale.US, "%.5f", end.longitude)

                    onStartSoloRun(
                        goalType.name,
                        targetGoalValue,
                        routeShape,
                        paceMode,
                        actualPace,
                        weatherSimulation,
                        themeSimulation,
                        ghostEnabled,
                        startLatStr,
                        startLngStr,
                        endLatStr,
                        endLngStr
                    )
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GO",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        letterSpacing = 3.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GoalSelectOptionRow(
    title: String,
    valueText: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) Color.Black.copy(alpha = 0.25f) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )

        Text(
            text = valueText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.Black else Color(0xFF333333)
        )
    }
}

@Composable
private fun DarkShapeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Color(0xFFC8FF00) else Color(0xFF1E1E1E),
        contentColor = if (selected) Color.Black else Color.LightGray,
        modifier = Modifier.height(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}
