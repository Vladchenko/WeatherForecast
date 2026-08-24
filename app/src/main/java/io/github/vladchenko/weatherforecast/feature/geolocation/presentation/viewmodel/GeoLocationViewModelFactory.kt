package io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.vladchenko.weatherforecast.core.geolocation.GeoLocationEventBus
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.geolocation.data.DeviceLocationProvider
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.Geolocator
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogController

/**
 * Factory for creating instances of [GeoLocationViewModel].
 *
 * This factory provides the necessary dependencies for the geolocation feature,
 * including location services, logging, and status management.
 *
 * @property geoLocationHelper Service responsible for reverse geocoding (converting coordinates to city names).
 * @property loggingService Centralized service for structured application logging.
 * @property geoLocator Service responsible for retrieving the device's current location (GPS/Network).
 * @property statusStateHolder Manager for handling and broadcasting UI status messages (e.g., errors, info).
 * @property geoLocationEventBus Unified event bus for broadcasting geolocation-related events.
 */
@Suppress("UNCHECKED_CAST")
class GeoLocationViewModelFactory(
    private val geoLocationHelper: Geolocator,
    private val loggingService: LoggingService,
    private val geoLocator: DeviceLocationProvider,
    private val statusStateHolder: StatusStateHolder,
    private val dialogController: WeatherDialogController,
    private val geoLocationEventBus: GeoLocationEventBus,
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
            geoLocator,
            statusStateHolder,
            dialogController,
            geoLocationEventBus
        ) as T
    }
}