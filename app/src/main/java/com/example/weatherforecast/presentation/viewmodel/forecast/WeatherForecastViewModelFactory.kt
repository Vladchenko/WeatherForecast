package com.example.weatherforecast.presentation.viewmodel.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor

/**
 * View model factory
 *
 * @property temperatureType type of temperature
 * @property coroutineDispatchers coroutines dispatchers
 * @property chosenCityInteractor downloads a previously chosen city
 * @property weatherForecastRemoteInteractor provides domain layer data for weather forecast through network
 */
class WeatherForecastViewModelFactory(
    private val temperatureType: TemperatureType,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val weatherForecastRemoteInteractor: WeatherForecastRemoteInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherForecastViewModel(
            temperatureType,
            coroutineDispatchers,
            chosenCityInteractor,
            weatherForecastRemoteInteractor
        ) as T
    }
}