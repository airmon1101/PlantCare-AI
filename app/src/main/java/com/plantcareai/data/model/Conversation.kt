package com.plantcareai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a chat conversation (session).
 *
 * @property id Unique identifier for this conversation.
 * @property title Display title, derived from the first user message.
 * @property createdAt When this conversation was started (epoch millis).
 * @property updatedAt When this conversation was last active (epoch millis).
 * @property previewText Short preview of the last message for the history list.
 */
@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Chat",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val previewText: String = ""
)
