package com.example.weatherforecast.presentation.viewmodel.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.presentation.converter.ForecastDomainToUiConverter
import com.example.weatherforecast.utils.ResourceManager

/**
 * WeatherForecastViewModel factory
 *
 * @property temperatureType type of temperature
 * @property resourceManager resource manager
 * @property connectivityObserver internet connectivity observer
 * @property coroutineDispatchers coroutines dispatchers
 * @property chosenCityInteractor downloads a previously chosen city
 * @property forecastLocalInteractor downloads weather forecast from database
 * @property forecastRemoteInteractor downloads weather forecast through network
 */
class WeatherForecastViewModelFactory(
    private val temperatureType: TemperatureType,
    private val resourceManager: ResourceManager,
    private val connectivityObserver: ConnectivityObserver,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val forecastLocalInteractor: WeatherForecastLocalInteractor,
    private val forecastRemoteInteractor: WeatherForecastRemoteInteractor,
    private val uiConverter: ForecastDomainToUiConverter,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherForecastViewModel::class.java)) {
            return WeatherForecastViewModel(
                connectivityObserver,
                temperatureType,
                resourceManager,
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