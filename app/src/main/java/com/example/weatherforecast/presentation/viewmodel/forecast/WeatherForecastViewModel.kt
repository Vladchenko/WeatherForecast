package com.example.weatherforecast.presentation.viewmodel.forecast

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.data.api.customexceptions.CityNotFoundException
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.geolocation.GeoLocationListener
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.network.NetworkConnectionListener
import com.example.weatherforecast.network.NetworkUtils.isNetworkAvailable
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.presentation.viewmodel.SingleLiveEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

/**
 * View model (MVVM component) for weather forecast presentation.
 *
 * @property app custom [Application] implementation for Hilt.
 * @property weatherForecastRemoteInteractor provides domain layer data.
 */
class WeatherForecastViewModel(
    private val app: Application,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val weatherForecastRemoteInteractor: WeatherForecastRemoteInteractor,
    private val weatherForecastLocalInteractor: WeatherForecastLocalInteractor
) : AbstractViewModel(app), GeoLocationListener, NetworkConnectionListener {

    private var permissionRequests = 0
    private var chosenCity: String = ""
    private var chosenLocation: Location? = null
    private var temperatureType: TemperatureType? = null

    //region livedata fields
    private val _onChosenCityNotFoundLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    private val _onCityRequestFailedLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    private val _onDefineCurrentGeoLocationLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _onDefineCityByGeoLocationLiveData: SingleLiveEvent<Location> = SingleLiveEvent()
    private val _onDefineCityByCurrentGeoLocationLiveData: SingleLiveEvent<Location> =
        SingleLiveEvent()
    private val _onDefineCityByCurrentGeoLocationSuccessLiveData: SingleLiveEvent<String> =
        SingleLiveEvent()
    private val _onShowLocationPermissionAlertDialogLiveData: SingleLiveEvent<Unit> =
        SingleLiveEvent()
    private val _onGotoCitySelectionLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _onRequestPermissionLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _onRequestPermissionDeniedLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _onShowProgressBarLiveData: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _onShowWeatherForecastLiveData: SingleLiveEvent<WeatherForecastDomainModel> =
        SingleLiveEvent()
    //endregion livedata fields

    //region livedata getters fields
    val onChosenCityNotFoundLiveData: LiveData<String>
        get() = _onChosenCityNotFoundLiveData

    val onCityRequestFailedLiveData: LiveData<String>
        get() = _onCityRequestFailedLiveData

    val onDefineCityByGeoLocationLiveData: LiveData<Location>
        get() = _onDefineCityByGeoLocationLiveData

    val onDefineCurrentGeoLocationLiveData: LiveData<Unit>
        get() = _onDefineCurrentGeoLocationLiveData

    val onGotoCitySelectionLiveData: LiveData<Unit>
        get() = _onGotoCitySelectionLiveData

    val onDefineCityByCurrentGeoLocationLiveData: LiveData<Location>
        get() = _onDefineCityByCurrentGeoLocationLiveData

    val onGetWeatherForecastLiveData: LiveData<WeatherForecastDomainModel>
        get() = _onShowWeatherForecastLiveData

    val onRequestPermissionLiveData: LiveData<Unit>
        get() = _onRequestPermissionLiveData

    val onRequestPermissionDeniedLiveData: LiveData<Unit>
        get() = _onRequestPermissionDeniedLiveData

    val onShowProgressBarLiveData: LiveData<Boolean>
        get() = _onShowProgressBarLiveData

    val onShowGeoLocationAlertDialogLiveData: LiveData<String>
        get() = _onDefineCityByCurrentGeoLocationSuccessLiveData

    val onShowLocationPermissionAlertDialogLiveData: LiveData<Unit>
        get() = _onShowLocationPermissionAlertDialogLiveData
    //endregion livedata getters fields

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("WeatherForecastViewModel", throwable.message ?: "")
        if (throwable is CityNotFoundException) {
            _onShowErrorLiveData.postValue(throwable.message)
            _onChosenCityNotFoundLiveData.postValue(throwable.city)
        }
        if (throwable is NoInternetException) {
            _onShowErrorLiveData.postValue(throwable.cause.toString())
        }
        if (throwable is NoSuchDatabaseEntryException) {
            _onShowErrorLiveData.postValue(
                app.applicationContext.getString(
                    R.string.database_record_for_city_not_found, throwable.message
                )
            )
        }
        throwable.stackTrace.forEach {
            Log.e("WeatherForecastViewModel", it.toString())
        }
    }

    override fun onNetworkConnectionAvailable() {
        _onUpdateStatusLiveData.postValue(app.applicationContext.getString(R.string.network_available_text))
        requestGeoLocationPermissionOrDownloadWeatherForecast(chosenCity)
    }

    override fun onNetworkConnectionLost() {
        _onShowErrorLiveData.postValue(app.applicationContext.getString(R.string.network_not_available_error_text))
    }

    override fun onNoGeoLocationPermission() {
        requestGeoLocationPermissionOrLoadForecast()
    }

    override fun onCurrentGeoLocationSuccess(location: Location) {
        chosenLocation = location
        _onDefineCityByCurrentGeoLocationLiveData.postValue(location)
    }

    override fun onCurrentGeoLocationFail(errorMessage: String) {
        _onShowErrorLiveData.postValue(errorMessage)
    }

    /**
     * Download weather forecast on a [temperatureType] and [chosenCity].
     */
    fun getWeatherForecast(chosenCity: String) {
        if (isNetworkAvailable(app.applicationContext)) {
            getForecastWhenNetworkAvailable(chosenCity)
        } else {
            getForecastWhenNetworkNotAvailable(chosenCity)
        }
    }

    private fun getForecastWhenNetworkAvailable(chosenCity: String) {
        if (chosenCity.isNotBlank()) {
            Log.d("WeatherForecastViewModel", "Chosen city is $chosenCity")
            this.chosenCity = chosenCity
            downloadWeatherForecastForCityOrGeoLocation(chosenCity)
        } else {
            Log.d("WeatherForecastViewModel", "Chosen city is empty")
            // Try loading a city model from DB
            viewModelScope.launch(exceptionHandler) {
                val cityModel = chosenCityInteractor.loadChosenCityModel()
                Log.d(
                    "WeatherForecastViewModel",
                    "Chosen city loaded from database = ${cityModel.city}, lat = ${cityModel.location.latitude}, lon = ${cityModel.location.longitude}"
                )
                // When there is no loaded city model from database,
                if (cityModel.city.isBlank()) {
                    // Define local city and try downloading a forecast for it
                    _onUpdateStatusLiveData.postValue(app.applicationContext.getString(R.string.current_location_defining_text))
                    // Following row defines a city and displays an alert
                    _onDefineCurrentGeoLocationLiveData.postValue(Unit)
                } else {
                    downloadWeatherForecastForCityOrGeoLocation(
                        cityModel.city
                    )
                }
            }
        }
    }

    private fun getForecastWhenNetworkNotAvailable(chosenCity: String) {
        if (chosenCity.isBlank()) {
            // Try loading a city model from DB
            viewModelScope.launch(exceptionHandler) {
                val cityModel = chosenCityInteractor.loadChosenCityModel()
                Log.d(
                    "WeatherForecastViewModel",
                    "Downloaded from database: city = ${cityModel.city}, lat = ${cityModel.location.latitude}, lon = ${cityModel.location.longitude}" //TODO Move to strings.xml
                )
                // If it is null, show error
                if (cityModel.city.isBlank()) {
                    _onShowErrorLiveData.postValue(app.applicationContext.getString(R.string.network_not_available_error_text))
                    // Else download a forecast from database
                } else {
                    downloadForecastFromDatabase(cityModel.city ?: "")
                }
            }
        } else {
            viewModelScope.launch(exceptionHandler) {
                downloadForecastFromDatabase(chosenCity)
            }
        }
    }

    /**
     * Requests a geo location or downloads a forecast, depending on a presence of a [chosenCity],
     * having [temperatureType] provided.
     */
    private fun requestGeoLocationPermissionOrDownloadWeatherForecast(chosenCity: String) {
        if (chosenCity.isBlank()) {
            requestGeoLocationPermissionOrLoadForecast()
        } else {
            Log.d("WeatherForecastViewModel", "getWeatherForecast for city $chosenCity")
            getWeatherForecast(chosenCity)
        }
    }

    fun onPermissionResolution(isGranted: Boolean, chosenCity: String) {
        if (isGranted) {
            Log.d(
                "CurrentTimeForecastFragment",
                "Chosen city for a permission granted callback is = $chosenCity"
            )
            if (chosenCity.isBlank()) {
                getWeatherForecast(chosenCity)
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
                Log.d("WeatherForecastViewModel", "requestGeoLocationPermission")
            }
        } else {
            getWeatherForecast(chosenCity)
        }
    }

    private fun hasPermissionForGeoLocation() =
        (ActivityCompat.checkSelfPermission(
            app.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)

    private fun getWeatherForecast(city: String, location: Location) {
        _onShowProgressBarLiveData.postValue(true)
        viewModelScope.launch(exceptionHandler) {
            var result = weatherForecastRemoteInteractor.loadRemoteForecastForLocation(
                temperatureType ?: TemperatureType.CELSIUS,
                location.latitude,
                location.longitude
            )
            // City in response is different than city in request
            result = result.copy(city = city)
            processServerResponse(result)
        }
    }

    private fun downloadWeatherForecastForCityOrGeoLocation(city: String) {
        _onShowProgressBarLiveData.postValue(true)
        try {
            viewModelScope.launch(exceptionHandler) {
                val result = weatherForecastRemoteInteractor.loadRemoteForecastForCity(
                    temperatureType ?: TemperatureType.CELSIUS,
                    city
                )
                processServerResponse(result)
            }
        } catch (ex: Exception) {
            Log.e(
                "WeatherForecastViewModel",
                app.applicationContext.getString(R.string.forecast_downloading_for_city_succeeded)
            )
            Log.e("WeatherForecastViewModel", ex.stackTraceToString())
            _onShowErrorLiveData.postValue(ex.message)
            _onCityRequestFailedLiveData.postValue(city)    //In fact, defines location and loads forecast
        }
    }

    private suspend fun saveChosenCity(result: WeatherForecastDomainModel) {
        viewModelScope.launch(exceptionHandler) {
            chosenCityInteractor.saveChosenCity(
                result.city,
                getLocationByLatLon(
                    result.coordinate.latitude,
                    result.coordinate.longitude
                )
            )
        }
    }

    private fun getLocationByLatLon(latitude: Double, longitude: Double): Location {
        val location = Location(LocationManager.NETWORK_PROVIDER)
        location.latitude = latitude
        location.longitude = longitude
        return location
    }

    private suspend fun processServerResponse(result: WeatherForecastDomainModel) {
        if (result.serverError.isBlank()) {
            Log.d("WeatherForecastViewModel", result.toString())
            _onShowWeatherForecastLiveData.postValue(result)
            saveChosenCity(result)
            saveForecastToDatabase(result)
        } else {
            _onShowErrorLiveData.postValue(result.serverError)
            Log.e("WeatherForecastViewModel8", result.serverError)
        }
    }

    private fun saveForecastToDatabase(result: WeatherForecastDomainModel) {
        Log.d("WeatherForecastViewModel", "Saving forecast to database $result")
        viewModelScope.launch(exceptionHandler) {
            weatherForecastLocalInteractor.saveForecast(result)
        }
        Log.d("WeatherForecastViewModel", "Forecast saved successfully")
    }

    private fun downloadForecastFromDatabase(city: String) {
        viewModelScope.launch(exceptionHandler) {
            val result = weatherForecastLocalInteractor.loadForecast(city)
            _onShowWeatherForecastLiveData.postValue(result)
            Log.d("WeatherForecastViewModel", result.toString())
            _onShowErrorLiveData.postValue(app.applicationContext.getString(R.string.database_forecast_downloading))
        }
    }

    /**
     * City locating successful callback, receiving [city] and its latitude, longitude as [location].
     */
    fun onDefineCityByGeoLocationSuccess(city: String, location: Location) {
        Log.d("WeatherForecastViewModel", "City defined successfully by geo location")
        Log.d("WeatherForecastViewModel", "city = $city, location = $location")
        viewModelScope.launch(exceptionHandler) {
            chosenCityInteractor.saveChosenCity(city, location)
        }
    }

    /**
     * City defining by current geo location successful callback.
     */
    fun onDefineCityByCurrentGeoLocationSuccess(city: String) {
        Log.d("WeatherForecastViewModel", "City defined successfully by CURRENT geo location")
        Log.d("WeatherForecastViewModel", "city = $city, location = $chosenLocation")
        viewModelScope.launch(exceptionHandler) {
            chosenCityInteractor.saveChosenCity(city, chosenLocation!!)     //TODO
        }
        _onDefineCityByCurrentGeoLocationSuccessLiveData.postValue(city)
        chosenCity = city
    }

    /**
     * Callback for successful geo location.
     * Save a [city] and its [location] and download its forecast.
     */
    fun onDefineGeoLocationByCitySuccess(city: String, location: Location) {
        Log.d("WeatherForecastViewModel", "Geo location defined successfully by city")
        Log.d("WeatherForecastViewModel", "city = $city, location = $location")
        viewModelScope.launch(exceptionHandler) {
            chosenCityInteractor.saveChosenCity(city, location)
        }
        Log.d("WeatherForecastViewModel", "Let's download a forecast for them")
        getWeatherForecast(city, location)
    }

    /**
     * Callback for failed geo location. Show an error message.
     */
    fun onDefineGeoLocationByCityFail(errorMessage: String) {
        Log.e("WeatherForecastViewModel", errorMessage)
        _onShowErrorLiveData.postValue(errorMessage)
    }

    /**
     * Go to city selection screen.
     */
    fun onGotoCitySelection() {
        _onGotoCitySelectionLiveData.call()
    }

    /**
     * Set chosen city
     */
    fun setChosenCity(city: String) {
        chosenCity = city
    }

    /**
     * Set temperature type
     */
    fun setTemperatureType(temperatureType: TemperatureType) {
        this.temperatureType = temperatureType
    }
}