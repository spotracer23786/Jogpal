package com.jogpal.app.features.matching

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jogpal.app.domain.matching.Match
import com.jogpal.app.domain.matching.MatchingRepository
import com.jogpal.app.domain.matching.RunRequest
import com.jogpal.app.domain.profile.ProfileRepository
import com.jogpal.app.domain.user.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MatchDisplayModel(
    val match: Match,
    val partnerProfile: UserProfile?
)

@OptIn(ExperimentalCoroutinesApi::class)
class MatchingViewModel(
    private val matchingRepository: MatchingRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val TAG = "MatchingVM"

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val sentRequests: StateFlow<List<RunRequest>> = matchingRepository.getSentRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val receivedRequests: StateFlow<List<RunRequest>> = matchingRepository.getReceivedRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Enhanced Matches stream with logging
    val matches: StateFlow<List<MatchDisplayModel>> = matchingRepository.getMatches()
        .flatMapLatest { matchList ->
            flow {
                Log.d(TAG, "Updating matches list: ${matchList.size} matches found")
                val currentUid = matchingRepository.currentUserUid ?: ""
                val enrichedMatches = matchList.map { match ->
                    val partnerUid = match.participantUids.find { it != currentUid } ?: ""
                    val profile = if (partnerUid.isNotEmpty()) {
                        profileRepository.getDiscoveryProfile(partnerUid).getOrNull()
                    } else null
                    MatchDisplayModel(match, profile)
                }
                emit(enrichedMatches)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            Log.d(TAG, "UI Trigger: Accept request $requestId")
            matchingRepository.acceptRequest(requestId).fold(
                onSuccess = {
                    Log.d(TAG, "Successfully accepted request")
                },
                onFailure = {
                    Log.e(TAG, "Failed to accept request", it)
                    _error.value = "Accept failed: ${it.message}"
                }
            )
        }
    }

    fun declineRequest(requestId: String) {
        viewModelScope.launch {
            Log.d(TAG, "UI Trigger: Decline request $requestId")
            matchingRepository.declineRequest(requestId).onFailure {
                Log.e(TAG, "Failed to decline request", it)
                _error.value = "Decline failed: ${it.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
