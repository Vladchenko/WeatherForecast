package com.example.weatherforecast.presentation.viewmodel.geolocation

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.api.customexceptions.GeoLocationException
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.geolocation.GeoLocationListener
import com.example.weatherforecast.geolocation.Geolocator
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator
import com.example.weatherforecast.geolocation.hasPermissionForGeoLocation
import com.example.weatherforecast.models.domain.CityLocationModel
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
 * @property app custom [Application] implementation for Hilt
 * @property geoLocationHelper provides geo location service
 * @property geoLocator provides geo location service
 * @property chosenCityInteractor saves/loads chosen city
 * @property coroutineDispatchers dispatchers for coroutines
 */
@HiltViewModel
class GeoLocationViewModel @Inject constructor(
    private val app: Application,
    private val geoLocationHelper: Geolocator,
    connectivityObserver: ConnectivityObserver,
    private val geoLocator: WeatherForecastGeoLocator,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
) : AbstractViewModel(connectivityObserver, coroutineDispatchers) {

    private var permissionRequests = 0
    private var geoLocatingAttempts = 0

    val selectCityFlow: SharedFlow<Unit>
        get() = _selectCityFlow
    val geoGeoLocationPermissionFlow: SharedFlow<GeoLocationPermission>
        get() = _geoGeoLocationPermissionFlow
    val geoLocationDefineCitySuccessFlow: SharedFlow<String>
        get() = _geoLocationDefineCitySuccessFlow
    val geoLocationSuccessFlow: SharedFlow<Location>
        get() = _geoLocationSuccessFlow
    val geoLocationByCitySuccessFlow: SharedFlow<CityLocationModel>
        get() = _geoLocationByCitySuccessFlow

    private val _selectCityFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val _geoGeoLocationPermissionFlow = MutableSharedFlow<GeoLocationPermission>(
        extraBufferCapacity = 1 // Collector is not alive when flow emits value, so buffer is needed
    )
    private val _geoLocationSuccessFlow = MutableSharedFlow<Location>(
        extraBufferCapacity = 1
    )
    private val _geoLocationDefineCitySuccessFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 1
    )
    private val _geoLocationByCitySuccessFlow = MutableSharedFlow<CityLocationModel>(
        extraBufferCapacity = 1
    )

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        showProgressBarState.value = false
        Log.e(TAG, throwable.message.orEmpty())
        Log.e(TAG, throwable.stackTraceToString())

        if (throwable is GeoLocationException) {
            showError(throwable.message.toString())
            retryGeoLocationOrGotoCitySelectionScreen()
        } else {
            showError(throwable.message.toString())
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
        if (hasPermissionForGeoLocation(app.applicationContext)) {
            defineCurrentGeoLocation()
        } else {
            permissionRequests++
            if (permissionRequests > 2) {
                _geoGeoLocationPermissionFlow.tryEmit(GeoLocationPermission.PermanentlyDenied)
            } else {
                _geoGeoLocationPermissionFlow.tryEmit(GeoLocationPermission.Requested)
                Log.i(TAG, "Geo location permission requested")
            }
        }
    }

    fun defineCurrentGeoLocation() {
        geoLocator.defineCurrentLocation(object : GeoLocationListener {
            override fun onCurrentGeoLocationSuccess(location: Location) {
                _geoLocationSuccessFlow.tryEmit(location)
                showProgressBarState.value = false
            }

            override fun onCurrentGeoLocationFail(errorMessage: String) {
                Log.e(TAG, errorMessage)
                showError(errorMessage)
            }

            override fun onNoGeoLocationPermission() {
                Log.i(TAG, "No geo location permission - requesting it")
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
                Log.i(
                    TAG,
                    "Geo location defined successfully for city = $city, location = $location"
                )
                val cityModel = CityLocationModel(city, location)
                saveChosenCity(cityModel)
                Log.i(TAG, "City and its location saved successfully.")
                _geoLocationByCitySuccessFlow.tryEmit(cityModel)
            } catch (ex: Exception) {
                showError(ex.message.toString())
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
            Log.d(TAG, "Chosen city saved to database: ${locationModel.city}")
        }
    }

    /**
     * Defines a city name that matches given [location]
     */
    fun defineCityNameByLocation(location: Location) {
        viewModelScope.launch(coroutineDispatchers.io + exceptionHandler) {
            val city = geoLocationHelper.defineCityNameByLocation(location)
            Log.d(
                TAG,
                "City defined successfully by location = $location, city = $city"
            )
            saveChosenCity(CityLocationModel(city, location))
            _geoLocationDefineCitySuccessFlow.tryEmit(city)
        }
    }

    companion object {
        private const val TAG = "GeoLocationViewModel"
        private const val GEO_LOCATING_ATTEMPTS = 3
        private const val DELAY_BETWEEN_ATTEMPTS = 1000L
    }
}