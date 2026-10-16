package com.plantcareai.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plantcareai.R
import com.plantcareai.ai.ModelState

/**
 * Settings screen showing model status, inference parameters, and app info.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Model Status Card
            SettingsCard(
                title = stringResource(R.string.model_status),
                icon = { Icon(Icons.Outlined.Memory, contentDescription = null) }
            ) {
                val statusText = when (val state = uiState.modelState) {
                    is ModelState.Ready -> if (state.isLocalModel) "🟢 Gemma 4 E2B Loaded (Local On-Device)" else "🌱 Built-in PlantCare Assistant (Active)"
                    is ModelState.DemoMode -> "🌱 Built-in PlantCare Assistant Active"
                    is ModelState.Loading -> "⏳ Loading model..."
                    is ModelState.Error -> "❌ ${state.message}"
                    is ModelState.Idle -> "⏸️ Model not loaded"
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (uiState.modelState is ModelState.DemoMode || (uiState.modelState as? ModelState.Ready)?.isLocalModel == false) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The app is using the built-in intelligent plant health assistant. To run local Gemma 4 E2B neural weights on-device:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. Download gemma-4-e2b-it.litertlm (LiteRT Community)\n" +
                               "2. Place at: ${uiState.modelPath}\n" +
                               "3. Or run: adb push gemma-4-e2b-it.litertlm /data/data/com.plantcareai/files/models/",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.reloadModel() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check / Reload Model Weights")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inference Settings Card
            SettingsCard(
                title = "Inference Settings",
                icon = { Icon(Icons.Outlined.Tune, contentDescription = null) }
            ) {
                // Temperature
                Text(
                    text = "${stringResource(R.string.temperature)}: ${"%.1f".format(uiState.temperature)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = uiState.temperature,
                    onValueChange = viewModel::updateTemperature,
                    valueRange = 0f..1.5f,
                    steps = 14
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Max output tokens
                Text(
                    text = "${stringResource(R.string.max_tokens)}: ${uiState.maxOutputTokens}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = uiState.maxOutputTokens.toFloat(),
                    onValueChange = { viewModel.updateMaxTokens(it.toInt()) },
                    valueRange = 128f..2048f,
                    steps = 14
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About Card
            SettingsCard(
                title = stringResource(R.string.about),
                icon = { Icon(Icons.Outlined.Info, contentDescription = null) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Eco,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PlantCare AI",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stringResource(R.string.version)}: 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Powered by Google Gemma 4 E2B (on-device)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.disclaimer),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
