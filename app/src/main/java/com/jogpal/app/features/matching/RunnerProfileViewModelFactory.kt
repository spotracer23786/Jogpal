package com.jogpal.app.features.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jogpal.app.data.auth.AuthRepositoryImpl
import com.jogpal.app.data.discovery.DiscoveryRepositoryImpl
import com.jogpal.app.data.matching.MatchingRepositoryImpl
import com.jogpal.app.data.profile.ProfileRepositoryImpl
import com.jogpal.app.data.user.UserRepositoryImpl
import com.jogpal.app.domain.auth.AuthRepository
import com.jogpal.app.domain.discovery.DiscoveryRepository
import com.jogpal.app.domain.matching.MatchingRepository
import com.jogpal.app.domain.profile.ProfileRepository

class RunnerProfileViewModelFactory(
    private val authRepository: AuthRepository = AuthRepositoryImpl(userRepository = UserRepositoryImpl()),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(),
    private val discoveryRepository: DiscoveryRepository = DiscoveryRepositoryImpl(),
    private val matchingRepository: MatchingRepository = MatchingRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RunnerProfileViewModel::class.java)) {
            return RunnerProfileViewModel(
                authRepository,
                profileRepository,
                discoveryRepository,
                matchingRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
