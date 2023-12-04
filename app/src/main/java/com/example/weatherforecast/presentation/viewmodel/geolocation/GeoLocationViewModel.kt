package com.example.weatherforecast.presentation.viewmodel.geolocation

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Defines geo location or city name of a device.
 */
@HiltViewModel
class GeoLocationViewModel @Inject constructor(
    private val app: Application,
    private val geoLocationHelper: Geolocator,
    private val geoLocator: WeatherForecastGeoLocator,
    private val chosenCityInteractor: ChosenCityInteractor,
) : AbstractViewModel(app) {

    private var permissionRequests = 0
    private var chosenLocation = Location("")

    val onShowGeoLocationAlertDialogLiveData: LiveData<String>
        get() = _onDefineCityByCurrentGeoLocationSuccessLiveData
    val onCurrentGeoLocationSuccessfulTriangulationLiveData: LiveData<Location>
        get() = _onCurrentGeoLocationSuccessfulTriangulationLiveData
    val onLoadWeatherForecastForLocationLiveData: LiveData<CityLocationModel>
        get() = _onLoadWeatherForecastForLocationLiveData
    val onShowNoPermissionForLocationTriangulatingAlertDialogLiveData: LiveData<Unit>
        get() = _onShowNoPermissionForLocationTriangulatingAlertDialogLiveData
    val onLoadForecastLiveData: LiveData<String>
        get() = _onLoadCityForecastLiveData
    val onRequestPermissionDeniedLiveData: LiveData<Unit>
        get() = _onRequestPermissionDeniedLiveData
    val onRequestPermissionLiveData: LiveData<Unit>
        get() = _onRequestPermissionLiveData

    private val _onLoadWeatherForecastForLocationLiveData = SingleLiveEvent<CityLocationModel>()
    private val _onDefineCityByCurrentGeoLocationSuccessLiveData = SingleLiveEvent<String>()
    private val _onCurrentGeoLocationSuccessfulTriangulationLiveData = SingleLiveEvent<Location>()
    private val _onShowNoPermissionForLocationTriangulatingAlertDialogLiveData = SingleLiveEvent<Unit>()
    private val _onRequestPermissionDeniedLiveData = SingleLiveEvent<Unit>()
    private val _onRequestPermissionLiveData = SingleLiveEvent<Unit>()
    private val _onLoadCityForecastLiveData = SingleLiveEvent<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, throwable.message.orEmpty())
        when (throwable) {
            is NoInternetException -> {
                onShowError(throwable.message.toString())
                viewModelScope.launch(Dispatchers.IO) {
                    delay(PresentationUtils.REPEAT_INTERVAL)
                    // weatherForecastDownloadJob.start()
                    //TODO Work through each case - for location for city
                }
            }

            is NoSuchDatabaseEntryException -> {
                onShowError(
                    app.applicationContext.getString(
                        R.string.database_entry_for_city_not_found, throwable.message
                    )
                )
            }

            else -> {
                Log.e(
                    TAG,
                    app.applicationContext.getString(R.string.forecast_downloading_for_city_failed)
                )
                Log.e(TAG, throwable.stackTraceToString())
                onShowError(throwable.message.toString())
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
            onShowStatus(app.applicationContext.getString(R.string.geo_location_permission_required))
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
        onShowStatus(app.applicationContext.getString(R.string.current_location_defining_text))
        geoLocator.defineCurrentLocation(app.applicationContext, object : GeoLocationListener {
            override fun onCurrentGeoLocationSuccess(location: Location) {
                this@GeoLocationViewModel.chosenLocation = location
                _onCurrentGeoLocationSuccessfulTriangulationLiveData.postValue(location)
                showProgressBarState.value = false
            }

            override fun onCurrentGeoLocationFail(errorMessage: String) {
                Log.e(TAG, errorMessage)
                // Since exception is not informative enough for user, replace it with a standard error one.
                if (errorMessage.contains("permission")) {
                    onShowError(R.string.geo_location_permission_required)
                    requestGeoLocationPermission()
                } else {
                    onShowError(errorMessage)
                }
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
            onShowError(app.applicationContext.getString(R.string.geo_location_no_permission))
            _onShowNoPermissionForLocationTriangulatingAlertDialogLiveData.call()
        }
    }

    fun defineLocationByCity(city: String) {
        viewModelScope.launch(Dispatchers.IO) {
            onShowStatus("Defining geo location for city $city")
            try {
                val location = geoLocationHelper.defineLocationByCity(city)
                Log.d(TAG, "Geo location defined successfully for city = $city, location = $location")
                val cityModel = CityLocationModel(city, location)
                saveChosenCity(cityModel)
                Log.d(TAG, "City and its location saved successfully.")
                // downloadWeatherForecastForLocation(cityModel)
                _onLoadWeatherForecastForLocationLiveData.postValue(cityModel)
            } catch (ex: Exception) {
                onShowError(ex.message.toString())
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
        viewModelScope.launch(Dispatchers.IO) {
            onShowStatus("Defining city from geo location")
            val city = geoLocationHelper.defineCityNameByLocation(location)
            Log.d(
                TAG,
                "City defined successfully by CURRENT geo location, city = $city, location = $chosenLocation"
            )
            saveChosenCity(CityLocationModel(city, chosenLocation))
            _onDefineCityByCurrentGeoLocationSuccessLiveData.postValue(city)
            //TODO chosenCity = city
        }
    }

    companion object {
        private const val TAG = "GeoLocationViewModel"
    }
}