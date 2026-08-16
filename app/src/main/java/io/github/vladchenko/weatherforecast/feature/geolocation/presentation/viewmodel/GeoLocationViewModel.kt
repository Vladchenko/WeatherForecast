package io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.ui.status.StatusType
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
     * Emitted when the user should be redirected to the city selection screen.
     *
     * This typically happens when geolocation fails after maximum retry attempts,
     * or when the user denies permission permanently.
     */
    val selectCityFlow: SharedFlow<Unit>
        get() = _selectCityFlow

    /**
     * Emits the current state of location permission request:
     * - [GeoLocationPermission.Requested] – permission is being requested
     * - [GeoLocationPermission.Denied] – user denied permission once
     * - [GeoLocationPermission.PermanentlyDenied] – user denied multiple times (treated as permanent denial)
     *
     * Used by the UI to show appropriate rationale or redirect to settings.
     */
    val geoGeoLocationPermissionFlow: SharedFlow<GeoLocationPermission>
        get() = _geoGeoLocationPermissionFlow

    /**
     * Emitted when a city name has been successfully resolved from the device's location.
     *
     * Carries a [io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel] containing the city name and associated coordinates.
     *
     * Triggers UI updates such as updating the selected city or refreshing weather data.
     */
    val geoLocationDefineCitySuccessFlow: SharedFlow<CityLocationModel>
        get() = _geoLocationDefineCitySuccessFlow

    /**
     * Emitted when the device's raw GPS location has been successfully obtained.
     *
     * Contains a [android.location.Location] object with latitude and longitude.
     *
     * Used to trigger downstream operations like reverse geocoding to find the city name.
     */
    val geoLocationSuccessFlow: SharedFlow<Location>
        get() = _geoLocationSuccessFlow

    /**
     * Emitted when a city has been successfully processed and saved as the chosen city.
     *
     * Indicates that geolocation-to-city workflow completed and the app can proceed
     * with loading weather data or navigating away from the geolocation screen.
     */
    val geoLocationByCitySuccessFlow: SharedFlow<Unit>
        get() = _geoLocationByCitySuccessFlow

    /**
     * Emitted when geolocation has failed after exhausting all retry attempts.
     *
     * Signals that the app should either allow manual city selection or let the user retry manually.
     *
     * Does not carry data — serves as a trigger for UI navigation or user interaction.
     */
    val geoLocationFailFlow: SharedFlow<Unit>
        get() = _geoLocationFailFlow

    private val _selectCityFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val _geoGeoLocationPermissionFlow = MutableSharedFlow<GeoLocationPermission>(
        extraBufferCapacity = 1 // Collector is not alive when flow emits value, so buffer is needed
    )
    private val _geoLocationSuccessFlow = MutableSharedFlow<Location>(
        extraBufferCapacity = 1
    )
    private val _geoLocationDefineCitySuccessFlow = MutableSharedFlow<CityLocationModel>(
        extraBufferCapacity = 1
    )
    private val _geoLocationByCitySuccessFlow = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )
    private val _geoLocationFailFlow = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        loggingService.logError(TAG, throwable.message.orEmpty())
        loggingService.logError(TAG, throwable.stackTraceToString())

        if (throwable is GeoLocationException) {
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
            _geoLocationFailFlow.tryEmit(Unit)
            geoLocatingAttempts = 0
        } else {
            viewModelScope.launch {
                showError(
                    resourceManager.getString(R.string.geo_max_attempts_exceeded)
                )
                delay(DELAY_BETWEEN_ATTEMPTS)
                defineCurrentGeoLocation()
            }
        }
    }

    fun defineCurrentGeoLocation() {
        loggingService.logInfoEvent(TAG, "defineCurrentGeoLocation called")
        geoLocator.defineCurrentLocation(object : GeoLocationListener {
            override fun onCurrentGeoLocationSuccess(location: Location) {
                _geoLocationSuccessFlow.tryEmit(location)
            }

            override fun onCurrentGeoLocationFail(errorMessage: String) {
                loggingService.logError(TAG, errorMessage)
                showError(errorMessage)
            }

            override fun onNoGeoLocationPermission() {
                loggingService.logInfoEvent(TAG, "No geo location permission - emitting Requested")
                // Emit Requested state to trigger permission request via WeatherActivity
                _geoGeoLocationPermissionFlow.tryEmit(GeoLocationPermission.Requested)
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
            // Immediately proceed with location retrieval
            defineCurrentGeoLocation()
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
                _geoGeoLocationPermissionFlow.tryEmit(GeoLocationPermission.PermanentlyDenied)
            } else {
                loggingService.logInfoEvent(
                    TAG,
                    "onPermissionResolution: emitting Denied (permissionRequests=$permissionRequests)"
                )
                _geoGeoLocationPermissionFlow.tryEmit(GeoLocationPermission.Denied)
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
            _geoLocationByCitySuccessFlow.tryEmit(Unit)
            _geoLocationDefineCitySuccessFlow.tryEmit(cityModel)
        }
    }

    private fun showError(errorMessage: String) {
        statusStateHolder.updateStatus(
            StatusType.Error(
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