package io.github.vladchenko.weatherforecast.feature.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel

/**
 * Root composable for the application settings screen.
 *
 * Displays the settings UI with current preferences and delegates
 * all interactions to [SettingsLayout].
 *
 * @param onNavigateUp Callback to navigate up in the navigation hierarchy
 * @param preferencesManager Manages user preferences (e.g., temperature unit)
 * @param appBarViewModel The toolbar state provider. Default: Hilt-provided instance.
 */
@ExperimentalMaterial3Api
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    preferencesManager: PreferencesManager,
    appBarViewModel: AppBarViewModel = hiltViewModel(),
) {
    val appBarUiState by appBarViewModel.appBarUiStateFlow.collectAsStateWithLifecycle()
    SettingsLayout(
        onNavigateUp = onNavigateUp,
        appBarUiState = appBarUiState,
        preferencesManager = preferencesManager,
    )
}