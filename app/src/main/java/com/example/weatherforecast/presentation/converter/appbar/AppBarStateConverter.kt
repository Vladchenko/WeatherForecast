package com.example.weatherforecast.presentation.converter.appbar

import com.example.weatherforecast.R
import com.example.weatherforecast.models.presentation.AppBarState
import com.example.weatherforecast.presentation.SubtitleSize
import com.example.weatherforecast.presentation.viewmodel.forecast.DataSource
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherUiState
import com.example.weatherforecast.utils.ResourceManager
import javax.inject.Inject

/**
 * Converter forecast ui state to an appbar ui state.
 *
 * @property resourceManager Helper to retrieve localized strings from resources
 */
class AppBarStateConverter @Inject constructor(
    private val resourceManager: ResourceManager
) {

    /**
     * Convert [forecastState] to [AppBarState]
     */
    fun convert(forecastState: WeatherUiState): AppBarState {
        val subtitle = when (forecastState) {
            is WeatherUiState.Loading -> resourceManager.getString(R.string.forecast_loading)
            is WeatherUiState.Error -> resourceManager.getString(
                R.string.forecast_load_error,
                forecastState.city.orEmpty()
            )
            is WeatherUiState.Success -> resourceManager.getString(
                R.string.forecast_loaded_success,
                forecastState.forecast.city
            )
        }

        val subtitleColorAttr = when (forecastState) {
            is WeatherUiState.Loading -> R.attr.colorInfo
            is WeatherUiState.Error -> R.attr.colorError
            is WeatherUiState.Success -> {
                getToolbarSubtitleColor(forecastState)
            }
        }

        return AppBarState(
            title = resourceManager.getString(R.string.app_name),
            subtitle = subtitle,
            subtitleColorAttr = subtitleColorAttr,
            subtitleSize = SubtitleSize.fromSubtitle(subtitle)
        )
    }

    fun getToolbarSubtitleColor(forecast: WeatherUiState.Success) =
        if (forecast.source == DataSource.REMOTE) {
            R.attr.colorInfo
        } else {
            R.attr.colorWarning
        }
}