package com.jogpal.app.features.run

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jogpal.app.data.auth.AuthRepositoryImpl
import com.jogpal.app.data.location.LocationRepositoryImpl
import com.jogpal.app.data.profile.ProfileRepositoryImpl
import com.jogpal.app.data.run.GeocodingRepositoryImpl
import com.jogpal.app.data.run.LiveRunRepositoryImpl
import com.jogpal.app.data.run.RouteRepositoryImpl
import com.jogpal.app.data.run.RunRepositoryImpl
import com.jogpal.app.data.user.UserRepositoryImpl
import com.jogpal.app.domain.auth.AuthRepository
import com.jogpal.app.domain.location.LocationRepository
import com.jogpal.app.domain.profile.ProfileRepository
import com.jogpal.app.domain.run.GeocodingRepository
import com.jogpal.app.domain.run.LiveRunRepository
import com.jogpal.app.domain.run.RouteRepository
import com.jogpal.app.domain.run.RunRepository

class PlanRunViewModelFactory(
    private val context: Context,
    private val authRepository: AuthRepository = AuthRepositoryImpl(userRepository = UserRepositoryImpl()),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(),
    private val locationRepository: LocationRepository = LocationRepositoryImpl(context),
    private val routeRepository: RouteRepository = RouteRepositoryImpl(),
    private val runRepository: RunRepository = RunRepositoryImpl(),
    private val geocodingRepository: GeocodingRepository = GeocodingRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlanRunViewModel::class.java)) {
            return PlanRunViewModel(authRepository, profileRepository, locationRepository, routeRepository, runRepository, geocodingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class RunDetailViewModelFactory(
    private val authRepository: AuthRepository = AuthRepositoryImpl(userRepository = UserRepositoryImpl()),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(),
    private val runRepository: RunRepository = RunRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RunDetailViewModel::class.java)) {
            return RunDetailViewModel(authRepository, profileRepository, runRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ActiveRunViewModelFactory(
    private val context: Context,
    private val authRepository: AuthRepository = AuthRepositoryImpl(userRepository = UserRepositoryImpl()),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(),
    private val locationRepository: LocationRepository = LocationRepositoryImpl(context),
    private val liveRunRepository: LiveRunRepository = LiveRunRepositoryImpl(),
    private val runRepository: RunRepository = RunRepositoryImpl(),
    private val routeRepository: RouteRepository = RouteRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveRunViewModel::class.java)) {
            return ActiveRunViewModel(authRepository, profileRepository, locationRepository, liveRunRepository, runRepository, routeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class RunHistoryViewModelFactory(
    private val runRepository: RunRepository = RunRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RunHistoryViewModel::class.java)) {
            return RunHistoryViewModel(runRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class RunHistoryDetailViewModelFactory(
    private val runRepository: RunRepository = RunRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RunHistoryDetailViewModel::class.java)) {
            return RunHistoryDetailViewModel(runRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
