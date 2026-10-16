package com.plantcareai.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.plantcareai.PlantCareApplication
import com.plantcareai.data.model.Conversation
import com.plantcareai.data.repository.ChatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the conversation history drawer.
 * Provides a reactive list of all conversations and deletion operations.
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository =
        (application as PlantCareApplication).chatRepository

    val conversations: StateFlow<List<Conversation>> = repository.allConversations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun deleteConversation(conversation: Conversation) {
        viewModelScope.launch {
            repository.deleteConversation(conversation)
        }
    }
}
