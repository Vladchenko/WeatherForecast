package com.example.weatherforecast.presentation.viewmodel.forecast

import android.Manifest
import android.app.Application
import android.content.Context
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
import com.example.weatherforecast.data.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.network.NetworkUtils.isNetworkAvailable
import com.example.weatherforecast.presentation.PresentationUtils.SHARED_PREFERENCES_KEY
import com.example.weatherforecast.presentation.fragments.forecast.CurrentTimeForecastFragment
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
    private val weatherForecastRemoteInteractor: WeatherForecastRemoteInteractor,
    private val weatherForecastLocalInteractor: WeatherForecastLocalInteractor
) : AndroidViewModel(app) {

    //region livedata fields
    private val sharedPreferences = app.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
    private val _getWeatherForecastLiveData: MutableLiveData<WeatherForecastDomainModel> = MutableLiveData()
    private val _showErrorLiveData: MutableLiveData<String> = MutableLiveData()
    private val _showStatusLiveData: MutableLiveData<String> = MutableLiveData()
    private val _showProgressBarLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val _defineCityByGeoLocationLiveData: SingleLiveEvent<Location> = SingleLiveEvent()
    private val _onGeoLocationSuccessLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _requestPermissionLiveData: MutableLiveData<Unit> = MutableLiveData()
    private val _locateCityLiveData: MutableLiveData<Unit> = MutableLiveData()
    private val _gotoCitySelectionLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _chooseAnotherCity: SingleLiveEvent<String> = SingleLiveEvent()
    //endregion livedata fields

    //region livedata getters fields
    val getWeatherForecastLiveData: LiveData<WeatherForecastDomainModel>
        get() = _getWeatherForecastLiveData

    val showErrorLiveData: LiveData<String>
        get() = _showErrorLiveData

    val showStatusLiveData: LiveData<String>
        get() = _showStatusLiveData

    val showProgressBarLiveData: LiveData<Boolean>
        get() = _showProgressBarLiveData

    val defineCityByGeoLocationLiveData: LiveData<Location>
        get() = _defineCityByGeoLocationLiveData

    val locationSuccessLiveData: LiveData<Unit>
        get() = _onGeoLocationSuccessLiveData

    val requestPermissionLiveData: LiveData<Unit>
        get() = _requestPermissionLiveData

    val locateCityLiveData: LiveData<Unit>
        get() = _locateCityLiveData

    val gotoCitySelectionLiveData: LiveData<Unit>
        get() = _gotoCitySelectionLiveData

    val chooseAnotherCity: LiveData<String>
        get() = _chooseAnotherCity
    //endregion livedata getters fields

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("WeatherForecastViewModel", throwable.message!!)
        if (throwable is CityNotFoundException) {
            //TODO Provide city name
            onUpdateStatus(throwable.message)
            _chooseAnotherCity.postValue(throwable.city)
        }
        throwable.stackTrace.forEach {
            Log.e("WeatherForecastViewModel", it.toString()) }
        _showErrorLiveData.postValue(throwable.message!!)
    }

    /**
     * Download weather forecast on a [city].
     */
    fun downloadWeatherForecast(city: String) {
        var chosenCity = city
        // If city was not chosen on a "city choosing screen",
        if (chosenCity.isBlank()) {
            // then try loading it from a saved one.
            chosenCity = downloadChosenCity()?:""
        }

        // If there is a chosen or a saved city,
        if (chosenCity.isNotBlank()) {
            Log.d("WeatherForecastViewModel", "city = $chosenCity")
            // persist it
            persistChosenCity(chosenCity)
            // and download its forecast
            downloadWeatherForecast(
                TemperatureType.CELSIUS,
                chosenCity,
                Location("")  // localLocation   // TODO
            )
        } else {
            // else the apps runs for the first time and it has to pass through all the steps -
            requestLocationPermissionOrLocateCity()
        }
    }

    /**
     * Request geo location permission, or if its granted - locate a city.
     */
    fun requestLocationPermissionOrLocateCity() {
        if (!hasPermissionForGeoLocation()) {
            _requestPermissionLiveData.postValue(Unit)
        } else {
            locateCityOrDownloadForecastData()
        }
    }

    private fun hasPermissionForGeoLocation() =
        (ActivityCompat.checkSelfPermission(app.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED)

    /**
     * Locating city or, if its already located, download its forecast
     */
    fun locateCityOrDownloadForecastData() {
        val city = downloadChosenCity()
        Log.d("WeatherForecastViewModel2", "city = $city")
        if (city.isNullOrBlank()) {
            locateCityOrDownloadLocalForecast()
        } else {
            _showStatusLiveData.postValue(app.applicationContext.getString(R.string.network_forecast_downloading_text))
            downloadWeatherForecast(
                TemperatureType.CELSIUS,
                city,
                Location("")  // localLocation   // TODO
            )
        }
    }

    private fun locateCityOrDownloadLocalForecast() {
        if (isNetworkAvailable(app.applicationContext)) {
            _showStatusLiveData.postValue(app.applicationContext.getString(R.string.location_defining_text))
            _locateCityLiveData.postValue(Unit)
        } else {
            downloadWeatherForecastFromDatabase(downloadChosenCity())
        }
    }

    /**
     * TODO
     */
    fun persistChosenCity(chosenCity: String) {
        sharedPreferences.edit().putString(CurrentTimeForecastFragment.CITY_ARGUMENT_KEY, chosenCity).apply()
    }

    fun downloadChosenCity() = sharedPreferences.getString(CurrentTimeForecastFragment.CITY_ARGUMENT_KEY, "")

    /**
     * Retrieve and save to database if retrieval is successful of city weather forecast, using [temperatureType],
     * [city] and [location], otherwise try to download it from database.
     */
    fun downloadWeatherForecast(temperatureType: TemperatureType, city: String?, location: Location?) {
        try {
            _showProgressBarLiveData.postValue(true)
            viewModelScope.launch(exceptionHandler) {
                postForecastDataOrProcessServerError(
                    downloadForecastForCityOrLocation(temperatureType, city, location)
                )
            }
        } catch (ex: Exception) {
            Log.e("WeatherForecastViewModel", ex.stackTraceToString())
            _showErrorLiveData.postValue(ex.message)
        }
    }

    private suspend fun downloadForecastForCityOrLocation(
        temperatureType: TemperatureType,
        city: String?,
        location: Location?
    ): WeatherForecastDomainModel {
        return if (!city.isNullOrBlank()) {
            weatherForecastRemoteInteractor.loadForecastForCity(temperatureType, city)
        } else {
            weatherForecastRemoteInteractor.loadRemoteForecastForLocation(
                temperatureType,
                location?.latitude ?: 0.0,
                location?.longitude ?: 0.0
            )
        }
    }

    private suspend fun postForecastDataOrProcessServerError(result: WeatherForecastDomainModel) {
        if (result.serverError.isBlank()) {
            _getWeatherForecastLiveData.postValue(result)
            saveForecastToDatabase(result)
        } else {
            _showErrorLiveData.postValue(result.serverError)
            Log.e("WeatherForecastViewModel8", result.serverError)
        }
    }

    private suspend fun saveForecastToDatabase(result: WeatherForecastDomainModel) {
        weatherForecastLocalInteractor.saveForecast(result)
    }

    private fun downloadWeatherForecastFromDatabase(city: String?) {
        viewModelScope.launch(exceptionHandler) {
            if (!city.isNullOrBlank()) {
                val result = weatherForecastLocalInteractor.loadForecast(city)
                _getWeatherForecastLiveData.postValue(result)
                Log.d("WeatherForecastViewModel", result.toString())
                _showErrorLiveData.postValue(app.applicationContext.getString(R.string.database_forecast_downloading))
            } else {
                _showErrorLiveData.postValue(app.applicationContext.getString(R.string.default_city_absent))
            }
        }
    }

    /**
     * City locating successful callback
     */
    fun onGeoLocationSuccess(locality: String) {
        Log.d("WeatherForecastViewModel", "onGeoLocationSuccess")
        persistChosenCity(locality)
        _onGeoLocationSuccessLiveData.postValue(Unit)
    }

    /**
     * City locating failed callback. Informs user with a [error] message.
     */
    fun onGeoLocationFail(error: String) {
        _showErrorLiveData.postValue(error)
    }

    /**
     * Defining city name by geo location (latitude and longitude)
     */
    fun onDefineCityByGeoLocation(location: Location) {
        _defineCityByGeoLocationLiveData.postValue(location)
    }

    /**
     * Show error to user.
     */
    fun onShowError(error: String) {
        _showErrorLiveData.postValue(error)
    }

    fun onUpdateStatus(statusMessage: String) {
        _showStatusLiveData.postValue(statusMessage)
    }

    fun onGotoCitySelection() {
        _gotoCitySelectionLiveData.call()
    }
}