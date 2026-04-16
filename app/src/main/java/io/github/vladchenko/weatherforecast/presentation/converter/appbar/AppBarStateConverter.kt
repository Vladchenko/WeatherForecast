package io.github.vladchenko.weatherforecast.presentation.converter.appbar

import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.constants.SubtitleSize
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain.model.HourlyWeather
import io.github.vladchenko.weatherforecast.models.presentation.AppBarState
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.models.CurrentWeatherUi
import io.github.vladchenko.weatherforecast.core.ui.state.DataSource
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
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
    fun convert(forecastState: WeatherUiState<*>): AppBarState {
        val subtitle = when (forecastState) {
            is WeatherUiState.Loading -> resourceManager.getString(R.string.forecast_loading)
            is WeatherUiState.Error -> resourceManager.getString(
                R.string.forecast_load_error,
                forecastState.city.orEmpty()
            )
            is WeatherUiState.Success -> {
                val city = when (val data = forecastState.data) {
                    is CurrentWeatherUi -> data.city
                    is HourlyWeather -> data.city
                    else -> ""
                }
                resourceManager.getString(
                    R.string.forecast_loaded_success,
                    city
                )
            }
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

    fun getToolbarSubtitleColor(forecast: WeatherUiState.Success<*>) =
        if (forecast.source == DataSource.REMOTE) {
            R.attr.colorInfo
        } else {
            R.attr.colorWarning
        }
}