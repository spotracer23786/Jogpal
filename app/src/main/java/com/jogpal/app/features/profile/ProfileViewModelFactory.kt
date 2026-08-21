package com.jogpal.app.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jogpal.app.data.profile.ProfileRepositoryImpl
import com.jogpal.app.domain.profile.ProfileRepository

class ProfileViewModelFactory(
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
