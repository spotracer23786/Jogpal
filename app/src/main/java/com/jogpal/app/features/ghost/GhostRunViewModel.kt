package com.jogpal.app.features.ghost

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jogpal.app.core.common.LocationUtils
import com.jogpal.app.data.ghost.GhostRepository
import com.jogpal.app.domain.ghost.GhostRun
import com.jogpal.app.domain.model.GeoPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import java.util.Locale

data class GhostActiveState(
    val ghostRun: GhostRun? = null,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedTimeSeconds: Long = 0,
    val userDistanceKm: Double = 0.0,
    val userCurrentPace: String = "0:00/km",
    val userLocation: LatLng? = null,
    val userPath: List<LatLng> = emptyList(),

    val ghostDistanceKm: Double = 0.0,
    val ghostLocation: LatLng? = null,

    val distanceGapMeters: Int = 0, // positive = user ahead, negative = ghost ahead
    val isUserAhead: Boolean = false,
    val statusMessage: String = "👻 Race Started!",
    val isOvertakingEvent: Boolean = false,
    val isFinished: Boolean = false
)

class GhostRunViewModel(
    private val context: Context,
    private val ghostRepository: GhostRepository = GhostRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GhostActiveState())
    val uiState: StateFlow<GhostActiveState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var wasUserAheadLast = false

    fun initGhostRun(ghostId: String, currentRuns: List<GhostRun>) {
        val ghost = ghostRepository.getGhostRunById(ghostId, currentRuns)
        _uiState.update { it.copy(ghostRun = ghost) }
    }

    fun startRace() {
        val ghost = _uiState.value.ghostRun ?: return
        _uiState.update {
            it.copy(
                isRunning = true,
                isPaused = false,
                ghostLocation = ghost.waypoints.firstOrNull()?.let { p -> LatLng(p.lat, p.lng) }
            )
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isRunning && !_uiState.value.isPaused && !_uiState.value.isFinished) {
                delay(1000L)
                tickSecond()
            }
        }
    }

    fun togglePause() {
        val currentPause = _uiState.value.isPaused
        _uiState.update { it.copy(isPaused = !currentPause) }
        if (!currentPause) {
            timerJob?.cancel()
        } else {
            startRace()
        }
    }

    fun updateSimulatedUserLocation(lat: Double, lng: Double) {
        val loc = LatLng(lat, lng)
        val currentPath = _uiState.value.userPath.toMutableList()

        var addedDistance = 0.0
        if (currentPath.isNotEmpty()) {
            val last = currentPath.last()
            addedDistance = LocationUtils.calculateDistanceKm(last.latitude, last.longitude, lat, lng)
        }
        currentPath.add(loc)

        val newDistance = _uiState.value.userDistanceKm + addedDistance

        _uiState.update {
            it.copy(
                userLocation = loc,
                userPath = currentPath,
                userDistanceKm = newDistance
            )
        }
    }

    private fun tickSecond() {
        val ghost = _uiState.value.ghostRun ?: return
        val newElapsed = _uiState.value.elapsedTimeSeconds + 1

        // Calculate ghost distance progress based on original time
        val totalGhostSeconds = ghost.durationSeconds.coerceAtLeast(1L)
        val ghostFraction = (newElapsed.toDouble() / totalGhostSeconds).coerceIn(0.0, 1.0)
        val ghostDistance = ghostFraction * ghost.distanceKm

        // Ghost location along waypoints
        val ghostLoc = calculateGhostLocation(ghost.waypoints, ghostFraction)

        // Calculate User distance (simulated or real increments)
        val userSpeedKmPerSec = (ghost.distanceKm / (ghost.durationSeconds * 0.95))
        val currentDistance = if (_uiState.value.userPath.isEmpty()) {
            (newElapsed * userSpeedKmPerSec).coerceAtMost(ghost.distanceKm * 1.05)
        } else {
            _uiState.value.userDistanceKm
        }

        // Calculate current pace
        val paceStr = if (currentDistance > 0.05) {
            val totalMinutes = newElapsed / 60.0
            val pacePerKm = totalMinutes / currentDistance
            val mins = pacePerKm.toInt()
            val secs = ((pacePerKm - mins) * 60).toInt().coerceIn(0, 59)
            String.format(Locale.US, "%d:%02d/km", mins, secs)
        } else {
            "0:00/km"
        }

        // Distance gap in meters
        val gapKm = currentDistance - ghostDistance
        val gapMeters = (gapKm * 1000).toInt()
        val isAhead = gapMeters >= 0

        // Detect overtake transition
        val isOvertake = isAhead && !wasUserAheadLast
        wasUserAheadLast = isAhead

        // Status messages
        val status = when {
            isOvertake -> "🔥 YOU OVERTOOK YOUR SHADOW!"
            gapMeters > 100 -> "🔥 You're ${gapMeters}m ahead!"
            gapMeters in 1..100 -> "🔥 You're ${gapMeters}m ahead"
            gapMeters in -50..0 -> "👻 Your shadow is pulling away (${Math.abs(gapMeters)}m behind)"
            else -> "👻 ${Math.abs(gapMeters)}m behind shadow"
        }

        // Check completion condition
        val finished = currentDistance >= ghost.distanceKm || newElapsed >= (totalGhostSeconds + 300)

        _uiState.update {
            it.copy(
                elapsedTimeSeconds = newElapsed,
                userDistanceKm = currentDistance,
                userCurrentPace = paceStr,
                ghostDistanceKm = ghostDistance,
                ghostLocation = ghostLoc ?: it.ghostLocation,
                distanceGapMeters = gapMeters,
                isUserAhead = isAhead,
                statusMessage = status,
                isOvertakingEvent = isOvertake,
                isFinished = finished
            )
        }
    }

    private fun calculateGhostLocation(waypoints: List<GeoPoint>, fraction: Double): LatLng? {
        if (waypoints.isEmpty()) return null
        if (fraction <= 0.0) return LatLng(waypoints.first().lat, waypoints.first().lng)
        if (fraction >= 1.0) return LatLng(waypoints.last().lat, waypoints.last().lng)

        val totalPoints = waypoints.size
        val exactIndex = fraction * (totalPoints - 1)
        val index = exactIndex.toInt().coerceIn(0, totalPoints - 2)
        val segmentFraction = exactIndex - index

        val p1 = waypoints[index]
        val p2 = waypoints[index + 1]

        val lat = p1.lat + (p2.lat - p1.lat) * segmentFraction
        val lng = p1.lng + (p2.lng - p1.lng) * segmentFraction
        return LatLng(lat, lng)
    }

    fun finishRace() {
        timerJob?.cancel()
        _uiState.update { it.copy(isFinished = true, isRunning = false) }
    }
}

class GhostRunViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GhostRunViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GhostRunViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
