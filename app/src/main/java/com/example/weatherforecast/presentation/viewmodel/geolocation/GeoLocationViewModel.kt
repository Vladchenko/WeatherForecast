package com.example.weatherforecast.presentation.viewmodel.geolocation

import android.location.Location
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.api.customexceptions.GeoLocationException
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.data.util.permission.PermissionChecker
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.geolocation.DeviceLocationProvider
import com.example.weatherforecast.geolocation.GeoLocationListener
import com.example.weatherforecast.geolocation.Geolocator
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
 * @property statusRenderer Displays loading, success, warning, or error statuses
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
    private val geoLocator: DeviceLocationProvider,
    private val permissionChecker: PermissionChecker,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
) : AbstractViewModel(connectivityObserver) {

    private var permissionRequests = 0
    private var geoLocatingAttempts = 0

    val selectCityFlow: SharedFlow<Unit>
        get() = _selectCityFlow
    val geoGeoLocationPermissionFlow: SharedFlow<GeoLocationPermission>
        get() = _geoGeoLocationPermissionFlow
    val geoLocationDefineCitySuccessFlow: SharedFlow<CityLocationModel>
        get() = _geoLocationDefineCitySuccessFlow
    val geoLocationSuccessFlow: SharedFlow<Location>
        get() = _geoLocationSuccessFlow
    val geoLocationByCitySuccessFlow: SharedFlow<Unit>
        get() = _geoLocationByCitySuccessFlow

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
            // TODO Bad UX approach - one should inform the user about the error, not send him right away to city chooser screen. As of now, there is no any user informing mechanism.
            _selectCityFlow.tryEmit(Unit)
        } else {
            viewModelScope.launch {
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
     * Define geo location by [city]
     */
    fun defineLocationByCity(city: String) {
        viewModelScope.launch(coroutineDispatchers.io) {
            try {
                val location = geoLocationHelper.defineLocationByCity(city)
                loggingService.logInfoEvent(
                    TAG,
                    "Geo location defined successfully for city = $city, location = $location"
                )
                val cityModel = CityLocationModel(city, location)
                saveChosenCity(cityModel)
                loggingService.logInfoEvent(TAG, "City and its location saved successfully.")
                _geoLocationByCitySuccessFlow.tryEmit(Unit)
            } catch (ex: Exception) {
                statusRenderer.showError(ex.message.toString())
            }
        }
    }

    /**
     * Save chosen city with data from [locationModel]
     */
    private fun saveChosenCity(locationModel: CityLocationModel) {
        viewModelScope.launch(exceptionHandler) {
            chosenCityInteractor.saveChosenCity(
                locationModel.city,
                locationModel.location
            )
            loggingService.logDebugEvent(
                TAG,
                "Chosen city saved to database: ${locationModel.city}"
            )
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
            saveChosenCity(cityModel)
            _geoLocationByCitySuccessFlow.tryEmit(Unit)
            _geoLocationDefineCitySuccessFlow.tryEmit(cityModel)
        }
    }

    companion object {
        private const val TAG = "GeoLocationViewModel"
        private const val GEO_LOCATING_ATTEMPTS = 3
        private const val DELAY_BETWEEN_ATTEMPTS = 1000L
    }
}