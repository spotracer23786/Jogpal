package com.jogpal.app.features.profile

import com.jogpal.app.domain.user.UserProfile

sealed interface ProfileUiState {
    object Idle : ProfileUiState
    object Loading : ProfileUiState
    data class Success(val profile: UserProfile?) : ProfileUiState
    object SaveSuccess : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}
