package com.example.weatherforecast.presentation.converter.appbar

import com.example.weatherforecast.R
import com.example.weatherforecast.models.presentation.AppBarState
import com.example.weatherforecast.models.presentation.MessageType
import com.example.weatherforecast.presentation.PresentationUtils
import com.example.weatherforecast.presentation.viewmodel.forecast.DataSource
import com.example.weatherforecast.presentation.viewmodel.forecast.ForecastUiState
import com.example.weatherforecast.utils.ResourceManager
import javax.inject.Inject

/**
 * Converter forecast ui state to an appbar ui state.
 */
class AppBarStateConverter @Inject constructor() {

    /**
     * Convert [forecastState] to [AppBarState], having [resourceManager] to get resource strings.
     */
    fun convert(
        forecastState: ForecastUiState,
        resourceManager: ResourceManager
    ): AppBarState {
        return when (forecastState) {
            is ForecastUiState.Loading -> {
                AppBarState(
                    title = resourceManager.getString(R.string.app_name),
                    subtitle = resourceManager.getString(R.string.forecast_for_city_loading),
                    subtitleColor = PresentationUtils.getToolbarSubtitleColor(MessageType.INFO),
                )
            }

            is ForecastUiState.Error -> {
                AppBarState(
                    title = resourceManager.getString(R.string.app_name),
                    subtitle = resourceManager.getString(R.string.forecast_for_city_error),
                    subtitleColor = PresentationUtils.getToolbarSubtitleColor(MessageType.ERROR),
                )
            }

            is ForecastUiState.Success -> {
                AppBarState(
                    title = resourceManager.getString(R.string.app_name),
                    subtitle = resourceManager.getString(
                        R.string.forecast_for_city_success,
                        forecastState.forecast.city
                    ),
                    subtitleColor = getToolbarSubtitleColor(forecastState),
                )
            }
        }
    }

    private fun getToolbarSubtitleColor(forecast: ForecastUiState.Success) =
        if (forecast.source == DataSource.REMOTE) {
            PresentationUtils.getToolbarSubtitleColor(MessageType.INFO)
        } else {
            PresentationUtils.getToolbarSubtitleColor(MessageType.WARNING)
        }
}