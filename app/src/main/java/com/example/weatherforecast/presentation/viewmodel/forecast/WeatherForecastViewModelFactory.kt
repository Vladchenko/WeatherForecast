package com.example.weatherforecast.presentation.viewmodel.forecast

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator

/**
 * View model factory
 *
 * @param app custom [Application] implementation for Hilt
 * @param geoLocator provides geo location service
 * @param chosenCityInteractor downloads a previously chosen city
 * @param weatherForecastRemoteInteractor provides domain layer data for weather forecast through network
 * @param weatherForecastLocalInteractor  provides domain layer data for weather forecast through database
 */
class WeatherForecastViewModelFactory(
    private val app: Application,
    private val geoLocator: WeatherForecastGeoLocator,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val weatherForecastLocalInteractor: WeatherForecastLocalInteractor,
    private val weatherForecastRemoteInteractor: WeatherForecastRemoteInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherForecastViewModel(
            app,
            geoLocator,
            chosenCityInteractor,
            weatherForecastLocalInteractor,
            weatherForecastRemoteInteractor
        ) as T
    }
}