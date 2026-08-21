package com.jogpal.app.features.run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jogpal.app.domain.auth.AuthRepository
import com.jogpal.app.domain.location.LocationRepository
import com.jogpal.app.domain.profile.ProfileRepository
import com.jogpal.app.domain.run.*
import com.jogpal.app.domain.user.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ActiveRunUiState(
    val isLoading: Boolean = false,
    val plan: RunPlan? = null,
    val partnerProfile: UserProfile? = null,
    val userLocation: com.jogpal.app.domain.user.UserLocation? = null,
    val partnerLocation: LiveLocation? = null,
    val error: String? = null,
    val currentUid: String = "",
    val isFinished: Boolean = false
)

class ActiveRunViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val locationRepository: LocationRepository,
    private val liveRunRepository: LiveRunRepository,
    private val runRepository: RunRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveRunUiState(currentUid = authRepository.currentUserUid ?: ""))
    val uiState: StateFlow<ActiveRunUiState> = _uiState.asStateFlow()

    private var locationJob: Job? = null
    private var partnerLocationJob: Job? = null

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
                        partnerProfile = partnerProfile
                    )
                    
                    if (plan.status == RunStatus.ACTIVE) {
                        observeLocations(runId, partnerUid)
                    } else if (plan.status == RunStatus.COMPLETED || plan.status == RunStatus.CANCELLED) {
                        stopTracking()
                        _uiState.value = _uiState.value.copy(isFinished = true)
                    }
                }
            }
        }
    }

    private fun observeLocations(runId: String, partnerUid: String) {
        // 1. Share my location
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            locationRepository.getLocationUpdates().collect { loc ->
                _uiState.value = _uiState.value.copy(userLocation = loc)
                liveRunRepository.shareLocation(
                    runId,
                    LiveLocation(uid = _uiState.value.currentUid, latitude = loc.latitude, longitude = loc.longitude)
                )
            }
        }

        // 2. Observe partner location
        partnerLocationJob?.cancel()
        partnerLocationJob = viewModelScope.launch {
            liveRunRepository.getPartnerLocation(runId, partnerUid).collect { loc ->
                _uiState.value = _uiState.value.copy(partnerLocation = loc)
            }
        }
    }

    fun finishRun() {
        val runId = _uiState.value.plan?.id ?: return
        viewModelScope.launch {
            runRepository.updateRunStatus(runId, RunStatus.COMPLETED)
        }
    }

    private fun stopTracking() {
        locationJob?.cancel()
        partnerLocationJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }
}
