package com.jogpal.app.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jogpal.app.domain.profile.ProfileRepository
import com.jogpal.app.domain.user.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(uid: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            profileRepository.getProfile(uid).fold(
                onSuccess = { _uiState.value = ProfileUiState.Success(it) },
                onFailure = { _uiState.value = ProfileUiState.Error(it.message ?: "Failed to load profile") }
            )
        }
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val updatedProfile = profile.copy(
                profileCompleted = true,
                updatedAt = System.currentTimeMillis()
            )
            profileRepository.updateProfile(updatedProfile).fold(
                onSuccess = { _uiState.value = ProfileUiState.SaveSuccess },
                onFailure = { _uiState.value = ProfileUiState.Error(it.message ?: "Failed to save profile") }
            )
        }
    }
}
