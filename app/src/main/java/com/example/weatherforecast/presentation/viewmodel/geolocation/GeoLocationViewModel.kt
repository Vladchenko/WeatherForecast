package com.example.weatherforecast.presentation.viewmodel.geolocation

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import com.example.weatherforecast.R
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.geolocation.GeoLocationListener
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.presentation.viewmodel.SingleLiveEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * View model for geo location.
 *
 * @property app custom [Application] implementation for Hilt.
 */
class GeoLocationViewModel(private val app: Application) : AbstractViewModel(app),
    GeoLocationListener {

    private var permissionRequests = 0
    private var chosenCity: String = ""
    private var chosenLocation: Location? = null
    private var temperatureType: TemperatureType? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("WeatherForecastViewModel", throwable.message ?: "")
        if (throwable is IOException) {
            _onShowErrorLiveData.postValue(throwable.message)
        }
        if (throwable is NoInternetException) {
            _onShowErrorLiveData.postValue(throwable.cause.toString())
        }
        throwable.stackTrace.forEach {
            Log.e("WeatherForecastViewModel", it.toString())
        }
    }

    //region livedata fields
    private val _onDefineCurrentGeoLocationLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
    //TODO Find out where this one should be called from
    private val _onDefineCityByGeoLocationLiveData: SingleLiveEvent<Location> = SingleLiveEvent()
    private val _onDefineCityByCurrentGeoLocationLiveData: SingleLiveEvent<Location> =
        SingleLiveEvent()
    private val _onDefineCityByCurrentGeoLocationSuccessLiveData: SingleLiveEvent<String> =
        SingleLiveEvent()
    private val _onShowLocationPermissionAlertDialogLiveData: SingleLiveEvent<Unit> =
        SingleLiveEvent()
    private val _onRequestPermissionLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _onRequestPermissionDeniedLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _onSaveCityLiveData: SingleLiveEvent<CityLocationModel> = SingleLiveEvent()
    private val _onGetWeatherForecastForCityLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    private val _onGetWeatherForecastForLocationLiveData: SingleLiveEvent<CityLocationModel> =
        SingleLiveEvent()
    //endregion livedata fields

    //region livedata getters fields
    val onDefineCityByGeoLocationLiveData: LiveData<Location>
        get() = _onDefineCityByGeoLocationLiveData

    val onDefineCurrentGeoLocationLiveData: LiveData<Unit>
        get() = _onDefineCurrentGeoLocationLiveData

    val onDefineCityByCurrentGeoLocationLiveData: LiveData<Location>
        get() = _onDefineCityByCurrentGeoLocationLiveData

    val onGetWeatherForecastForCityLiveData: LiveData<String>
        get() = _onGetWeatherForecastForCityLiveData

    val onGetWeatherForecastForLocationLiveData: LiveData<CityLocationModel>
        get() = _onGetWeatherForecastForLocationLiveData

    val onRequestPermissionLiveData: LiveData<Unit>
        get() = _onRequestPermissionLiveData

    val onRequestPermissionDeniedLiveData: LiveData<Unit>
        get() = _onRequestPermissionDeniedLiveData

    val onSaveCityLiveData: LiveData<CityLocationModel>
        get() = _onSaveCityLiveData

    val onShowGeoLocationAlertDialogLiveData: LiveData<String>
        get() = _onDefineCityByCurrentGeoLocationSuccessLiveData

    val onShowLocationPermissionAlertDialogLiveData: LiveData<Unit>
        get() = _onShowLocationPermissionAlertDialogLiveData
    //endregion livedata getters fields

    override fun onNoGeoLocationPermission() {
        requestGeoLocationPermissionOrLoadForecast()
    }

    override fun onCurrentGeoLocationSuccess(location: Location) {
        chosenLocation = location
        _onDefineCityByCurrentGeoLocationLiveData.postValue(location)
        _onShowProgressBarLiveData.postValue(false)
    }

    override fun onCurrentGeoLocationFail(errorMessage: String) {
        Log.e("GeoLocationViewModel",errorMessage)
        // Since exception is not informative enough for user, replace it with a standard error one.
        if (errorMessage.contains("permission")) {
            _onShowErrorLiveData.postValue(app.getString(R.string.geo_location_permission_required))
        } else {
            _onShowErrorLiveData.postValue(errorMessage)
        }
    }

    /**
     * Requests a geo location or downloads a forecast, depending on a presence of a [chosenCity]
     * or [savedCity], having [temperatureType] provided.
     */
    fun requestGeoLocationPermissionOrDownloadWeatherForecast(chosenCity: String, savedCity: String) {
        if (chosenCity.isBlank()
            && savedCity.isBlank()) {
            requestGeoLocationPermissionOrLoadForecast()
        } else {
            val city = chosenCity.ifBlank {
                savedCity
            }
            Log.d("GeoLocationViewModel", "getWeatherForecast for city $city")
            _onGetWeatherForecastForCityLiveData.postValue(city)
        }
    }

    /**
     * Proceed with a geo location permission result, having [isGranted] flag as a permission result,
     * [chosenCity] as a previously chosen city on city selection screen, or a [savedCity] loaded
     * from database.
     */
    fun onPermissionResolution(isGranted: Boolean, chosenCity: String, savedCity: String) {
        if (isGranted) {
            Log.d(
                "CurrentTimeForecastFragment",
                "Permission granted callback. Chosen city = $chosenCity, saved city = $savedCity"
            )
            if (chosenCity.isNotBlank()) {
                // Get weather forecast, when there is a chosen city (from a city selection fragment)
                _onGetWeatherForecastForCityLiveData.postValue(chosenCity)
            } else {
                if (savedCity.isNotBlank()) {
                    // Get weather forecast, when there is a saved city (from a database)
                    _onGetWeatherForecastForCityLiveData.postValue(savedCity)
                } else {
                    // Else show alert dialog on a city defined by current geo location
                    _onDefineCurrentGeoLocationLiveData.call()
                }
            }
        } else {
            _onShowErrorLiveData.postValue(app.applicationContext.getString(R.string.geo_location_no_permission))
            _onShowLocationPermissionAlertDialogLiveData.call()
        }
    }

    /**
     * Request geo location permission, when it is not granted.
     */
    fun requestGeoLocationPermissionOrLoadForecast() {
        if (!hasPermissionForGeoLocation()) {
            _onUpdateStatusLiveData.postValue(app.applicationContext.getString(R.string.geo_location_permission_required))
            permissionRequests++
            if (permissionRequests > 2) {
                _onRequestPermissionDeniedLiveData.call()
            } else {
                _onRequestPermissionLiveData.postValue(Unit)
                Log.d("GeoLocationViewModel", "requestGeoLocationPermission")
            }
        } else {
            if (chosenCity.isBlank()) {
                _onDefineCurrentGeoLocationLiveData.call()
            } else {
                _onGetWeatherForecastForCityLiveData.postValue(chosenCity)
            }
        }
    }

    suspend fun onDefineCityFromLocation(geoCoder: Geocoder, location: Location) {
        _onUpdateStatusLiveData.postValue("Defining city from location")
        withContext(Dispatchers.IO) {
            geoCoder.getFromLocation(location.latitude, location.longitude, 1).first().locality
        }
    }

    fun hasPermissionForGeoLocation() =
        (ActivityCompat.checkSelfPermission(
            app.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)

    /**
     * City locating successful callback, receiving [city] and its latitude, longitude as [location].
     */
    fun onDefineCityByGeoLocationSuccess(city: String, location: Location) {
        Log.d("GeoLocationViewModel", "City defined successfully by geo location")
        Log.d("GeoLocationViewModel", "city = $city, location = $location")
        _onSaveCityLiveData.postValue(CityLocationModel(city, location))
    }

    /**
     * City defining by current geo location successful callback.
     */
    fun onDefineCityByCurrentGeoLocationSuccess(city: String) {
        Log.d("GeoLocationViewModel", "City defined successfully by CURRENT geo location")
        Log.d("GeoLocationViewModel", "city = $city, location = $chosenLocation")
//        viewModelScope.launch(exceptionHandler) {
//            chosenCityInteractor.saveChosenCity(city, chosenLocation!!)     //TODO
//        }
        _onSaveCityLiveData.postValue(CityLocationModel(city, chosenLocation!!))    //TODO Remove !!
        _onDefineCityByCurrentGeoLocationSuccessLiveData.postValue(city)
        chosenCity = city
    }

    /**
     * Callback for successful geo location.
     * Save a [city] and its [location] and download its forecast.
     */
    fun onDefineGeoLocationByCitySuccess(city: String, location: Location) {
        Log.d("GeoLocationViewModel", "Geo location defined successfully by city")
        Log.d("GeoLocationViewModel", "city = $city, location = $location")
//        viewModelScope.launch(exceptionHandler) {
//            chosenCityInteractor.saveChosenCity(city, location)
//        }
        val cityModel = CityLocationModel(city, location)
        _onSaveCityLiveData.postValue(cityModel)
        Log.d("GeoLocationViewModel", "Let's download a forecast for them")
//        getWeatherForecast(city, location)
        _onGetWeatherForecastForLocationLiveData.postValue(cityModel)
    }

    /**
     * Callback for failed geo location. Show an error message.
     */
    fun onDefineGeoLocationByCityFail(errorMessage: String) {
        Log.e("GeoLocationViewModel", errorMessage)
        _onShowErrorLiveData.postValue(errorMessage)
    }

    /**
     * Set chosen city
     */
    fun setChosenCity(city: String) {
        chosenCity = city
    }
}