package io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.domain.model.Coordinate
import io.github.vladchenko.weatherforecast.core.geolocation.GeoLocationEvent
import io.github.vladchenko.weatherforecast.core.geolocation.GeoLocationEventBus
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.geolocation.data.DeviceLocationProvider
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationException
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationListener
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.Geolocator
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel.GeoLocationViewModel.Companion.GEO_LOCATING_ATTEMPTS
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogController
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the device's geolocation workflow.
 *
 * This ViewModel orchestrates the entire process of determining the user's current location:
 * 1. **Permission Handling**: Manages the lifecycle of location permissions (Requested, Denied, PermanentlyDenied)
 *    using [permissionRequests] counter to detect permanent denials.
 * 2. **Hardware Location**: Delegates raw GPS/Network location retrieval to [DeviceLocationProvider].
 * 3. **Reverse Geocoding**: Converts [Coordinate] coordinates to a human-readable city name
 *    using [Geolocator].
 *
 * The ViewModel emits various events via [SharedFlow]s to notify the UI layer (or Coordinators)
 * about state changes, such as location acquisition, errors, or navigation requirements,
 * broadcasting them through [geoLocationEventBus].
 *
 * ## Retry Mechanism
 * Implements an automatic retry mechanism ([retryGeoLocationOrGotoCitySelectionScreen]) for hardware
 * location failures, attempting up to [GEO_LOCATING_ATTEMPTS] times before failing the entire process.
 *
 * @property geoLocationHelper Provides reverse geocoding capabilities (coordinates -> city name).
 * @property loggingService Centralized service for application logging.
 * @property geoLocator Hardware service for retrieving current device location.
 * @property statusStateHolder Manages and broadcasts UI status messages (errors, warnings, info).
 * @property geoLocationEventBus Unified event bus for broadcasting geolocation-related events.
 */
