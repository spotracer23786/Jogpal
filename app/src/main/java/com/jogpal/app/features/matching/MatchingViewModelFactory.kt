package com.jogpal.app.features.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jogpal.app.data.matching.MatchingRepositoryImpl
import com.jogpal.app.data.profile.ProfileRepositoryImpl
import com.jogpal.app.domain.matching.MatchingRepository
import com.jogpal.app.domain.profile.ProfileRepository

class MatchingViewModelFactory(
    private val matchingRepository: MatchingRepository = MatchingRepositoryImpl(),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MatchingViewModel::class.java)) {
            return MatchingViewModel(matchingRepository, profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
