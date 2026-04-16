package io.github.vladchenko.weatherforecast.presentation.viewmodel.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain.HourlyWeatherInteractor
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer

/**
 * WeatherForecastViewModel factory
 *
 * @property loggingService centralized service for structured logging
 * @property statusRenderer displays status messages to the user
 * @property resourceManager resource manager
 * @property preferencesManager preferences manager
 * @property connectivityObserver internet connectivity observer
 * @property chosenCityInteractor downloads a previously chosen city
 * @property forecastRemoteInteractor downloads weather forecast through network
 */
@Suppress("UNCHECKED_CAST")
class HourlyWeatherViewModelFactory(
    private val loggingService: LoggingService,
    private val statusRenderer: StatusRenderer,
    private val resourceManager: ResourceManager,
    private val preferencesManager: PreferencesManager,
    private val connectivityObserver: ConnectivityObserver,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val forecastRemoteInteractor: HourlyWeatherInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HourlyWeatherViewModel(
            connectivityObserver,
            statusRenderer,
            loggingService,
            resourceManager,
            preferencesManager,
            chosenCityInteractor,
            forecastRemoteInteractor
        ) as T
    }
}