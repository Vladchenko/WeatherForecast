package com.example.weatherforecast.presentation.viewmodel.geolocation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.geolocation.Geolocator
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator

/**
 * View model factory
 *
 * @param app custom [Application] implementation for Hilt
 * @param geoLocationHelper provides geo location service
 * @param geoLocator provides geo location service
 * @param chosenCityInteractor downloads a previously chosen city
 */
class GeoLocationViewModelFactory(
    private val app: Application,
    private val geoLocationHelper: Geolocator,
    private val geoLocator: WeatherForecastGeoLocator,
    private val chosenCityInteractor: ChosenCityInteractor,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GeoLocationViewModel(
            app,
            geoLocationHelper,
            geoLocator,
            chosenCityInteractor,
        ) as T
    }
}