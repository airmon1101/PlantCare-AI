package com.plantcareai

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.plantcareai.ui.chat.ChatScreen
import com.plantcareai.ui.settings.SettingsScreen

/**
 * Root navigation composable.
 * Defines the navigation graph with Chat and Settings destinations.
 */
@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(ChatDestination)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<ChatDestination> {
                ChatScreen(
                    onNavigateToSettings = { backStack.add(SettingsDestination) },
                    modifier = Modifier.safeDrawingPadding()
                )
            }

            entry<SettingsDestination> {
                SettingsScreen(
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding()
                )
            }
        }
    )
}
