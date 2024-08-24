package com.example.weatherforecast.presentation.viewmodel.persistence

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor

/**
 * View model factory
 *
 * @property app custom [Application] implementation for Hilt
 * @property coroutineDispatchers coroutines dispatchers
 * @property chosenCityInteractor downloads a previously chosen city
 * @property weatherForecastLocalInteractor  provides data for weather forecast through database
 */
class PersistenceViewModelFactory(
    private val app: Application,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val weatherForecastLocalInteractor: WeatherForecastLocalInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PersistenceViewModel(
            app,
            coroutineDispatchers,
            chosenCityInteractor,
            weatherForecastLocalInteractor
        ) as T
    }
}