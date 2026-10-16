package com.plantcareai.ui.chat

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.plantcareai.PlantCareApplication
import com.plantcareai.ai.GemmaModelManager
import com.plantcareai.ai.ModelState
import com.plantcareai.ai.PlantCarePrompts
import com.plantcareai.data.model.ChatMessage
import com.plantcareai.data.model.Conversation
import com.plantcareai.data.model.MessageRole
import com.plantcareai.data.model.MessageStatus
import com.plantcareai.data.repository.ChatRepository
import com.plantcareai.util.ImageUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * UI state for the chat screen.
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentConversationId: String? = null,
    val inputText: String = "",
    val attachedImage: Bitmap? = null,
    val attachedImageUri: Uri? = null,
    val isGenerating: Boolean = false,
    val modelState: ModelState = ModelState.Idle,
    val showImagePicker: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for the chat screen.
 *
 * Manages the conversation lifecycle, user input, image attachments,
 * and orchestrates AI inference through [GemmaModelManager].
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PlantCareApplication
    private val repository: ChatRepository = app.chatRepository
    private val modelManager: GemmaModelManager = app.gemmaModelManager

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var messageCollectionJob: Job? = null
    private var inferenceJob: Job? = null

    init {
        // Start loading the model immediately
        loadModel()
    }

    /**
     * Loads the Gemma 4 E2B model in the background.
     */
    private fun loadModel() {
        viewModelScope.launch {
            _uiState.update { it.copy(modelState = ModelState.Loading) }
            modelManager.loadModel()
            _uiState.update { it.copy(modelState = modelManager.modelState) }
        }
    }

    /**
     * Starts a new conversation, clearing the current chat.
     */
    fun startNewConversation() {
        viewModelScope.launch {
            // Cancel any ongoing inference
            inferenceJob?.cancel()

            val conversation = Conversation()
            repository.createConversation(conversation)

            // Create a new model conversation session
            modelManager.createNewConversation()

            // Add welcome message
            val welcomeMessage = ChatMessage(
                conversationId = conversation.id,
                role = MessageRole.ASSISTANT,
                text = PlantCarePrompts.WELCOME_MESSAGE
            )
            repository.addMessage(welcomeMessage)

            _uiState.update {
                it.copy(
                    currentConversationId = conversation.id,
                    inputText = "",
                    attachedImage = null,
                    attachedImageUri = null,
                    isGenerating = false,
                    errorMessage = null
                )
            }

            // Start collecting messages for this conversation
            observeMessages(conversation.id)
        }
    }

    /**
     * Loads an existing conversation from history.
     */
    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            inferenceJob?.cancel()

            // Recreate model conversation and replay context
            modelManager.createNewConversation()

            _uiState.update {
                it.copy(
                    currentConversationId = conversationId,
                    inputText = "",
                    attachedImage = null,
                    attachedImageUri = null,
                    isGenerating = false,
                    errorMessage = null
                )
            }

            observeMessages(conversationId)
        }
    }

    /**
     * Observes messages for the given conversation and updates the UI state.
     */
    private fun observeMessages(conversationId: String) {
        messageCollectionJob?.cancel()
        messageCollectionJob = viewModelScope.launch {
            repository.getMessages(conversationId).collect { messages ->
                // Load image bitmaps for messages that have image paths
                val messagesWithImages = messages.map { message ->
                    if (message.imagePath != null && message.imageBitmap == null) {
                        val bitmap = ImageUtils.loadBitmapFromPath(message.imagePath)
                        message.copy().also { it.imageBitmap = bitmap }
                    } else {
                        message
                    }
                }
                _uiState.update { it.copy(messages = messagesWithImages) }
            }
        }
    }

    /**
     * Updates the text input field.
     */
    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    /**
     * Attaches an image from a gallery URI.
     */
    fun attachImage(uri: Uri) {
        viewModelScope.launch {
            val bitmap = ImageUtils.loadBitmapFromUri(getApplication(), uri)
            _uiState.update {
                it.copy(
                    attachedImage = bitmap,
                    attachedImageUri = uri,
                    showImagePicker = false
                )
            }
        }
    }

    /**
     * Attaches an image from a camera capture.
     */
    fun attachCameraImage(bitmap: Bitmap) {
        _uiState.update {
            it.copy(
                attachedImage = bitmap,
                attachedImageUri = null,
                showImagePicker = false
            )
        }
    }

    /**
     * Removes the currently attached image.
     */
    fun removeAttachedImage() {
        _uiState.update {
            it.copy(attachedImage = null, attachedImageUri = null)
        }
    }

    /**
     * Toggles the image picker bottom sheet.
     */
    fun toggleImagePicker(show: Boolean) {
        _uiState.update { it.copy(showImagePicker = show) }
    }

    /**
     * Sends the current message (text + optional image) and triggers AI inference.
     */
    fun sendMessage() {
        val state = _uiState.value
        val text = state.inputText.trim()
        val image = state.attachedImage

        // Need at least text or image
        if (text.isBlank() && image == null) return

        val conversationId = state.currentConversationId ?: run {
            // Auto-start a new conversation if none exists
            viewModelScope.launch {
                startNewConversation()
                // Retry after conversation is created
                kotlinx.coroutines.delay(100)
                sendMessage()
            }
            return
        }

        viewModelScope.launch {
            // Save the image to internal storage if attached
            var imagePath: String? = null
            if (image != null) {
                imagePath = ImageUtils.saveBitmapToInternal(
                    getApplication(), image, conversationId
                )
            }

            // Create and save the user message
            val userMessage = ChatMessage(
                conversationId = conversationId,
                role = MessageRole.USER,
                text = text,
                imagePath = imagePath
            ).also { it.imageBitmap = image }

            repository.addMessage(userMessage)

            // Clear input
            _uiState.update {
                it.copy(
                    inputText = "",
                    attachedImage = null,
                    attachedImageUri = null,
                    isGenerating = true
                )
            }

            // Generate AI response
            generateAiResponse(conversationId, text, image)
        }
    }

    /**
     * Generates an AI response using the Gemma model.
     */
    private fun generateAiResponse(
        conversationId: String,
        userText: String,
        userImage: Bitmap?
    ) {
        inferenceJob?.cancel()
        inferenceJob = viewModelScope.launch {
            // Create a placeholder message for streaming
            val responseId = UUID.randomUUID().toString()
            val aiMessage = ChatMessage(
                id = responseId,
                conversationId = conversationId,
                role = MessageRole.ASSISTANT,
                text = "",
                status = MessageStatus.STREAMING
            )
            repository.addMessage(aiMessage)

            val responseBuilder = StringBuilder()

            try {
                modelManager.generateResponse(
                    text = userText,
                    image = userImage
                ).collect { token ->
                    responseBuilder.append(token)
                    // Update the streaming message with accumulated text
                    repository.updateMessage(
                        aiMessage.copy(
                            text = responseBuilder.toString(),
                            status = MessageStatus.STREAMING
                        )
                    )
                }

                // Mark as complete
                repository.updateMessage(
                    aiMessage.copy(
                        text = responseBuilder.toString(),
                        status = MessageStatus.COMPLETE
                    )
                )
            } catch (e: Exception) {
                val errorText = if (responseBuilder.isNotEmpty()) {
                    responseBuilder.toString() + "\n\n⚠️ Response was interrupted."
                } else {
                    "⚠️ I'm sorry, I encountered an error processing your request. Please try again."
                }
                repository.updateMessage(
                    aiMessage.copy(
                        text = errorText,
                        status = MessageStatus.ERROR
                    )
                )
            } finally {
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    /**
     * Dismisses the current error message.
     */
    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Retries loading the AI model.
     */
    fun retryModelLoad() {
        loadModel()
    }

    override fun onCleared() {
        super.onCleared()
        inferenceJob?.cancel()
        messageCollectionJob?.cancel()
    }
}
