package io.github.vladchenko.weatherforecast.presentation.viewmodel.geolocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.vladchenko.weatherforecast.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.data.util.LoggingService
import io.github.vladchenko.weatherforecast.data.util.permission.PermissionChecker
import io.github.vladchenko.weatherforecast.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.domain.city.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.geolocation.DeviceLocationProvider
import io.github.vladchenko.weatherforecast.geolocation.Geolocator
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.utils.ResourceManager

/**
 * GeoLocationViewModel factory
 *
 * @property geoLocationHelper provides geo location service
 * @property loggingService centralized service for application logging
 * @property permissionChecker to check if needed permission is provided
 * @property statusRenderer displays status messages to the user
 * @property resourceManager helper to retrieve localized strings from resources
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
    private val resourceManager: ResourceManager,
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
            resourceManager,
            geoLocator,
            permissionChecker,
            chosenCityInteractor,
            coroutineDispatchers
        ) as T
    }
}