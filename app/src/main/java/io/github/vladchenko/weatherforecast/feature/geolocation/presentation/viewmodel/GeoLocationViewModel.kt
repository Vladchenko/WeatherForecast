package io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.ui.status.StatusType.Error
import io.github.vladchenko.weatherforecast.core.ui.status.StatusType.Info
import io.github.vladchenko.weatherforecast.core.ui.status.StatusType.Warning
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.geolocation.data.DeviceLocationProvider
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationException
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationListener
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.Geolocator
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.model.GeoLocationPermission
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel.GeoLocationViewModel.Companion.GEO_LOCATING_ATTEMPTS
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
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
 * 3. **Reverse Geocoding**: Converts [Location] coordinates to a human-readable city name
 *    using [Geolocator].
 *
 * The ViewModel emits various events via [SharedFlow]s to notify the UI layer (or Coordinators)
 * about state changes, such as location acquisition, errors, or navigation requirements.
 *
 * ## Retry Mechanism
 * Implements an automatic retry mechanism ([retryGeoLocationOrGotoCitySelectionScreen]) for hardware
 * location failures, attempting up to [GEO_LOCATING_ATTEMPTS] times before failing the entire process.
 *
 * @property geoLocationHelper Provides reverse geocoding capabilities (coordinates -> city name).
 * @property loggingService Centralized service for application logging.
 * @property resourceManager Provides access to string resources for UI messages.
 * @property geoLocator Hardware service for retrieving current device location.
 * @property statusStateHolder Manages and broadcasts UI status messages (errors, warnings, info).
 *
 * @see DeviceLocationProvider
 * @see Geolocator
 * @see GeoLocationPermission
 */
@HiltViewModel
class GeoLocationViewModel @Inject constructor(
    private val geoLocationHelper: Geolocator,
    private val loggingService: LoggingService,
    private val resourceManager: ResourceManager,
    private val geoLocator: DeviceLocationProvider,
    private val statusStateHolder: StatusStateHolder,
) : ViewModel() {

    private var permissionRequests = 0
    private var geoLocatingAttempts = 0

    /**
     * Unified event bus for the entire geolocation workflow.
     *
     * Consolidates all state changes, user interactions, and results into a single [SharedFlow].
     * Emits events of type [GeoLocationEvent], covering permission state transitions, raw location
     * acquisition, city resolution, and navigation triggers. Replaces multiple individual flows to
     * simplify UI handling. The [extraBufferCapacity = 1] ensures events are not dropped during
     * ViewModel-to-UI state transitions.
     */
    val geoLocationEventsFlow: SharedFlow<GeoLocationEvent>
        get() = _geoLocationEventsFlow

    private val _geoLocationEventsFlow =
        MutableSharedFlow<GeoLocationEvent>(extraBufferCapacity = 1)

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        loggingService.logError(TAG, throwable.message.orEmpty())
        loggingService.logError(TAG, throwable.stackTraceToString())

        if (throwable is GeoLocationException) {
            // TODO Bad to retry from exceptionHandler, replace with using try-catch when needed
            retryGeoLocationOrGotoCitySelectionScreen()
        }
        showError(throwable.message.toString())
    }

    private fun retryGeoLocationOrGotoCitySelectionScreen() {
        geoLocatingAttempts++
        if (geoLocatingAttempts == GEO_LOCATING_ATTEMPTS) {
            showError(
                resourceManager.getString(R.string.geo_retry)
            )
            _geoLocationEventsFlow.tryEmit(GeoLocationEvent.DefineLocationFail)
            geoLocatingAttempts = 0
        } else {
            viewModelScope.launch {
                showError(
                    resourceManager.getString(R.string.geo_max_attempts_exceeded)
                )
                delay(DELAY_BETWEEN_ATTEMPTS)
                defineDeviceGeoLocation()
            }
        }
    }

    fun defineDeviceGeoLocation() {
        loggingService.logInfoEvent(TAG, "defineDeviceGeoLocation called")
        statusStateHolder.updateStatus(
            Info(
                resourceManager.getString(R.string.geo_detecting)
            )
        )
        geoLocator.defineDeviceLocation(object : GeoLocationListener {
            override fun onDeviceGeoLocationSuccess(location: Location) {
                loggingService.logError(TAG, "Device geo location success")
                statusStateHolder.updateStatus(
                    Info(resourceManager.getString(R.string.geo_success))
                )
                _geoLocationEventsFlow.tryEmit(GeoLocationEvent.DeviceGeoLocationSuccess(location))
            }

            override fun onDeviceGeoLocationFail(errorMessage: String) {
                loggingService.logError(TAG, errorMessage)
                showError(errorMessage)
            }

            override fun onNoGeoLocationPermission() {
                loggingService.logInfoEvent(TAG, "No geo location permission - emitting Requested")
                statusStateHolder.updateStatus(
                    Info(resourceManager.getString(R.string.geo_permission_required))
                )
                // Emit Requested state to trigger permission request via WeatherActivity
                _geoLocationEventsFlow.tryEmit(GeoLocationEvent.GeoLocationPermission.Requested)
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
            statusStateHolder.updateStatus(
                Info(resourceManager.getString(R.string.geo_granted))
            )
            // Immediately proceed with location retrieval
            _geoLocationEventsFlow.tryEmit(GeoLocationEvent.GeoLocationPermission.Granted)
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
                statusStateHolder.updateStatus(
                    Error(resourceManager.getString(R.string.geo_permission_denied_permanently))
                )
                _geoLocationEventsFlow.tryEmit(GeoLocationEvent.GeoLocationPermission.PermanentlyDenied)
            } else {
                loggingService.logInfoEvent(
                    TAG,
                    "onPermissionResolution: emitting Denied (permissionRequests=$permissionRequests)"
                )
                statusStateHolder.updateStatus(
                    Warning(resourceManager.getString(R.string.geo_permission_denied))
                )
                _geoLocationEventsFlow.tryEmit(GeoLocationEvent.GeoLocationPermission.Denied)
            }
        }
    }

    /**
     * Defines a city name that matches given [location]
     */
    fun defineCityNameByLocation(location: Location) {
        viewModelScope.launch(exceptionHandler) {
            val city = geoLocationHelper.defineCityNameByLocation(location)
            loggingService.logDebugEvent(
                TAG,
                "City defined successfully by location = $location, city = $city"
            )
            val cityModel = CityLocationModel(city, location)
            _geoLocationEventsFlow.tryEmit(
                GeoLocationEvent.DefineCityNameByLocationSuccess(
                    cityModel
                )
            )
        }
    }

    private fun showError(errorMessage: String) {
        statusStateHolder.updateStatus(
            Error(
                errorMessage
            )
        )
    }

    companion object {
        private const val TAG = "GeoLocationViewModel"
        private const val GEO_LOCATING_ATTEMPTS = 3
        private const val DELAY_BETWEEN_ATTEMPTS = 2000L
    }
}