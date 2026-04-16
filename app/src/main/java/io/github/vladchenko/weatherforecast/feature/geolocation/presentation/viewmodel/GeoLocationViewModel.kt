package io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.feature.geolocation.data.DeviceLocationProvider
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationException
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationListener
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.Geolocator
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.PermissionChecker
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.model.GeoLocationPermission
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.presentation.viewmodel.AbstractViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * View model for geo location or city name of a device.
 *
 * @constructor
 * @param connectivityObserver provides connectivity state
 * @property geoLocationHelper provides geo location service
 * @property loggingService centralized service for application logging
 * @property statusRenderer displays loading, success, warning, or error statuses
 * @property resourceManager provides access to string resources for dynamic UI content
 * @property geoLocator provides geo location service
 * @property permissionChecker to check if needed permission is provided
 * @property chosenCityInteractor saves/loads chosen city
 * @property coroutineDispatchers dispatchers for coroutines
 */
@HiltViewModel
class GeoLocationViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    private val geoLocationHelper: Geolocator,
    private val loggingService: LoggingService,
    private val statusRenderer: StatusRenderer,
    private val resourceManager: ResourceManager,
    private val geoLocator: DeviceLocationProvider,
    private val permissionChecker: PermissionChecker,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
) : AbstractViewModel(connectivityObserver) {

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
     * - [io.github.vladchenko.weatherforecast.feature.geolocation.presentation.model.GeoLocationPermission.Requested] – permission is being requested
     * - [io.github.vladchenko.weatherforecast.feature.geolocation.presentation.model.GeoLocationPermission.Denied] – user denied permission once
     * - [io.github.vladchenko.weatherforecast.feature.geolocation.presentation.model.GeoLocationPermission.PermanentlyDenied] – user denied multiple times (treated as permanent denial)
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
        showProgressBarState.value = false
        loggingService.logError(TAG, throwable.message.orEmpty())
        loggingService.logError(TAG, throwable.stackTraceToString())

        if (throwable is GeoLocationException) {
            statusRenderer.showError(throwable.message.toString())
            retryGeoLocationOrGotoCitySelectionScreen()
        } else {
            statusRenderer.showError(throwable.message.toString())
        }
    }

    private fun retryGeoLocationOrGotoCitySelectionScreen() {
        geoLocatingAttempts++
        if (geoLocatingAttempts == GEO_LOCATING_ATTEMPTS) {
            statusRenderer.showError(
                resourceManager.getString(R.string.geo_retry)
            )
            _geoLocationFailFlow.tryEmit(Unit)
            geoLocatingAttempts = 0
        } else {
            viewModelScope.launch {
                statusRenderer.showError(
                    resourceManager.getString(R.string.geo_max_attempts_exceeded)
                )
                delay(DELAY_BETWEEN_ATTEMPTS)
                defineCurrentGeoLocation()
            }
        }
    }

    /**
     * Request geo location permission, when it is not granted.
     */
    fun requestGeoLocationPermission() {
        if (permissionChecker.hasLocationPermission()) {
            defineCurrentGeoLocation()
        } else {
            permissionRequests++
            if (permissionRequests > 2) {
                _geoGeoLocationPermissionFlow.tryEmit(GeoLocationPermission.PermanentlyDenied)
            } else {
                _geoGeoLocationPermissionFlow.tryEmit(GeoLocationPermission.Requested)
                loggingService.logInfoEvent(TAG, "Geo location permission requested")
            }
        }
    }

    fun resetGeoLocationRequestAttempts() {
        permissionRequests = 0
        geoLocatingAttempts = 0
    }

    fun defineCurrentGeoLocation() {
        geoLocator.defineCurrentLocation(object : GeoLocationListener {
            override fun onCurrentGeoLocationSuccess(location: Location) {
                _geoLocationSuccessFlow.tryEmit(location)
                showProgressBarState.value = false
            }

            override fun onCurrentGeoLocationFail(errorMessage: String) {
                loggingService.logError(TAG, errorMessage)
                statusRenderer.showError(errorMessage)
            }

            override fun onNoGeoLocationPermission() {
                loggingService.logInfoEvent(TAG, "No geo location permission - requesting it")
                requestGeoLocationPermission()
            }
        })
    }

    /**
     * Proceed with a geo location permission result, having [isGranted] flag as a permission result.
     */
    fun onPermissionResolution(isGranted: Boolean) {
        if (isGranted) {
            defineCurrentGeoLocation()
        } else {
            _geoGeoLocationPermissionFlow.tryEmit(GeoLocationPermission.Denied)
        }
    }

    /**
     * Defines a city name that matches given [location]
     */
    fun defineCityNameByLocation(location: Location) {
        viewModelScope.launch(coroutineDispatchers.io + exceptionHandler) {
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

    companion object {
        private const val TAG = "GeoLocationViewModel"
        private const val GEO_LOCATING_ATTEMPTS = 3
        private const val DELAY_BETWEEN_ATTEMPTS = 2000L
    }
}