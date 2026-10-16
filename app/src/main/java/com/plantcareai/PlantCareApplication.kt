package com.plantcareai

import android.app.Application
import com.plantcareai.ai.GemmaModelManager
import com.plantcareai.data.local.AppDatabase
import com.plantcareai.data.repository.ChatRepository

/**
 * Application class that initializes app-wide singletons.
 *
 * Provides lazy access to:
 * - [database]: Room database for conversation persistence
 * - [chatRepository]: Repository for chat data operations
 * - [gemmaModelManager]: Manager for the on-device Gemma 4 E2B model
 */
class PlantCareApplication : Application() {

    /** Room database instance, created lazily on first access. */
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    /** Repository for chat conversations and messages. */
    val chatRepository: ChatRepository by lazy {
        ChatRepository(database.chatDao())
    }

    /** Manager for the on-device Gemma AI model. */
    val gemmaModelManager: GemmaModelManager by lazy {
        GemmaModelManager(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: PlantCareApplication
            private set
    }
}
