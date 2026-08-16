package io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.geolocation.data.DeviceLocationProvider
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.Geolocator

/**
 * Factory for creating instances of [GeoLocationViewModel].
 *
 * This factory provides the necessary dependencies for the geolocation feature,
 * including location services, logging, and status management.
 *
 * @property geoLocationHelper Service responsible for reverse geocoding (converting coordinates to city names).
 * @property loggingService Centralized service for structured application logging.
 * @property resourceManager Helper to retrieve localized strings and application resources.
 * @property geoLocator Service responsible for retrieving the device's current location (GPS/Network).
 * @property statusStateHolder Manager for handling and broadcasting UI status messages (e.g., errors, info).
 */
@Suppress("UNCHECKED_CAST")
class GeoLocationViewModelFactory(
    private val geoLocationHelper: Geolocator,
    private val loggingService: LoggingService,
    private val resourceManager: ResourceManager,
    private val geoLocator: DeviceLocationProvider,
    private val statusStateHolder: StatusStateHolder,
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of [GeoLocationViewModel].
     *
     * @param modelClass The class of the ViewModel to create.
     * @return The newly created [GeoLocationViewModel] instance.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GeoLocationViewModel(
            geoLocationHelper,
            loggingService,
            resourceManager,
            geoLocator,
            statusStateHolder
        ) as T
    }
}