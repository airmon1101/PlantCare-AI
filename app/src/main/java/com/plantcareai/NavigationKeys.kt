package com.plantcareai

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation route keys for the app's screen destinations.
 */
@Serializable
data object ChatDestination : NavKey

@Serializable
data object SettingsDestination : NavKey
