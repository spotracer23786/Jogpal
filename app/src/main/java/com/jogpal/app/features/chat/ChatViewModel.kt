package com.jogpal.app.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jogpal.app.domain.chat.ChatMessage
import com.jogpal.app.domain.chat.ChatRepository
import com.jogpal.app.domain.profile.ProfileRepository
import com.jogpal.app.domain.user.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val partnerProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    private val partnerUid: String,
    private val chatRepository: ChatRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadPartnerProfile()
        observeMessages()
    }

    private fun loadPartnerProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            profileRepository.getDiscoveryProfile(partnerUid).fold(
                onSuccess = { profile ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        partnerProfile = profile
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load partner profile"
                    )
                }
            )
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.getMessages(partnerUid).collectLatest { messageList ->
                _uiState.value = _uiState.value.copy(messages = messageList)
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(partnerUid, content).onFailure {
                _uiState.value = _uiState.value.copy(error = it.message ?: "Failed to send message")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
