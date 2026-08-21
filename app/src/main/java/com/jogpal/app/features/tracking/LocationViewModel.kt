package com.jogpal.app.features.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jogpal.app.domain.location.LocationRepository
import com.jogpal.app.domain.profile.ProfileRepository
import com.jogpal.app.domain.user.UserLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LocationUiState {
    object Idle : LocationUiState
    object Loading : LocationUiState
    object PermissionRequired : LocationUiState
    object PermissionDenied : LocationUiState
    object LocationDisabled : LocationUiState
    data class LocationAvailable(val location: UserLocation) : LocationUiState
    data class Error(val message: String) : LocationUiState
}

class LocationViewModel(
    private val locationRepository: LocationRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LocationUiState>(LocationUiState.Idle)
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    fun checkPermissionAndFetchLocation(uid: String) {
        if (!locationRepository.hasLocationPermission()) {
            _uiState.value = LocationUiState.PermissionRequired
        } else if (!locationRepository.isLocationEnabled()) {
            _uiState.value = LocationUiState.LocationDisabled
        } else {
            updateLocation(uid)
        }
    }

    fun updateLocation(uid: String) {
        viewModelScope.launch {
            _uiState.value = LocationUiState.Loading
            locationRepository.getCurrentLocation().fold(
                onSuccess = { location ->
                    saveLocationToProfile(uid, location)
                    _uiState.value = LocationUiState.LocationAvailable(location)
                },
                onFailure = { error ->
                    when {
                        error is SecurityException -> {
                            _uiState.value = LocationUiState.PermissionRequired
                        }
                        error.message == "LOCATION_DISABLED" -> {
                            _uiState.value = LocationUiState.LocationDisabled
                        }
                        else -> {
                            _uiState.value = LocationUiState.Error(error.message ?: "Failed to get location")
                        }
                    }
                }
            )
        }
    }

    private suspend fun saveLocationToProfile(uid: String, location: UserLocation) {
        profileRepository.getProfile(uid).onSuccess { profile ->
            if (profile != null) {
                val updatedProfile = profile.copy(
                    location = location,
                    locationSharingEnabled = true,
                    updatedAt = System.currentTimeMillis()
                )
                profileRepository.updateProfile(updatedProfile)
            }
        }
    }

    fun onPermissionDenied() {
        _uiState.value = LocationUiState.PermissionDenied
    }

    fun resetToIdle() {
        _uiState.value = LocationUiState.Idle
    }
}
