package com.example.weatherforecast.presentation.viewmodel.forecast

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor

/**
 * View model factory
 *
 * @param app custom [Application] implementation for Hilt
 * @param coroutineDispatchers coroutines dispatchers
 * @param chosenCityInteractor downloads a previously chosen city
 * @param weatherForecastRemoteInteractor provides domain layer data for weather forecast through network
 * @param weatherForecastLocalInteractor  provides domain layer data for weather forecast through database
 */
class WeatherForecastViewModelFactory(
    private val app: Application,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val weatherForecastLocalInteractor: WeatherForecastLocalInteractor,
    private val weatherForecastRemoteInteractor: WeatherForecastRemoteInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherForecastViewModel(
            app,
            coroutineDispatchers,
            chosenCityInteractor,
            weatherForecastLocalInteractor,
            weatherForecastRemoteInteractor
        ) as T
    }
}