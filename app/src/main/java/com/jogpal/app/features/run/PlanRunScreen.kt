package com.jogpal.app.features.run

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import org.maplibre.android.annotations.IconFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanRunScreen(
    partnerUid: String,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: PlanRunViewModel = viewModel(factory = PlanRunViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()

    val calendar = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) } }
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var selectedHour by remember { mutableIntStateOf(7) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    
    var distance by remember { mutableStateOf("5") }
    var isCustomDistance by remember { mutableStateOf(false) }
    var pace by remember { mutableStateOf("Moderate") }
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val storageFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    LaunchedEffect(partnerUid) {
        viewModel.loadInitialData(partnerUid)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Plan a Run", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        uiState.partnerProfile?.let {
                            Text("with ${it.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel", color = MaterialTheme.colorScheme.error)
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
            // 0. SEARCH SECTION
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        viewModel.searchDestination(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search destination...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                
                if (uiState.searchResults.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .heightIn(max = 200.dp),
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 4.dp,
                        shadowElevation = 8.dp
                    ) {
                        LazyColumn {
                            items(uiState.searchResults) { result ->
                                ListItem(
                                    headlineContent = { Text(result.name) },
                                    supportingContent = { Text(result.address, maxLines = 1) },
                                    modifier = Modifier.clickable {
                                        searchQuery = result.name
                                        viewModel.selectSearchResult(result)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 1. MAP SECTION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                val userLocation = uiState.userLocation
                val routeResult = uiState.routeResult
                
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

                // Auto-zoom to fit route when calculated - in a LaunchedEffect to avoid snapping on every update
                LaunchedEffect(routeResult) {
                    routeResult?.let { route ->
                        val map = mapLibreMap ?: return@LaunchedEffect
                        val dest = LatLng(route.endLat, route.endLng)
                        val points = PolylineUtils.decodePolyline(route.encodedPolyline).toLatLngList()
                        val builder = LatLngBounds.Builder()
                            .include(LatLng(route.startLat, route.startLng))
                            .include(dest)
                        points.forEach { builder.include(it) }
                        
                        try {
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
                        } catch (e: Exception) { }
                    }
                }

                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        if (mapLibreMap == null) {
                            view.getMapAsync { map ->
                                mapLibreMap = map
                                map.uiSettings.isLogoEnabled = false
                                map.uiSettings.isAttributionEnabled = false
                                map.setStyle(Style.Builder().fromJson(osmStyleJson)) { style ->
                                    // Initial center on user
                                    userLocation?.let {
                                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15.0))
                                    }
                                }

                                map.addOnMapClickListener { point ->
                                    if (point.latitude == 0.0 && point.longitude == 0.0) return@addOnMapClickListener false
                                    viewModel.selectDestination(point.latitude, point.longitude)
                                    true
                                }
                            }
                        }
                        
                        val map = mapLibreMap ?: return@AndroidView
                        
                        // Sync markers and polyline
                        map.clear()

                        // Add User Marker (START)
                        userLocation?.let {
                            map.addMarker(MarkerOptions()
                                .position(LatLng(it.latitude, it.longitude))
                                .title("START")
                                .snippet("Your location"))
                        }

                        // Add Alternatives (Unselected first so they are behind)
                        uiState.routeAlternatives.forEach { alt ->
                            if (alt.encodedPolyline != uiState.routeResult?.encodedPolyline) {
                                val points = PolylineUtils.decodePolyline(alt.encodedPolyline).toLatLngList()
                                if (points.isNotEmpty()) {
                                    map.addPolyline(PolylineOptions()
                                        .addAll(points)
                                        .color(android.graphics.Color.LTGRAY)
                                        .width(4f))
                                }
                            }
                        }

                        // Add Destination and Selected Route (FINISH)
                        uiState.routeResult?.let { route ->
                            val dest = LatLng(route.endLat, route.endLng)
                            map.addMarker(MarkerOptions()
                                .position(dest)
                                .title("FINISH")
                                .snippet(route.destinationName))
                            
                            val points = PolylineUtils.decodePolyline(route.encodedPolyline).toLatLngList()
                            if (points.isNotEmpty()) {
                                map.addPolyline(PolylineOptions()
                                    .addAll(points)
                                    .color(android.graphics.Color.parseColor("#007AFF")) // Selected Blue
                                    .width(6f))
                            }
                        }
                    }
                )

                // Current Location Button
                FloatingActionButton(
                    onClick = {
                        userLocation?.let {
                            mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15.0))
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }
                
                // Overlay instructions
                if (uiState.routeResult == null) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.7f)
                    ) {
                        Text(
                            "Tap on map to select destination",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Attribution (Bottom Start)
                Text(
                    "© OpenStreetMap",
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 8.sp
                )
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                // Route Alternatives Selection
                if (uiState.routeAlternatives.size > 1) {
                    SectionHeader("CHOOSE ROUTE")
                    androidx.compose.foundation.lazy.LazyRow(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.routeAlternatives) { route ->
                            val isSelected = route.encodedPolyline == uiState.routeResult?.encodedPolyline
                            Surface(
                                onClick = { viewModel.selectAlternative(route) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.width(140.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = route.destinationName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${route.distanceKm} km", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("${route.durationMinutes} min", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                uiState.routeResult?.let { route ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Distance", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("${route.distanceKm} km", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Est. Time", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("~${route.durationMinutes} min", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    // Update the distance field if route is selected
                    distance = route.distanceKm.toString()
                }

                SectionHeader("WHEN")
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickOptionChip(
                        text = "Today",
                        selected = isSameDay(selectedYear, selectedMonth, selectedDay, Calendar.getInstance()),
                        onClick = {
                            val now = Calendar.getInstance()
                            selectedYear = now.get(Calendar.YEAR)
                            selectedMonth = now.get(Calendar.MONTH)
                            selectedDay = now.get(Calendar.DAY_OF_MONTH)
                        }
                    )
                    QuickOptionChip(
                        text = "Tomorrow",
                        selected = isSameDay(selectedYear, selectedMonth, selectedDay, Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }),
                        onClick = {
                            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                            selectedYear = tomorrow.get(Calendar.YEAR)
                            selectedMonth = tomorrow.get(Calendar.MONTH)
                            selectedDay = tomorrow.get(Calendar.DAY_OF_MONTH)
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Date", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            val displayCal = Calendar.getInstance().apply { set(selectedYear, selectedMonth, selectedDay) }
                            Text(dateFormatter.format(displayCal.time), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Time", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            val displayTime = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, selectedHour)
                                set(Calendar.MINUTE, selectedMinute)
                            }
                            Text(timeFormatter.format(displayTime.time), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                SectionHeader("DISTANCE")
                
                val distances = listOf("2", "3", "5", "7", "10")
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    distances.forEach { d ->
                        FilterChip(
                            selected = distance == d && !isCustomDistance,
                            onClick = { distance = d; isCustomDistance = false },
                            label = { Text("$d km") },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    FilterChip(
                        selected = isCustomDistance,
                        onClick = { isCustomDistance = true },
                        label = { Text("Custom") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                if (isCustomDistance) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = distance,
                        onValueChange = { distance = it },
                        label = { Text("Enter distance (km)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                SectionHeader("PACE")
                val paces = listOf("Easy", "Moderate", "Fast", "Competitive")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    paces.forEach { p ->
                        Box(modifier = Modifier.weight(1f)) {
                            FilterChip(
                                selected = pace == p,
                                onClick = { pace = p },
                                label = { Text(p, fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                SectionHeader("OPTIONAL")
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Run Title (e.g. Morning Jog)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes for your partner") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                if (uiState.error != null) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val buttonText = when {
                    uiState.isLoading -> "Sending..."
                    uiState.isSuccess -> "Invitation Sent ✓"
                    else -> "Send Run Invitation"
                }

                JogpalButton(
                    text = buttonText,
                    onClick = {
                        val finalCal = Calendar.getInstance().apply { set(selectedYear, selectedMonth, selectedDay) }
                        viewModel.sendInvitation(
                            date = storageFormatter.format(finalCal.time),
                            time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute),
                            distance = distance.toDoubleOrNull() ?: 0.0,
                            pace = pace,
                            title = title,
                            notes = notes
                        )
                    },
                    enabled = !uiState.isLoading && !uiState.isSuccess && distance.toDoubleOrNull() != null && uiState.routeResult != null
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance().apply { timeInMillis = it }
                        selectedYear = cal.get(Calendar.YEAR)
                        selectedMonth = cal.get(Calendar.MONTH)
                        selectedDay = cal.get(Calendar.DAY_OF_MONTH)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

private fun isSameDay(year: Int, month: Int, day: Int, target: Calendar): Boolean {
    return year == target.get(Calendar.YEAR) &&
            month == target.get(Calendar.MONTH) &&
            day == target.get(Calendar.DAY_OF_MONTH)
}

@Composable
private fun SectionHeader(title: String) {
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun QuickOptionChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.Black
        )
    )
}
