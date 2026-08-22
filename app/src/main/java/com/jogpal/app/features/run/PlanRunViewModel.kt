package com.jogpal.app.features.run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jogpal.app.domain.auth.AuthRepository
import com.jogpal.app.domain.location.LocationRepository
import com.jogpal.app.domain.profile.ProfileRepository
import com.jogpal.app.domain.run.*
import com.jogpal.app.domain.user.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlanRunUiState(
    val isLoading: Boolean = false,
    val partnerProfile: UserProfile? = null,
    val userLocation: com.jogpal.app.domain.user.UserLocation? = null,
    val routeResult: RouteResult? = null,
    val routeAlternatives: List<RouteResult> = emptyList(),
    val searchResults: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class PlanRunViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val locationRepository: LocationRepository,
    private val routeRepository: RouteRepository,
    private val runRepository: RunRepository,
    private val geocodingRepository: GeocodingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanRunUiState())
    val uiState: StateFlow<PlanRunUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun loadInitialData(partnerUid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val partnerResult = profileRepository.getDiscoveryProfile(partnerUid)
            val locationResult = locationRepository.getCurrentLocation()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                partnerProfile = partnerResult.getOrNull(),
                userLocation = locationResult.getOrNull(),
                error = if (partnerResult.isFailure) "Failed to load partner" else null
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
            delay(500) // Debounce
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

    fun selectSearchResult(result: SearchResult) {
        _uiState.value = _uiState.value.copy(searchResults = emptyList())
        selectDestination(result.location.lat, result.location.lng, result.name)
    }

    fun selectAlternative(route: RouteResult) {
        _uiState.value = _uiState.value.copy(routeResult = route)
    }

    fun selectDestination(lat: Double, lng: Double, name: String? = null) {
        val start = _uiState.value.userLocation ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            routeRepository.calculateRoute(
                startLat = start.latitude,
                startLng = start.longitude,
                endLat = lat,
                endLng = lng,
                alternatives = true
            ).fold(
                onSuccess = { results ->
                    val primary = if (name != null) results.firstOrNull()?.copy(destinationName = name) else results.firstOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, 
                        routeResult = primary,
                        routeAlternatives = results,
                        error = null
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, 
                        error = it.message ?: "Route calculation failed",
                        routeResult = null,
                        routeAlternatives = emptyList()
                    )
                }
            )
        }
    }

    fun sendInvitation(
        date: String,
        time: String,
        distance: Double,
        pace: String,
        title: String,
        notes: String
    ) {
        val currentUid = authRepository.currentUserUid ?: return
        val partnerUid = _uiState.value.partnerProfile?.uid ?: return
        val route = _uiState.value.routeResult ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val plan = RunPlan(
                creatorUid = currentUid,
                partnerUid = partnerUid,
                date = date,
                startTime = time,
                distanceKm = distance,
                pace = pace,
                title = title,
                notes = notes,
                startLat = route.startLat,
                startLng = route.startLng,
                endLat = route.endLat,
                endLng = route.endLng,
                destinationName = route.destinationName,
                encodedPolyline = route.encodedPolyline,
                estimatedDurationMinutes = route.durationMinutes
            )
            runRepository.createRunPlan(plan).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }
}
