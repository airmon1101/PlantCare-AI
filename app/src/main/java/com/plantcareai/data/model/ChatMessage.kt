package com.plantcareai.data.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents the role/sender of a chat message.
 */
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

/**
 * Represents the current state of an AI response being generated.
 */
enum class MessageStatus {
    COMPLETE,
    STREAMING,
    ERROR
}

/**
 * A single message in a chat conversation.
 *
 * @property id Unique identifier for this message.
 * @property conversationId The conversation this message belongs to.
 * @property role Who sent this message (USER, ASSISTANT, or SYSTEM).
 * @property text The text content of the message.
 * @property imagePath Local file path to an attached image, if any.
 * @property timestamp When this message was created (epoch millis).
 * @property status Current status of the message (for streaming AI responses).
 */
@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val role: MessageRole,
    val text: String = "",
    val imagePath: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.COMPLETE
) {
    /**
     * Transient bitmap for the attached image. Not stored in Room.
     * Loaded on-demand from [imagePath] for display and inference.
     */
    @Ignore
    var imageBitmap: Bitmap? = null
}
