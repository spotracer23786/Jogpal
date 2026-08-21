package com.jogpal.app.features.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jogpal.app.data.discovery.DiscoveryRepositoryImpl
import com.jogpal.app.domain.discovery.DiscoveryRepository

class DiscoveryViewModelFactory(
    private val discoveryRepository: DiscoveryRepository = DiscoveryRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiscoveryViewModel::class.java)) {
            return DiscoveryViewModel(discoveryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
