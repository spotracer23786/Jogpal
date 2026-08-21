package com.jogpal.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jogpal.app.data.auth.AuthRepositoryImpl
import com.jogpal.app.data.profile.ProfileRepositoryImpl
import com.jogpal.app.data.user.UserRepositoryImpl
import com.jogpal.app.domain.auth.AuthRepository
import com.jogpal.app.domain.profile.ProfileRepository

class MainViewModelFactory(
    private val authRepository: AuthRepository = AuthRepositoryImpl(userRepository = UserRepositoryImpl()),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(authRepository, profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
