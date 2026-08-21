package com.jogpal.app.features.tracking

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jogpal.app.data.location.LocationRepositoryImpl
import com.jogpal.app.data.profile.ProfileRepositoryImpl
import com.jogpal.app.domain.location.LocationRepository
import com.jogpal.app.domain.profile.ProfileRepository

class LocationViewModelFactory(
    private val context: Context,
    private val locationRepository: LocationRepository = LocationRepositoryImpl(context),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(locationRepository, profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
