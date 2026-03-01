package com.example.weatherforecast.presentation.viewmodel.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.preferences.PreferencesManager
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.HourlyWeatherInteractor
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.utils.ResourceManager

/**
 * WeatherForecastViewModel factory
 *
 * @property loggingService centralized service for structured logging
 * @property statusRenderer displays status messages to the user
 * @property resourceManager resource manager
 * @property preferencesManager preferences manager
 * @property connectivityObserver internet connectivity observer
 * @property coroutineDispatchers coroutines dispatchers
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
    private val coroutineDispatchers: CoroutineDispatchers,
    private val forecastRemoteInteractor: HourlyWeatherInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HourlyWeatherViewModel(
            connectivityObserver,
            coroutineDispatchers,
            statusRenderer,
            loggingService,
            resourceManager,
            preferencesManager,
            chosenCityInteractor,
            forecastRemoteInteractor
        ) as T
    }
}