package com.example.weatherforecast.presentation.viewmodel.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.preferences.PreferencesManager
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.CurrentWeatherInteractor
import com.example.weatherforecast.presentation.converter.WeatherDomainToUiConverter
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.utils.ResourceManager
import kotlinx.serialization.InternalSerializationApi

/**
 * WeatherForecastViewModel factory
 *
 * @property loggingService centralized service for application logging
 * @property statusRenderer displays loading, success, warning, or error statuses
 * @property resourceManager resource manager
 * @property preferencesManager to provide preferences for application
 * @property connectivityObserver internet connectivity observer
 * @property coroutineDispatchers coroutines dispatchers
 * @property chosenCityInteractor downloads a previously chosen city
 * @property weatherInteractor downloads weather forecast through network
 */
class CurrentWeatherViewModelFactory(
    private val loggingService: LoggingService,
    private val statusRenderer: StatusRenderer,
    private val resourceManager: ResourceManager,
    private val preferencesManager: PreferencesManager,
    private val connectivityObserver: ConnectivityObserver,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val weatherInteractor: CurrentWeatherInteractor,
    private val uiConverter: WeatherDomainToUiConverter,
) : ViewModelProvider.Factory {

    @InternalSerializationApi
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CurrentWeatherViewModel::class.java)) {
            return CurrentWeatherViewModel(
                connectivityObserver,
                statusRenderer,
                loggingService,
                resourceManager,
                preferencesManager,
                coroutineDispatchers,
                chosenCityInteractor,
                weatherInteractor,
                uiConverter
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}