package com.jogpal.app.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jogpal.app.data.chat.ChatRepositoryImpl
import com.jogpal.app.data.profile.ProfileRepositoryImpl
import com.jogpal.app.domain.chat.ChatRepository
import com.jogpal.app.domain.profile.ProfileRepository

class ChatViewModelFactory(
    private val partnerUid: String,
    private val chatRepository: ChatRepository = ChatRepositoryImpl(),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(partnerUid, chatRepository, profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
