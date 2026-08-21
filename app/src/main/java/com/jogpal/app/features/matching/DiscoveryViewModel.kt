package com.jogpal.app.features.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jogpal.app.core.common.LocationUtils
import com.jogpal.app.domain.discovery.DiscoveryRepository
import com.jogpal.app.domain.user.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RunnerDisplayModel(
    val profile: UserProfile,
    val distanceKm: Double
)

sealed interface DiscoveryUiState {
    object Idle : DiscoveryUiState
    object Loading : DiscoveryUiState
    data class Success(val runners: List<RunnerDisplayModel>) : DiscoveryUiState
    object Empty : DiscoveryUiState
    data class Error(val message: String) : DiscoveryUiState
}

class DiscoveryViewModel(
    private val discoveryRepository: DiscoveryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoveryUiState>(DiscoveryUiState.Idle)
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    private val DEFAULT_RADIUS_KM = 10.0

    fun loadNearbyRunners(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = DiscoveryUiState.Loading
            discoveryRepository.findNearbyRunners(latitude, longitude, DEFAULT_RADIUS_KM).fold(
                onSuccess = { profiles ->
                    if (profiles.isEmpty()) {
                        _uiState.value = DiscoveryUiState.Empty
                    } else {
                        val displayModels = profiles.map { profile ->
                            val distance = LocationUtils.calculateDistanceKm(
                                latitude, longitude,
                                profile.location?.latitude ?: 0.0,
                                profile.location?.longitude ?: 0.0
                            )
                            RunnerDisplayModel(profile, distance)
                        }
                        _uiState.value = DiscoveryUiState.Success(displayModels)
                    }
                },
                onFailure = {
                    _uiState.value = DiscoveryUiState.Error(it.message ?: "Failed to find runners")
                }
            )
        }
    }
}
