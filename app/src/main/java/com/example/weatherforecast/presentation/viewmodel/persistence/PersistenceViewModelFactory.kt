package com.example.weatherforecast.presentation.viewmodel.persistence

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor

/**
 * View model factory
 *
 * @property app custom [Application] implementation for Hilt
 * @property weatherForecastLocalInteractor  provides domain layer data through database
 */
class PersistenceViewModelFactory(
    private val app: Application,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val weatherForecastLocalInteractor: WeatherForecastLocalInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PersistenceViewModel(
            app,
            chosenCityInteractor,
            weatherForecastLocalInteractor
        ) as T
    }
}