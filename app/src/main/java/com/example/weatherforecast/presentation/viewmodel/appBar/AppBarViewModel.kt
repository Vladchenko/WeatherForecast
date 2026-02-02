package com.example.weatherforecast.presentation.viewmodel.appBar

import androidx.lifecycle.ViewModel
import com.example.weatherforecast.models.presentation.AppBarState
import com.example.weatherforecast.models.presentation.MessageType
import com.example.weatherforecast.presentation.PresentationUtils
import com.example.weatherforecast.presentation.converter.appbar.AppBarStateConverter
import com.example.weatherforecast.presentation.viewmodel.forecast.ForecastUiState
import com.example.weatherforecast.utils.ResourceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for AppBar
 *
 * @property resourceManager to get string resources
 * @property appBarStateConverter to convert forecast ui state to appbar ui state
 */
@HiltViewModel
class AppBarViewModel @Inject constructor(
    private val resourceManager: ResourceManager,
    private val appBarStateConverter: AppBarStateConverter
) : ViewModel() {
    private val _appBarState = MutableStateFlow(AppBarState())
    val appBarState: StateFlow<AppBarState> = _appBarState.asStateFlow()

    /**
     * Update appbar state using [forecastUiState]
     */
    fun updateAppBarState(forecastUiState: ForecastUiState) {
        val appBarState = appBarStateConverter.convert(
            forecastState = forecastUiState,
            resourceManager = resourceManager,
        )
        _appBarState.update { appBarState }
    }

    fun updateTitle(title: String) {
        _appBarState.update { it.copy(title = title) }
    }

    fun updateSubtitle(subtitle: String) {
        _appBarState.update { it.copy(subtitle = subtitle) }
    }

    fun updateSubtitleWithError(subtitle: String) {
        _appBarState.update {
            it.copy(
                subtitle = subtitle,
                subtitleColor = PresentationUtils.getToolbarSubtitleColor(MessageType.ERROR)
            )
        }
    }

    fun updateSubtitleWithWarning(subtitle: String) {
        _appBarState.update {
            it.copy(
                subtitle = subtitle,
                subtitleColor = PresentationUtils.getToolbarSubtitleColor(MessageType.WARNING)
            )
        }
    }

    fun setActionsVisible(visible: Boolean) {
        _appBarState.update { it.copy(actionsVisible = visible) }
    }
}