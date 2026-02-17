package com.example.weatherforecast.presentation.viewmodel.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.preferences.PreferencesManager
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.CurrentWeatherLocalInteractor
import com.example.weatherforecast.domain.forecast.CurrentWeatherRemoteInteractor
import com.example.weatherforecast.presentation.converter.WeatherDomainToUiConverter
import com.example.weatherforecast.utils.ResourceManager
import kotlinx.serialization.InternalSerializationApi

/**
 * WeatherForecastViewModel factory
 *
 * @property resourceManager resource manager
 * @property preferencesManager to provide preferences for application
 * @property connectivityObserver internet connectivity observer
 * @property coroutineDispatchers coroutines dispatchers
 * @property chosenCityInteractor downloads a previously chosen city
 * @property forecastLocalInteractor downloads weather forecast from database
 * @property forecastRemoteInteractor downloads weather forecast through network
 */
class CurrentWeatherViewModelFactory(
    private val resourceManager: ResourceManager,
    private val preferencesManager: PreferencesManager,
    private val connectivityObserver: ConnectivityObserver,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val forecastLocalInteractor: CurrentWeatherLocalInteractor,
    private val forecastRemoteInteractor: CurrentWeatherRemoteInteractor,
    private val uiConverter: WeatherDomainToUiConverter,
) : ViewModelProvider.Factory {

    @InternalSerializationApi
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CurrentWeatherViewModel::class.java)) {
            return CurrentWeatherViewModel(
                connectivityObserver,
                resourceManager,
                preferencesManager,
                coroutineDispatchers,
                chosenCityInteractor,
                forecastLocalInteractor,
                forecastRemoteInteractor,
                uiConverter
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}