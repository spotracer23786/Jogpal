package com.jogpal.app.features.run

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jogpal.app.domain.auth.AuthRepository
import com.jogpal.app.domain.profile.ProfileRepository
import com.jogpal.app.domain.run.RunPlan
import com.jogpal.app.domain.run.RunRepository
import com.jogpal.app.domain.run.RunStatus
import com.jogpal.app.domain.user.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RunDetailUiState(
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val plan: RunPlan? = null,
    val partnerProfile: UserProfile? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val currentUid: String = ""
)

class RunDetailViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val runRepository: RunRepository
) : ViewModel() {

    private val TAG = "RunDetailVM"
    private val _uiState = MutableStateFlow(RunDetailUiState(currentUid = authRepository.currentUserUid ?: ""))
    val uiState: StateFlow<RunDetailUiState> = _uiState.asStateFlow()

    fun loadRunDetails(runId: String) {
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
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Run not found")
                }
            }
        }
    }

    fun updateStatus(newStatus: RunStatus) {
        val runId = _uiState.value.plan?.id ?: return
        viewModelScope.launch {
            Log.d(TAG, "UI Trigger: Update status of $runId to $newStatus")
            _uiState.value = _uiState.value.copy(isActionLoading = true, error = null, successMessage = null)
            runRepository.updateRunStatus(runId, newStatus).fold(
                onSuccess = {
                    Log.d(TAG, "Update status success")
                    val msg = when(newStatus) {
                        RunStatus.ACCEPTED -> "Run Confirmed ✓"
                        RunStatus.ACTIVE -> "Run Started ●"
                        RunStatus.DECLINED -> "Invitation Declined"
                        RunStatus.CANCELLED -> "Run Cancelled"
                        RunStatus.COMPLETED -> "Run Completed ✓"
                        else -> "Status Updated"
                    }
                    _uiState.value = _uiState.value.copy(isActionLoading = false, successMessage = msg)
                },
                onFailure = {
                    Log.e(TAG, "Update status failed", it)
                    _uiState.value = _uiState.value.copy(isActionLoading = false, error = it.message ?: "Permission Denied")
                }
            )
        }
    }
}
