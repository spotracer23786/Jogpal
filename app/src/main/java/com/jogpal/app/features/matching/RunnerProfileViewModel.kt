package com.jogpal.app.features.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jogpal.app.core.common.CompatibilityEngine
import com.jogpal.app.domain.auth.AuthRepository
import com.jogpal.app.domain.discovery.DiscoveryRepository
import com.jogpal.app.domain.matching.MatchingRepository
import com.jogpal.app.domain.matching.RequestStatus
import com.jogpal.app.domain.matching.RunRequest
import com.jogpal.app.domain.profile.ProfileRepository
import com.jogpal.app.domain.user.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RunnerProfileUiState(
    val isLoading: Boolean = false,
    val targetProfile: UserProfile? = null,
    val compatibilityScore: Int = 0,
    val existingRequest: RunRequest? = null,
    val error: String? = null,
    val requestSent: Boolean = false
)

class RunnerProfileViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val discoveryRepository: DiscoveryRepository,
    private val matchingRepository: MatchingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RunnerProfileUiState())
    val uiState: StateFlow<RunnerProfileUiState> = _uiState.asStateFlow()

    fun loadRunnerProfile(targetUid: String) {
        val currentUid = authRepository.currentUserUid ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val userResult = profileRepository.getProfile(currentUid)
            val targetResult = profileRepository.getDiscoveryProfile(targetUid)
            val requestResult = matchingRepository.getRequestBetweenUsers(currentUid, targetUid)

            if (userResult.isSuccess && targetResult.isSuccess) {
                val user = userResult.getOrNull()!!
                val target = targetResult.getOrNull()
                
                if (target != null) {
                    val score = CompatibilityEngine.calculateScore(user, target)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        targetProfile = target,
                        compatibilityScore = score,
                        existingRequest = requestResult.getOrNull()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Runner profile not found"
                    )
                }
            } else {
                val errorMsg = targetResult.exceptionOrNull()?.message ?: userResult.exceptionOrNull()?.message ?: "Unknown error"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load runner profile: $errorMsg"
                )
            }
        }
    }

    fun sendRunRequest() {
        val target = _uiState.value.targetProfile ?: return
        val score = _uiState.value.compatibilityScore
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            matchingRepository.sendRequest(target.uid, score).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, requestSent = true)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = RunnerProfileUiState()
    }
}
