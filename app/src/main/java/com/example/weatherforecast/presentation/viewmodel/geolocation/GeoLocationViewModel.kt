package com.example.weatherforecast.presentation.viewmodel.geolocation

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.geolocation.GeoLocationListener
import com.example.weatherforecast.geolocation.Geolocator
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator
import com.example.weatherforecast.geolocation.hasPermissionForGeoLocation
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.presentation.PresentationUtils
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.presentation.viewmodel.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
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
    private val geoLocator: WeatherForecastGeoLocator,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
) : AbstractViewModel(coroutineDispatchers) {

    private var permissionRequests = 0

    val onLoadForecastLiveData: LiveData<String>
        get() = _onLoadCityForecastLiveData
    val onRequestPermissionLiveData: LiveData<Unit>
        get() = _onRequestPermissionLiveData
    val onRequestPermissionDeniedLiveData: LiveData<Unit>
        get() = _onRequestPermissionDeniedLiveData
    val onShowGeoLocationAlertDialogLiveData: LiveData<String>
        get() = _onDefineCityByCurrentGeoLocationSuccessLiveData
    val onDefineCurrentGeoLocationSuccessLiveData: LiveData<Location>
        get() = _onDefineCurrentGeoLocationSuccessLiveData
    val onDefineGeoLocationByCitySuccessLiveData: LiveData<CityLocationModel>
        get() = _onDefineGeoLocationByCitySuccessLiveData
    val onShowNoPermissionForLocationTriangulatingAlertDialogLiveData: LiveData<Unit>
        get() = _onShowNoPermissionForLocationTriangulatingAlertDialogLiveData

    private val _onRequestPermissionLiveData = SingleLiveEvent<Unit>()
    private val _onLoadCityForecastLiveData = SingleLiveEvent<String>()
    private val _onRequestPermissionDeniedLiveData = SingleLiveEvent<Unit>()
    private val _onDefineCurrentGeoLocationSuccessLiveData = SingleLiveEvent<Location>()
    private val _onDefineCityByCurrentGeoLocationSuccessLiveData = SingleLiveEvent<String>()
    private val _onDefineGeoLocationByCitySuccessLiveData = SingleLiveEvent<CityLocationModel>()
    private val _onShowNoPermissionForLocationTriangulatingAlertDialogLiveData = SingleLiveEvent<Unit>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, throwable.message.orEmpty())
        when (throwable) {
            is NoInternetException -> {
                showError(throwable.message.toString())
                viewModelScope.launch(coroutineDispatchers.io) {
                    delay(PresentationUtils.REPEAT_INTERVAL)
                    // weatherForecastDownloadJob.start()
                    //TODO Work through each case - for location for city
                }
            }

            is NoSuchDatabaseEntryException -> {
                showError(R.string.database_entry_for_city_not_found, throwable.message.toString())
            }

            else -> {
                Log.e(TAG, throwable.stackTraceToString())
                showError(throwable.message.toString())
                //In fact, defines location and loads forecast for it
                // _onCityRequestFailedLiveData.postValue(chosenCity)
                // TODO What should go here ?
            }
        }
        throwable.stackTrace.forEach {
            Log.e(TAG, it.toString())
        }
    }

    /**
     * Request geo location permission, when it is not granted.
     */
    fun requestGeoLocationPermission() {
        if (hasPermissionForGeoLocation(app.applicationContext)) {
            defineCurrentGeoLocation()
        } else {
            showStatus(R.string.geo_location_permission_required)
            permissionRequests++
            if (permissionRequests > 2) {
                _onRequestPermissionDeniedLiveData.postValue(Unit)
            } else {
                _onRequestPermissionLiveData.postValue(Unit)
                Log.d(TAG, "Geo location permission requested")
            }
        }
    }

    fun defineCurrentGeoLocation() {
        showStatus(R.string.current_location_triangulating)
        geoLocator.defineCurrentLocation(object : GeoLocationListener {
            override fun onCurrentGeoLocationSuccess(location: Location) {
                _onDefineCurrentGeoLocationSuccessLiveData.postValue(location)
                showProgressBarState.value = false
            }

            override fun onCurrentGeoLocationFail(errorMessage: String) {
                Log.e(TAG, errorMessage)
                showError(errorMessage)
            }

            override fun onNoGeoLocationPermission() {
                Log.e(TAG, "No geo location permission - requesting it")
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
            showError(R.string.geo_location_no_permission)
            _onShowNoPermissionForLocationTriangulatingAlertDialogLiveData.call()
        }
    }

    /**
     * Define geo location by [city]
     */
    fun defineLocationByCity(city: String) {
        viewModelScope.launch(coroutineDispatchers.io) {
            showStatus("Defining geo location for city $city")
            try {
                val location = geoLocationHelper.defineLocationByCity(city)
                Log.d(TAG, "Geo location defined successfully for city = $city, location = $location")
                val cityModel = CityLocationModel(city, location)
                saveChosenCity(cityModel)
                Log.d(TAG, "City and its location saved successfully.")
                _onDefineGeoLocationByCitySuccessLiveData.postValue(cityModel)
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
        viewModelScope.launch(coroutineDispatchers.io) {
            showStatus("Defining city from geo location")
            val city = geoLocationHelper.defineCityNameByLocation(location)
            Log.d(
                TAG,
                "City defined successfully by location = $location, city = $city"
            )
            saveChosenCity(CityLocationModel(city, location))
            _onDefineCityByCurrentGeoLocationSuccessLiveData.postValue(city)
        }
    }

    companion object {
        private const val TAG = "GeoLocationViewModel"
    }
}