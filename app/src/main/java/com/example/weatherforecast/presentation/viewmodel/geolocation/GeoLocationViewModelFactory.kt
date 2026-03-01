package com.example.weatherforecast.presentation.viewmodel.geolocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.data.util.permission.PermissionChecker
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.geolocation.DeviceLocationProvider
import com.example.weatherforecast.geolocation.Geolocator
import com.example.weatherforecast.presentation.status.StatusRenderer

/**
 * GeoLocationViewModel factory
 *
 * @property geoLocationHelper provides geo location service
 * @property loggingService centralized service for application logging
 * @property permissionChecker to check if needed permission is provided
 * @property statusRenderer displays status messages to the user
 * @property geoLocator provides geo location service
 * @property connectivityObserver internet connectivity observer
 * @property chosenCityInteractor downloads a previously chosen city
 * @property coroutineDispatchers dispatchers for coroutines
 */
@Suppress("UNCHECKED_CAST")
class GeoLocationViewModelFactory(
    private val geoLocationHelper: Geolocator,
    private val loggingService: LoggingService,
    private val statusRenderer: StatusRenderer,
    private val geoLocator: DeviceLocationProvider,
    private val permissionChecker: PermissionChecker,
    private val connectivityObserver: ConnectivityObserver,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GeoLocationViewModel(
            connectivityObserver,
            geoLocationHelper,
            loggingService,
            statusRenderer,
            geoLocator,
            permissionChecker,
            chosenCityInteractor,
            coroutineDispatchers
        ) as T
    }
}