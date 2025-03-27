package com.example.weatherforecast.presentation.viewmodel.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor

/**
 * WeatherForecastViewModel factory
 *
 * @property temperatureType type of temperature
 * @property connectivityObserver internet connectivity observer
 * @property coroutineDispatchers coroutines dispatchers
 * @property chosenCityInteractor downloads a previously chosen city
 * @property forecastLocalInteractor downloads weather forecast from database
 * @property forecastRemoteInteractor downloads weather forecast through network
 */
class HourlyForecastViewModelFactory(
    private val temperatureType: TemperatureType,
    private val connectivityObserver: ConnectivityObserver,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val forecastLocalInteractor: WeatherForecastLocalInteractor,
    private val forecastRemoteInteractor: WeatherForecastRemoteInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HourlyForecastViewModel(
            connectivityObserver,
            temperatureType,
            coroutineDispatchers,
            chosenCityInteractor,
            forecastRemoteInteractor
        ) as T
    }
}