package io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar

import androidx.annotation.AttrRes
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.core.ui.status.MessageType
import io.github.vladchenko.weatherforecast.core.ui.status.StatusDisplay
import io.github.vladchenko.weatherforecast.models.presentation.AppBarUiState
import io.github.vladchenko.weatherforecast.presentation.converter.appbar.AppBarStateMapper
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel responsible for managing the state of the AppBar (toolbar) in the UI.
 *
 * Observes and reacts to changes in the forecast state and user messages,
 * updating the title, subtitle, and subtitle color accordingly.
 *
 * This ViewModel implements [StatusDisplay] to handle display of info, warning,
 * and error messages in the app bar's subtitle area.
 *
 * @property statusRenderer Displays loading, success, warning, or error statuses
 * @property resourceManager Provides access to string resources
 * @property appBarStateMapper Converts [WeatherUiState] into [AppBarUiState] for UI rendering
 */
@HiltViewModel
class AppBarViewModel @Inject constructor(
    private val statusRenderer: StatusRenderer,
    private val resourceManager: ResourceManager,
    private val appBarStateMapper: AppBarStateMapper
) : ViewModel(), StatusDisplay {

    /**
     * Read-only StateFlow emitting the current [AppBarUiState].
     *
     * Observed by the UI to update the toolbar's appearance (title, subtitle, colors).
     */
    val appBarUiStateFlow: StateFlow<AppBarUiState>
        get() = _appBarUiStateFlow.asStateFlow()

    private val _appBarUiStateFlow = MutableStateFlow(AppBarUiState())

    init {
        statusRenderer.setTarget(this)
    }

    override fun onCleared() {
        statusRenderer.clearTarget()
        super.onCleared()
    }

    override fun showStatus(status: StatusDisplay.Status) {
        when (status.type) {
            MessageType.INFO -> updateSubtitle(status.text, R.attr.colorInfo)
            MessageType.WARNING -> updateSubtitle(status.text, R.attr.colorWarning)
            MessageType.ERROR -> updateSubtitle(status.text, R.attr.colorError)
        }
    }

    /**
     * Updates the entire AppBar state based on the current forecast UI state.
     *
     * Uses [appBarStateMapper] to transform [WeatherUiState] into a corresponding
     * [AppBarUiState], including dynamic title, subtitle, and styling.
     *
     * @param weatherUiState The current state of the forecast screen
     */
    fun updateAppBarState(weatherUiState: WeatherUiState<*>) {
        val appBarState = appBarStateMapper.toAppbarState(forecastState = weatherUiState)
        _appBarUiStateFlow.update { appBarState }
    }

    /**
     * Updates only the title of the AppBar.
     *
     * Use this method when you need to change the title independently
     * of the full forecast state update (e.g., during navigation).
     *
     * @param title New title text to display
     */
    fun updateTitle(title: String) {
        _appBarUiStateFlow.update { it.copy(title = title) }
    }

    private fun updateSubtitle(text: String, @AttrRes colorAttr: Int) {
        _appBarUiStateFlow.update {
            it.copy(subtitle = text, subtitleColorAttr = colorAttr) // ← Сохраняем атрибут, не цвет
        }
    }
}