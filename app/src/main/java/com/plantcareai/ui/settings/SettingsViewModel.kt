package com.plantcareai.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.plantcareai.PlantCareApplication
import com.plantcareai.ai.GemmaModelManager
import com.plantcareai.ai.ModelState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val modelState: ModelState = ModelState.Idle,
    val modelPath: String = "",
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 1024
)

/**
 * ViewModel for the settings screen.
 * Manages inference parameters and displays model status.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val modelManager: GemmaModelManager =
        (application as PlantCareApplication).gemmaModelManager

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            modelState = modelManager.modelState,
            modelPath = modelManager.getExpectedModelPath(),
            temperature = modelManager.temperature,
            maxOutputTokens = modelManager.maxOutputTokens
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateTemperature(temp: Float) {
        modelManager.temperature = temp
        _uiState.update { it.copy(temperature = temp) }
    }

    fun updateMaxTokens(tokens: Int) {
        modelManager.maxOutputTokens = tokens
        _uiState.update { it.copy(maxOutputTokens = tokens) }
    }

    fun reloadModel() {
        viewModelScope.launch {
            _uiState.update { it.copy(modelState = ModelState.Loading) }
            modelManager.loadModel()
            _uiState.update {
                it.copy(
                    modelState = modelManager.modelState,
                    modelPath = modelManager.getExpectedModelPath()
                )
            }
        }
    }
}