@HiltViewModel
class GeoLocationViewModel @Inject constructor(
    private val geoLocationHelper: Geolocator,
    private val loggingService: LoggingService,
    private val geoLocator: DeviceLocationProvider,
    private val statusStateHolder: StatusStateHolder,
    private val dialogController: WeatherDialogController,
    private val geoLocationEventBus: GeoLocationEventBus,
) : ViewModel() {

    private var permissionRequests = 0
    private var geoLocatingAttempts = 0

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        loggingService.logError(TAG, throwable.message.orEmpty())
        loggingService.logError(TAG, throwable.stackTraceToString())

        if (throwable is GeoLocationException) {
            // TODO Bad to retry from exceptionHandler, replace with using try-catch when needed
            retryGeoLocationOrGotoCitySelectionScreen()
        }
        statusStateHolder.updateErrorStatus(throwable.message.toString())
    }

    fun defineDeviceGeoLocation() {
        loggingService.logInfoEvent(TAG, "defineDeviceGeoLocation called")
        statusStateHolder.updateInfoStatus(R.string.geo_detecting)
        geoLocator.defineDeviceLocation(object : GeoLocationListener {
            override fun onDeviceGeoLocationSuccess(coordinate: Coordinate) {
                loggingService.logError(TAG, "Device geo location success")
                statusStateHolder.updateInfoStatus(R.string.geo_success)
                defineCityNameByLocation(coordinate)
            }

            override fun onDeviceGeoLocationFail(errorMessage: String) {
                loggingService.logError(TAG, errorMessage)
                statusStateHolder.updateErrorStatus(errorMessage)
            }

            override fun onNoGeoLocationPermission() {
                loggingService.logInfoEvent(TAG, "No geo location permission - emitting Requested")
                statusStateHolder.updateInfoStatus(R.string.geo_permission_required)
                // Emit Requested state to trigger permission request via WeatherActivity
                geoLocationEventBus.send(GeoLocationEvent.RequestPermission)
            }
        })
    }

    /**
     * Proceed with a geo location permission result, having [isGranted] flag as a permission result.
     */
    fun onPermissionResolution(isGranted: Boolean) {
        loggingService.logInfoEvent(
            TAG,
            "onPermissionResolution: isGranted=$isGranted, previous permissionRequests=$permissionRequests"
        )
        if (isGranted) {
            // Reset permission counter when permission is granted
            permissionRequests = 0
            loggingService.logInfoEvent(
                TAG,
                "onPermissionResolution: permission granted, permissionRequests reset to 0"
            )
            statusStateHolder.updateInfoStatus(R.string.geo_granted)
            // Immediately proceed with location retrieval
            defineDeviceGeoLocation()
        } else {
            loggingService.logInfoEvent(TAG, "onPermissionResolution: permission denied")
            // Increment counter on denial and emit Denied state
            permissionRequests++
            loggingService.logInfoEvent(
                TAG,
                "onPermissionResolution: incrementing permissionRequests to $permissionRequests"
            )
            if (permissionRequests > 1) {
                loggingService.logInfoEvent(
                    TAG,
                    "onPermissionResolution: emitting PermanentlyDenied (permissionRequests=$permissionRequests)"
                )
                showError(R.string.geo_permission_denied_permanently)
                dialogController.showPermissionPermanentlyDenied(
                    onPositiveClick = {
                        geoLocationEventBus.send(GeoLocationEvent.OnPermanentlyDenied)
                    },
                    onNegativeClick = {
                        geoLocationEventBus.send(GeoLocationEvent.OnPermanentlyDenied)
                    }
                )
            } else {
                loggingService.logInfoEvent(
                    TAG,
                    "onPermissionResolution: emitting Denied (permissionRequests=$permissionRequests)"
                )
                statusStateHolder.updateWarningStatus(R.string.geo_permission_denied)
                dialogController.showNoPermission(
                    onPositiveClick = {
                        geoLocationEventBus.send(GeoLocationEvent.RequestPermission)
                    },
                    onNegativeClick = {
                        geoLocationEventBus.send(GeoLocationEvent.OnNegativeNoPermission)
                    }
                )
            }
        }
    }

    /**
     * Defines a city name that matches given [coordinate]
     */
    fun defineCityNameByLocation(coordinate: Coordinate) {
        viewModelScope.launch(exceptionHandler) {
            val city = geoLocationHelper.defineCityNameByLocation(coordinate)
            loggingService.logDebugEvent(
                TAG,
                "City defined successfully by location = $coordinate, city = $city"
            )
            val cityModel =
                CityLocationModel(
                    city,
                    Coordinate(coordinate.latitude, coordinate.longitude)
                )
            dialogController.showLocationDefined(
                city = cityModel.city,
                onPositiveClick = {
                    geoLocationEventBus.send(GeoLocationEvent.OnForecastLoadForLocation(cityModel))
                },
                onNegativeClick = {
                    statusStateHolder.updateInfoStatus(R.string.city_selection_title)
                    geoLocationEventBus.send(GeoLocationEvent.GotoCitySelection)
                }
            )
        }
    }

    private fun retryGeoLocationOrGotoCitySelectionScreen() {
        geoLocatingAttempts++
        if (geoLocatingAttempts == GEO_LOCATING_ATTEMPTS) {
            showError(R.string.geo_retry)
            dialogController.showGeoLocationError(
                onPositiveClick = {
                    geoLocationEventBus.send(GeoLocationEvent.GotoCitySelection)
                },
                onNegativeClick = {
                    statusStateHolder.updateInfoStatus(R.string.geo_detecting)
                    defineDeviceGeoLocation()
                }
            )
            geoLocatingAttempts = 0
        } else {
            viewModelScope.launch {
                showError(R.string.geo_max_attempts_exceeded)
                delay(DELAY_BETWEEN_ATTEMPTS)
                defineDeviceGeoLocation()
            }
        }
    }

    private fun showError(errorMessage: Int) {
        statusStateHolder.updateErrorStatus(errorMessage)
    }

    companion object {
        private const val TAG = "GeoLocationViewModel"
        private const val GEO_LOCATING_ATTEMPTS = 3
        private const val DELAY_BETWEEN_ATTEMPTS = 2000L
    }
}