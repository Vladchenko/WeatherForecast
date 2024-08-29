package com.example.weatherforecast.presentation.viewmodel.persistence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor

/**
 * View model factory
 *
 * @property coroutineDispatchers coroutines dispatchers
 * @property chosenCityInteractor downloads a previously chosen city
 * @property weatherForecastLocalInteractor  provides data for weather forecast through database
 */
class PersistenceViewModelFactory(
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val weatherForecastLocalInteractor: WeatherForecastLocalInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PersistenceViewModel(
            coroutineDispatchers,
            chosenCityInteractor,
            weatherForecastLocalInteractor
        ) as T
    }
}