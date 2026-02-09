package com.example.weatherforecast.presentation.viewmodel.appBar

import androidx.lifecycle.ViewModel
import com.example.weatherforecast.models.presentation.AppBarState
import com.example.weatherforecast.models.presentation.MessageType
import com.example.weatherforecast.presentation.PresentationUtils
import com.example.weatherforecast.presentation.converter.appbar.AppBarStateConverter
import com.example.weatherforecast.presentation.status.StatusDisplay
import com.example.weatherforecast.presentation.viewmodel.forecast.ForecastUiState
import com.example.weatherforecast.utils.ResourceManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
 * @property resourceManager Provides access to string resources
 * @property appBarStateConverter Converts [ForecastUiState] into [AppBarState] for UI rendering
 */
@HiltViewModel
class AppBarViewModel @Inject constructor(
    private val resourceManager: ResourceManager,
    private val appBarStateConverter: AppBarStateConverter
) : ViewModel(), StatusDisplay {

    private val _appBarState = MutableStateFlow(AppBarState())

    /**
     * Read-only StateFlow emitting the current [AppBarState].
     *
     * Observed by the UI to update the toolbar's appearance (title, subtitle, colors).
     */
    val appBarState: StateFlow<AppBarState> = _appBarState.asStateFlow()

    override fun showStatus(status: StatusDisplay.Status) {
        when (status.type) {
            MessageType.INFO -> {
                _appBarState.update {
                    it.copy(
                        subtitle = status.text,
                        subtitleColor = PresentationUtils.getToolbarSubtitleColor(MessageType.INFO)
                    )
                }
            }

            MessageType.WARNING -> {
                _appBarState.update {
                    it.copy(
                        subtitle = status.text,
                        subtitleColor = PresentationUtils.getToolbarSubtitleColor(MessageType.WARNING)
                    )
                }
            }

            MessageType.ERROR -> {
                _appBarState.update {
                    it.copy(
                        subtitle = status.text,
                        subtitleColor = PresentationUtils.getToolbarSubtitleColor(MessageType.ERROR)
                    )
                }
            }
        }
    }

    /**
     * Updates the entire AppBar state based on the current forecast UI state.
     *
     * Uses [appBarStateConverter] to transform [ForecastUiState] into a corresponding
     * [AppBarState], including dynamic title, subtitle, and styling.
     *
     * @param forecastUiState The current state of the forecast screen
     */
    fun updateAppBarState(forecastUiState: ForecastUiState) {
        val appBarState = appBarStateConverter.convert(
            forecastState = forecastUiState,
            resourceManager = resourceManager,
        )
        _appBarState.update { appBarState }
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
        _appBarState.update { it.copy(title = title) }
    }
}