package com.jogpal.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jogpal.app.domain.auth.AuthRepository
import com.jogpal.app.domain.profile.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MainUiState {
    object Loading : MainUiState
    object Unauthenticated : MainUiState
    // Improved Authenticated state with optional data to allow fast-path UI rendering
    data class Authenticated(
        val uid: String, 
        val email: String = "", 
        val name: String = "", 
        val profileCompleted: Boolean = false,
        val isProfileLoading: Boolean = true
    ) : MainUiState
}

class MainViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        viewModelScope.launch {
            val currentUid = authRepository.currentUserUid
            if (currentUid != null) {
                // FAST PATH: Emit authenticated state immediately so UI can transition away from splash/login
                // We assume profile is not completed yet until proven otherwise to be safe
                _uiState.value = MainUiState.Authenticated(uid = currentUid, isProfileLoading = true)
                
                // BACKGROUND: Load the actual profile details
                profileRepository.getProfile(currentUid).fold(
                    onSuccess = { profile ->
                        if (profile != null) {
                            _uiState.value = MainUiState.Authenticated(
                                uid = currentUid,
                                email = profile.email,
                                name = profile.name,
                                profileCompleted = profile.profileCompleted,
                                isProfileLoading = false
                            )
                        } else {
                            // User authenticated but no profile doc yet
                            _uiState.value = MainUiState.Authenticated(currentUid, isProfileLoading = false)
                        }
                    },
                    onFailure = {
                        // Keep the authenticated state but mark as failed to load profile
                        _uiState.value = MainUiState.Authenticated(currentUid, isProfileLoading = false)
                    }
                )
            } else {
                _uiState.value = MainUiState.Unauthenticated
            }
        }
    }
}
