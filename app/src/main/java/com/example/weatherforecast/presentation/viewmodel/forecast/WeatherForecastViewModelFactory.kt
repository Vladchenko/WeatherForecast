package com.example.weatherforecast.presentation.viewmodel.forecast

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor

/**
 * View model factory
 *
 * @property app custom [Application] implementation for Hilt
 * @property weatherForecastRemoteInteractor provides domain layer data through network
 * @property weatherForecastLocalInteractor  provides domain layer data through database
 */
class WeatherForecastViewModelFactory(
    private val app: Application,
    private val weatherForecastRemoteInteractor: WeatherForecastRemoteInteractor,
    private val weatherForecastLocalInteractor: WeatherForecastLocalInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherForecastViewModel(
            app,
            weatherForecastRemoteInteractor,
            weatherForecastLocalInteractor
        ) as T
    }
}