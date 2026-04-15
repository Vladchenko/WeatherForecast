package io.github.vladchenko.weatherforecast.presentation.viewmodel.geolocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.vladchenko.weatherforecast.core.location.geolocation.DeviceLocationProvider
import io.github.vladchenko.weatherforecast.core.location.geolocation.geolocator.Geolocator
import io.github.vladchenko.weatherforecast.core.location.permission.PermissionChecker
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.domain.city.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer

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