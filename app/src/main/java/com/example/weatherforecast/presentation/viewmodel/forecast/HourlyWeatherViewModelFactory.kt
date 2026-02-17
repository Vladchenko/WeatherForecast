package com.example.weatherforecast.presentation.viewmodel.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.preferences.PreferencesManager
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.HourlyWeatherLocalInteractor
import com.example.weatherforecast.domain.forecast.HourlyWeatherRemoteInteractor

/**
 * WeatherForecastViewModel factory
 *
 * @property connectivityObserver internet connectivity observer
 * @property coroutineDispatchers coroutines dispatchers
 * @property chosenCityInteractor downloads a previously chosen city
 * @property forecastLocalInteractor downloads weather forecast from database
 * @property forecastRemoteInteractor downloads weather forecast through network
 */
@Suppress("UNCHECKED_CAST")
class HourlyWeatherViewModelFactory(
    private val preferencesManager: PreferencesManager,
    private val connectivityObserver: ConnectivityObserver,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val forecastLocalInteractor: HourlyWeatherLocalInteractor,
    private val forecastRemoteInteractor: HourlyWeatherRemoteInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HourlyWeatherViewModel(
            connectivityObserver,
            coroutineDispatchers,
            preferencesManager,
            chosenCityInteractor,
            forecastLocalInteractor,
            forecastRemoteInteractor
        ) as T
    }
}