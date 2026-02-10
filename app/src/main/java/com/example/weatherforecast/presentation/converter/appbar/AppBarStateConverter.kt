package com.example.weatherforecast.presentation.converter.appbar

import com.example.weatherforecast.R
import com.example.weatherforecast.models.presentation.AppBarState
import com.example.weatherforecast.presentation.viewmodel.forecast.DataSource
import com.example.weatherforecast.presentation.viewmodel.forecast.ForecastUiState
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
    fun convert(forecastState: ForecastUiState): AppBarState {
        return when (forecastState) {
            is ForecastUiState.Loading -> {
                AppBarState(
                    title = resourceManager.getString(R.string.app_name),
                    subtitle = resourceManager.getString(R.string.forecast_for_city_loading),
                    subtitleColorAttr = resourceManager.getThemeColorRes(R.attr.colorInfo),
                )
            }

            is ForecastUiState.Error -> {
                AppBarState(
                    title = resourceManager.getString(R.string.app_name),
                    subtitle = resourceManager.getString(R.string.forecast_for_city_error),
                    subtitleColorAttr = resourceManager.getThemeColorRes(R.attr.colorError),
                )
            }

            is ForecastUiState.Success -> {
                AppBarState(
                    title = resourceManager.getString(R.string.app_name),
                    subtitle = resourceManager.getString(
                        R.string.forecast_for_city_success,
                        forecastState.forecast.city
                    ),
                    subtitleColorAttr = getToolbarSubtitleColor(forecastState),
                )
            }
        }
    }

    private fun getToolbarSubtitleColor(forecast: ForecastUiState.Success) =
        if (forecast.source == DataSource.REMOTE) {
            resourceManager.getThemeColorRes(R.attr.colorInfo)
        } else {
            resourceManager.getThemeColorRes(R.attr.colorWarning)
        }
}