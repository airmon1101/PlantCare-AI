package com.plantcareai.data.repository

import com.plantcareai.data.local.ChatDao
import com.plantcareai.data.model.ChatMessage
import com.plantcareai.data.model.Conversation
import kotlinx.coroutines.flow.Flow

/**
 * Repository layer that mediates between the data sources (Room) and the rest of the app.
 * Provides a clean API for conversation and message operations.
 */
class ChatRepository(private val chatDao: ChatDao) {

    // ── Conversations ──────────────────────────────────────────────

    val allConversations: Flow<List<Conversation>> = chatDao.getAllConversations()

    suspend fun createConversation(conversation: Conversation) {
        chatDao.insertConversation(conversation)
    }

    suspend fun updateConversation(conversation: Conversation) {
        chatDao.updateConversation(conversation)
    }

    suspend fun deleteConversation(conversation: Conversation) {
        chatDao.deleteMessagesForConversation(conversation.id)
        chatDao.deleteConversation(conversation)
    }

    suspend fun getConversation(id: String): Conversation? {
        return chatDao.getConversation(id)
    }

    // ── Messages ───────────────────────────────────────────────────

    fun getMessages(conversationId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForConversation(conversationId)
    }

    suspend fun getMessagesOnce(conversationId: String): List<ChatMessage> {
        return chatDao.getMessagesForConversationOnce(conversationId)
    }

    suspend fun addMessage(message: ChatMessage) {
        chatDao.insertMessage(message)
        // Update the conversation's timestamp and preview
        chatDao.getConversation(message.conversationId)?.let { conversation ->
            val updatedConversation = conversation.copy(
                updatedAt = System.currentTimeMillis(),
                previewText = message.text.take(100),
                // Set title from first user message if still default
                title = if (conversation.title == "New Chat" && message.text.isNotBlank()) {
                    message.text.take(50).let {
                        if (it.length == 50) "$it…" else it
                    }
                } else {
                    conversation.title
                }
            )
            chatDao.updateConversation(updatedConversation)
        }
    }

    suspend fun updateMessage(message: ChatMessage) {
        chatDao.updateMessage(message)
    }
}
