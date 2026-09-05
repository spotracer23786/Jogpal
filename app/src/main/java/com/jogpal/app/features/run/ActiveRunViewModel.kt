package com.jogpal.app.features.run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jogpal.app.core.common.LocationUtils
import com.jogpal.app.core.common.PolylineUtils
import com.jogpal.app.domain.auth.AuthRepository
import com.jogpal.app.domain.location.LocationRepository
import com.jogpal.app.domain.profile.ProfileRepository
import com.jogpal.app.domain.run.*
import com.jogpal.app.domain.user.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TrackingStatus {
    IDLE, ACTIVE, PAUSED, FINISHED
}

data class ActiveRunUiState(
    val isLoading: Boolean = false,
    val isRerouting: Boolean = false,
    val plan: RunPlan? = null,
    val partnerProfile: UserProfile? = null,
    val userLocation: com.jogpal.app.domain.user.UserLocation? = null,
    val partnerLocation: LiveLocation? = null,
    val currentRoutePolyline: String? = null,
    val remainingDistanceKm: Double? = null,
    val remainingDurationMinutes: Int? = null,
    val error: String? = null,
    val currentUid: String = "",
    val isFinished: Boolean = false,
    val status: TrackingStatus = TrackingStatus.IDLE,
    val elapsedTimeSeconds: Long = 0,
    val traveledDistanceKm: Double = 0.0,
    val currentPace: String = "--:--",
    val stepsCount: Int = 0,
    val caloriesBurned: Int = 0,
    val finalSummary: RunSummary? = null
)

data class RunSummary(
    val totalDistance: Double,
    val elapsedTimeSeconds: Long,
    val averagePace: String,
    val calories: Int,
    val startTime: Long,
    val finishTime: Long
)

class ActiveRunViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val locationRepository: LocationRepository,
    private val liveRunRepository: LiveRunRepository,
    private val runRepository: RunRepository,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveRunUiState(currentUid = authRepository.currentUserUid ?: ""))
    val uiState: StateFlow<ActiveRunUiState> = _uiState.asStateFlow()

    private var locationJob: Job? = null
    private var partnerLocationJob: Job? = null
    private var timerJob: Job? = null
    private var lastLocation: com.jogpal.app.domain.user.UserLocation? = null

    private val DEVIATION_THRESHOLD_KM = 0.08 // 80 meters

    fun startTracking(runId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            runRepository.getRunPlan(runId).collectLatest { plan ->
                if (plan != null) {
                    val partnerUid = if (plan.creatorUid == _uiState.value.currentUid) plan.partnerUid else plan.creatorUid
                    val partnerProfile = profileRepository.getDiscoveryProfile(partnerUid).getOrNull()
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        plan = plan,
                        partnerProfile = partnerProfile,
                        currentRoutePolyline = _uiState.value.currentRoutePolyline ?: plan.encodedPolyline,
                        remainingDistanceKm = _uiState.value.remainingDistanceKm ?: plan.distanceKm,
                        remainingDurationMinutes = _uiState.value.remainingDurationMinutes ?: plan.estimatedDurationMinutes
                    )
                    
                    if (plan.status == RunStatus.ACTIVE && _uiState.value.status == TrackingStatus.IDLE) {
                        resumeRun()
                        observePartnerLocation(runId, partnerUid)
                    } else if (plan.status == RunStatus.COMPLETED || plan.status == RunStatus.CANCELLED) {
                        stopTracking()
                        _uiState.value = _uiState.value.copy(isFinished = true, status = TrackingStatus.FINISHED)
                    }
                }
            }
        }
    }

    fun pauseRun() {
        _uiState.value = _uiState.value.copy(status = TrackingStatus.PAUSED)
        timerJob?.cancel()
        locationJob?.cancel()
    }

    fun resumeRun() {
        val runId = _uiState.value.plan?.id ?: "SOLO_${System.currentTimeMillis()}"
        _uiState.value = _uiState.value.copy(status = TrackingStatus.ACTIVE)
        
        // Start Timer
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.value = _uiState.value.copy(elapsedTimeSeconds = _uiState.value.elapsedTimeSeconds + 1)
                updatePace()
            }
        }

        // Start Location Updates
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            locationRepository.getLocationUpdates().collect { loc ->
                if (_uiState.value.status == TrackingStatus.ACTIVE) {
                    val distance = calculateIncrementalDistance(loc)
                    val newTotalDist = _uiState.value.traveledDistanceKm + distance
                    // Average stride length ~0.76 meters (1315 steps per km)
                    val steps = (newTotalDist * 1315.0).toInt()
                    // 70kg runner burns ~70 kcal/km
                    val calories = (newTotalDist * 72.5).toInt()

                    _uiState.value = _uiState.value.copy(
                        userLocation = loc,
                        traveledDistanceKm = newTotalDist,
                        stepsCount = steps,
                        caloriesBurned = calories
                    )
                    
                    checkRouteDeviation(loc)

                    liveRunRepository.shareLocation(
                        runId,
                        LiveLocation(uid = _uiState.value.currentUid, latitude = loc.latitude, longitude = loc.longitude)
                    )
                }
            }
        }
    }

    private fun calculateIncrementalDistance(newLoc: com.jogpal.app.domain.user.UserLocation): Double {
        val prev = lastLocation
        lastLocation = newLoc
        if (prev == null || _uiState.value.status != TrackingStatus.ACTIVE) return 0.0
        
        val distKm = LocationUtils.calculateDistanceKm(
            prev.latitude, prev.longitude,
            newLoc.latitude, newLoc.longitude
        )
        
        // Calculate speed in km/h based on timestamp delta
        val timeDeltaHours = (newLoc.updatedAt - prev.updatedAt) / 3600000.0
        if (timeDeltaHours > 0) {
            val speedKmH = distKm / timeDeltaHours
            // Discard movement if speed > 22 km/h (World-record human sprinting limit ~25 km/h over distance)
            if (speedKmH > 22.0) {
                return 0.0
            }
        }
        
        return distKm
    }

    private fun checkRouteDeviation(currentLoc: com.jogpal.app.domain.user.UserLocation) {
        if (_uiState.value.isRerouting || _uiState.value.status != TrackingStatus.ACTIVE) return
        
        val polyline = _uiState.value.currentRoutePolyline ?: return
        val points = PolylineUtils.decodePolyline(polyline)
        if (points.isEmpty()) return

        // Find distance to the nearest point on the polyline
        var minDistance = Double.MAX_VALUE
        for (p in points) {
            val d = LocationUtils.calculateDistanceKm(currentLoc.latitude, currentLoc.longitude, p.lat, p.lng)
            if (d < minDistance) minDistance = d
        }

        if (minDistance > DEVIATION_THRESHOLD_KM) {
            reroute(currentLoc)
        }
    }

    private fun reroute(currentLoc: com.jogpal.app.domain.user.UserLocation) {
        val plan = _uiState.value.plan ?: return
        if (plan.endLat == null || plan.endLng == null) return

        _uiState.value = _uiState.value.copy(isRerouting = true)
        viewModelScope.launch {
            routeRepository.calculateRoute(
                startLat = currentLoc.latitude,
                startLng = currentLoc.longitude,
                endLat = plan.endLat,
                endLng = plan.endLng,
                alternatives = false
            ).fold(
                onSuccess = { results ->
                    val result = results.firstOrNull() ?: return@fold
                    _uiState.value = _uiState.value.copy(
                        currentRoutePolyline = result.encodedPolyline,
                        remainingDistanceKm = result.distanceKm,
                        remainingDurationMinutes = result.durationMinutes,
                        isRerouting = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isRerouting = false)
                }
            )
        }
    }

    private fun updatePace() {
        _uiState.value = _uiState.value.copy(
            currentPace = calculatePace(_uiState.value.traveledDistanceKm, _uiState.value.elapsedTimeSeconds)
        )
    }

    private fun calculatePace(distance: Double, seconds: Long): String {
        if (distance < 0.01 || seconds < 1) return "--:--"
        val totalMinutes = seconds / 60.0
        val paceDecimal = totalMinutes / distance
        val paceMins = paceDecimal.toInt()
        val paceSecs = ((paceDecimal - paceMins) * 60).toInt()
        return String.format("%d:%02d", paceMins, paceSecs)
    }

    private fun observePartnerLocation(runId: String, partnerUid: String) {
        partnerLocationJob?.cancel()
        partnerLocationJob = viewModelScope.launch {
            liveRunRepository.getPartnerLocation(runId, partnerUid).collect { loc ->
                _uiState.value = _uiState.value.copy(partnerLocation = loc)
            }
        }
    }

    fun finishRun() {
        val runId = _uiState.value.plan?.id ?: return
        val distance = _uiState.value.traveledDistanceKm
        val seconds = _uiState.value.elapsedTimeSeconds
        val startTime = _uiState.value.plan?.actualStartTime ?: _uiState.value.plan?.createdAt ?: System.currentTimeMillis()
        val finishTime = System.currentTimeMillis()
        
        // Calorie Formula: Weight(kg) * Distance(km) * 1.036
        // Default weight 70kg
        val calories = (70 * distance * 1.036).toInt()
        
        val summary = RunSummary(
            totalDistance = distance,
            elapsedTimeSeconds = seconds,
            averagePace = calculatePace(distance, seconds),
            calories = calories,
            startTime = startTime,
            finishTime = finishTime
        )

        viewModelScope.launch {
            stopTracking()
            runRepository.completeRun(
                runId = runId,
                actualDistanceKm = distance,
                actualDurationSeconds = seconds,
                calories = calories,
                actualPolyline = _uiState.value.currentRoutePolyline
            ).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        status = TrackingStatus.FINISHED,
                        finalSummary = summary
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = "Failed to save run results")
                }
            )
        }
    }

    private fun stopTracking() {
        _uiState.value = _uiState.value.copy(status = TrackingStatus.FINISHED)
        locationJob?.cancel()
        partnerLocationJob?.cancel()
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }
}
