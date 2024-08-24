package com.example.weatherforecast.presentation.viewmodel.geolocation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.geolocation.Geolocator
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator

/**
 * View model factory
 *
 * @property app custom [Application] implementation for Hilt
 * @property geoLocationHelper provides geo location service
 * @property geoLocator provides geo location service
 * @property chosenCityInteractor downloads a previously chosen city
 * @property coroutineDispatchers dispatchers for coroutines
 */
class GeoLocationViewModelFactory(
    private val app: Application,
    private val geoLocationHelper: Geolocator,
    private val geoLocator: WeatherForecastGeoLocator,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GeoLocationViewModel(
            app,
            geoLocationHelper,
            geoLocator,
            chosenCityInteractor,
            coroutineDispatchers
        ) as T
    }
}