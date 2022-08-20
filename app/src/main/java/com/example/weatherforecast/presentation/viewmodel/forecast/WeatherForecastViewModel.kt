package com.example.weatherforecast.presentation.viewmodel.forecast

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.data.api.customexceptions.CityNotFoundException
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.geolocation.GeoLocationListener
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.network.NetworkUtils.isNetworkAvailable
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
) : AndroidViewModel(app), GeoLocationListener {

    private var chosenCity: String = ""

    //region livedata fields
    private val _showWeatherForecastLiveData: MutableLiveData<WeatherForecastDomainModel> = MutableLiveData()
    private val _showErrorLiveData: MutableLiveData<String> = MutableLiveData()
    private val _updateStatusLiveData: MutableLiveData<String> = MutableLiveData()
    private val _showProgressBarLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val _defineCityByGeoLocationLiveData: SingleLiveEvent<Location> = SingleLiveEvent()
    private val _defineCityCurrentByGeoLocationLiveData: SingleLiveEvent<Location> = SingleLiveEvent()
    private val _showGeoLocationAlertDialogLiveData: SingleLiveEvent<CityLocationModel> = SingleLiveEvent()
    private val _requestPermissionLiveData: MutableLiveData<Unit> = MutableLiveData()
    private val _defineGeoLocationByCityLiveData: MutableLiveData<String> = MutableLiveData()
    private val _defineCurrentGeoLocationLiveData: MutableLiveData<Unit> = MutableLiveData()
    private val _gotoCitySelectionLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _chooseAnotherCityLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    //endregion livedata fields

    //region livedata getters fields
    val getWeatherForecastLiveData: LiveData<WeatherForecastDomainModel>
        get() = _showWeatherForecastLiveData

    val showErrorLiveData: LiveData<String>
        get() = _showErrorLiveData

    val updateStatusLiveData: LiveData<String>
        get() = _updateStatusLiveData

    val showProgressBarLiveData: LiveData<Boolean>
        get() = _showProgressBarLiveData

    val defineCityByGeoLocationLiveData: LiveData<Location>
        get() = _defineCityByGeoLocationLiveData

    val defineCityByCurrentGeoLocationLiveData: LiveData<Location>
        get() = _defineCityCurrentByGeoLocationLiveData

    val showGeoLocationAlertDialogLiveData: LiveData<CityLocationModel>
        get() = _showGeoLocationAlertDialogLiveData

    val requestPermissionLiveData: LiveData<Unit>
        get() = _requestPermissionLiveData

    val defineGeoLocationByCityLiveData: LiveData<String>
        get() = _defineGeoLocationByCityLiveData

    val defineCurrentGeoLocationLiveData: LiveData<Unit>
        get() = _defineCurrentGeoLocationLiveData

    val gotoCitySelectionLiveData: LiveData<Unit>
        get() = _gotoCitySelectionLiveData

    val chooseAnotherCityLiveData: LiveData<String>
        get() = _chooseAnotherCityLiveData
    //endregion livedata getters fields

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("WeatherForecastViewModel", throwable.message ?: "")
        if (throwable is CityNotFoundException) {
            onUpdateStatus(throwable.message)
            _chooseAnotherCityLiveData.postValue(throwable.city)
        }
        if (throwable is NoInternetException) {
            _showErrorLiveData.postValue(throwable.cause.toString())
        }
        throwable.stackTrace.forEach {
            Log.e("WeatherForecastViewModel", it.toString())
        }
        _showErrorLiveData.postValue(throwable.message ?: "")
    }

    /**
     * Download weather forecast on a [temperatureType] and [chosenCity].
     */
    fun getWeatherForecast(temperatureType: TemperatureType, chosenCity: String) {
        var cityModel: CityLocationModel? = null
        if (isNetworkAvailable(app.applicationContext)) {
            if (chosenCity.isNotBlank()) {
                Log.d("WeatherForecastViewModel", "Chosen city is $chosenCity")
                this.chosenCity = chosenCity
                downloadWeatherForecastForCityOrGeoLocation(temperatureType, chosenCity)
            } else {
                Log.d("WeatherForecastViewModel", "Chosen city is empty")
                // Try loading a city model from DB
                viewModelScope.launch(exceptionHandler) {
                    cityModel = chosenCityInteractor.loadChosenCityModel()
                    Log.d(
                        "WeatherForecastViewModel",
                        "City loaded from database = ${cityModel?.city}, lat = ${cityModel?.location?.latitude}, lon = ${cityModel?.location?.longitude}"
                    )
                }
                // When there is no loaded city model from database,
                if (cityModel?.city.isNullOrBlank()) {
                    // Define local city and try downloading a forecast for it
                    _updateStatusLiveData.postValue(app.applicationContext.getString(R.string.location_defining_text))
                    // Following row defines a city and displays an alert
                    _defineCurrentGeoLocationLiveData.postValue(Unit)
                } else {
                    downloadWeatherForecastForCityOrGeoLocation(temperatureType, cityModel?.city ?: "")
                }
            }
        } else {
            // Try loading a city model from DB
            viewModelScope.launch(exceptionHandler) {
                cityModel = chosenCityInteractor.loadChosenCityModel()
                Log.d(
                    "WeatherForecastViewModel",
                    "loaded city = ${cityModel?.city}, lat = ${cityModel?.location?.latitude}, lon = ${cityModel?.location?.longitude}" //TODO Move to strings.xml
                )
            }
            // If it is null, show error
            if (cityModel?.city.isNullOrBlank()) {
                _showErrorLiveData.postValue(app.applicationContext.getString(R.string.network_not_available_error_text))
                // Else download a forecast from database
            } else {
                downloadForecastFromDatabase(cityModel?.city ?: "")
            }
        }
    }

    /**
     * Request geo location permission, or if its granted - locate a city.
     */
    override fun onNoGeoLocationPermission() {
        requestGeoLocationPermission()
    }

    fun requestGeoLocationPermission() {
        if (!hasPermissionForGeoLocation()) {
            _updateStatusLiveData.postValue(app.applicationContext.getString(R.string.geo_location__permission_required))
            _requestPermissionLiveData.postValue(Unit)
        } else {
            getWeatherForecast(TemperatureType.CELSIUS, chosenCity)    //TODO
        }
    }

    private fun hasPermissionForGeoLocation() =
        (ActivityCompat.checkSelfPermission(app.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED)

    private fun getWeatherForecast(temperatureType: TemperatureType, location: Location, city: String) {
        _showProgressBarLiveData.postValue(true)
        viewModelScope.launch(exceptionHandler) {
            var result = weatherForecastRemoteInteractor.loadRemoteForecastForLocation(
                temperatureType,
                location.latitude,
                location.longitude
            )
            // City in response is different than city in request
            result = result.copy(city = city)
            saveForecastToDatabase(result)
            showForecastDataOrProcessServerError(result)
        }
    }

    private fun downloadWeatherForecastForCityOrGeoLocation(temperatureType: TemperatureType, city: String) {
        _showProgressBarLiveData.postValue(true)
        try {
            viewModelScope.launch(exceptionHandler) {
                var result = weatherForecastRemoteInteractor.loadRemoteForecastForCity(
                    temperatureType,
                    city
                )
                // City in response is different than city in request
                result = result.copy(city = city)
                saveForecastToDatabase(result)
                showForecastDataOrProcessServerError(result)
            }
        } catch (ex: Exception) {
            Log.e(
                "WeatherForecastViewModel",
                app.applicationContext.getString(R.string.forecast_downloading_for_city_succeded)
            )
            Log.e("WeatherForecastViewModel", ex.stackTraceToString())
            _showErrorLiveData.postValue(ex.message)
            _defineGeoLocationByCityLiveData.postValue(city)    //In fact, defines location and loads forecast
        }
    }

    private suspend fun showForecastDataOrProcessServerError(result: WeatherForecastDomainModel) {
        if (result.serverError.isBlank()) {
            Log.d("WeatherForecastViewModel", result.toString())
            _showWeatherForecastLiveData.postValue(result)
            saveForecastToDatabase(result)
        } else {
            _showErrorLiveData.postValue(result.serverError)
            Log.e("WeatherForecastViewModel8", result.serverError)
        }
    }

    private suspend fun saveForecastToDatabase(result: WeatherForecastDomainModel) {
        Log.d("WeatherForecastViewModel", "Saving forecast to database $result")
        weatherForecastLocalInteractor.saveForecast(result)
    }

    private fun downloadForecastFromDatabase(city: String) {
        viewModelScope.launch(exceptionHandler) {
            val result = weatherForecastLocalInteractor.loadForecast(city)
            _showWeatherForecastLiveData.postValue(result)
            Log.d("WeatherForecastViewModel", result.toString())
            _showErrorLiveData.postValue(app.applicationContext.getString(R.string.database_forecast_downloading))
        }
    }

    /**
     * City locating successful callback, receiving [city] and its latitude, longitude as [location].
     */
    fun onDefineCityByGeoLocationSuccess(city: String, location: Location) {
        Log.d("WeatherForecastViewModel", "Location defined successfully")
        Log.d("WeatherForecastViewModel", "city = $city, location = $location")
        viewModelScope.launch(exceptionHandler) {
            chosenCityInteractor.saveChosenCity(city, location)
        }
    }

    /**
     * TODO
     */
    fun onDefineCityByCurrentGeoLocationSuccess(city: String, location: Location) {
        Log.d("WeatherForecastViewModel", "Location defined successfully")
        Log.d("WeatherForecastViewModel", "city = $city, location = $location")
        viewModelScope.launch(exceptionHandler) {
            chosenCityInteractor.saveChosenCity(city, location)
        }
        _showGeoLocationAlertDialogLiveData.postValue(
            CityLocationModel(city, location)
        )
    }

    /**
     * Save a [city] and its [location] and download its forecast.
     */
    fun onDefineGeoLocationByCitySuccess(city: String, location: Location) {
        Log.d("WeatherForecastViewModel", "City defined successfully")
        Log.d("WeatherForecastViewModel", "city = $city, location = $location")
        viewModelScope.launch(exceptionHandler) {
            chosenCityInteractor.saveChosenCity(city, location)
        }
        Log.d("WeatherForecastViewModel", "Let's download a forecast for them")
        getWeatherForecast(TemperatureType.CELSIUS, location, city)
    }

    fun onDefineGeoLocationByCityFail(errorMessage: String) {
        Log.e("WeatherForecastViewModel", errorMessage)
        _showErrorLiveData.postValue(errorMessage)
    }

    /**
     * Define a city by the succeeded geo location.
     */
    override fun onCurrentGeoLocationSuccess(location: Location) {
        _defineCityCurrentByGeoLocationLiveData.postValue(location)
    }

    /**
     * City locating failed callback. Informs user with an [errorMessage] message.
     */
    override fun onCurrentGeoLocationFail(errorMessage: String) {
        _showErrorLiveData.postValue(errorMessage)
    }

    /**
     * Update status message.
     */
    fun onUpdateStatus(statusMessage: String) {
        _updateStatusLiveData.postValue(statusMessage)
    }

    /**
     * Go to city selection screen.
     */
    fun onGotoCitySelection() {
        _gotoCitySelectionLiveData.call()
    }
}