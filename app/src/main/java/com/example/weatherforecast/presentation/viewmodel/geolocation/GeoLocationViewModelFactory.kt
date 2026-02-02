package com.example.weatherforecast.presentation.viewmodel.geolocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.util.permission.PermissionChecker
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.geolocation.Geolocator
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator

/**
 * GeoLocationViewModel factory
 *
 * @property permissionChecker to check if needed permission is provided
 * @property geoLocationHelper provides geo location service
 * @property geoLocator provides geo location service
 * @property connectivityObserver internet connectivity observer
 * @property chosenCityInteractor downloads a previously chosen city
 * @property coroutineDispatchers dispatchers for coroutines
 */
class GeoLocationViewModelFactory(
    private val permissionChecker: PermissionChecker,
    private val geoLocationHelper: Geolocator,
    private val geoLocator: WeatherForecastGeoLocator,
    private val connectivityObserver: ConnectivityObserver,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GeoLocationViewModel(
            permissionChecker,
            geoLocationHelper,
            connectivityObserver,
            geoLocator,
            chosenCityInteractor,
            coroutineDispatchers
        ) as T
    }
}