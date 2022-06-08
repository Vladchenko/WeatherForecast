package com.example.weatherforecast.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.domain.WeatherForecastInteractor
import javax.inject.Inject

/**
 * View model factory
 *
 * @property app custom [Application] implementation for Hilt
 * @property weatherForecastInteractor provides domain layer data
 */
class WeatherForecastViewModelFactory(
    private val app: Application,
    private val weatherForecastInteractor: WeatherForecastInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherForecastViewModel(
            app,
            weatherForecastInteractor
        ) as T
    }
}