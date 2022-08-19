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
    private val _showStatusLiveData: MutableLiveData<String> = MutableLiveData()
    private val _showProgressBarLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val _defineCityByGeoLocationLiveData: SingleLiveEvent<Location> = SingleLiveEvent()
    private val _showGeoLocationAlertDialogLiveData: SingleLiveEvent<CityLocationModel> = SingleLiveEvent()
    private val _requestPermissionLiveData: MutableLiveData<Unit> = MutableLiveData()
    private val _defineGeoLocationByCityLiveData: MutableLiveData<String> = MutableLiveData()
    private val _defineCurrentGeoLocationLiveData: MutableLiveData<String> = MutableLiveData()
    private val _gotoCitySelectionLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _chooseAnotherCityLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    //endregion livedata fields

    //region livedata getters fields
    val getWeatherForecastLiveData: LiveData<WeatherForecastDomainModel>
        get() = _showWeatherForecastLiveData

    val showErrorLiveData: LiveData<String>
        get() = _showErrorLiveData

    val showStatusLiveData: LiveData<String>
        get() = _showStatusLiveData

    val showProgressBarLiveData: LiveData<Boolean>
        get() = _showProgressBarLiveData

    val defineCityByGeoLocationLiveData: LiveData<Location>
        get() = _defineCityByGeoLocationLiveData

    val showGeoLocationAlertDialogLiveData: LiveData<CityLocationModel>
        get() = _showGeoLocationAlertDialogLiveData

    val requestPermissionLiveData: LiveData<Unit>
        get() = _requestPermissionLiveData

    val defineGeoLocationLiveData: LiveData<String>
        get() = _defineGeoLocationByCityLiveData

    val defineCurrentGeoLocationLiveData: LiveData<String>
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
     * Download weather forecast on a [chosenCity].
     */
    fun downloadWeatherForecast(chosenCity: String, temperatureType: TemperatureType) {
        var cityModel: CityLocationModel? = null
        if (isNetworkAvailable(app.applicationContext)) {
            if (chosenCity.isNotBlank()) {
                this.chosenCity = chosenCity
                _defineGeoLocationByCityLiveData.postValue(chosenCity)
            } else {
                viewModelScope.launch(exceptionHandler) {
                    cityModel = chosenCityInteractor.loadChosenCityModel()
                    Log.d("WeatherForecastViewModel", "loaded city = ${cityModel?.city}, lat = ${cityModel?.location?.latitude}, lon = ${cityModel?.location?.longitude}")
                }
                if (cityModel?.city.isNullOrBlank()) {
                    onNoLocationPermission()
                } else {
                    downloadWeatherForecast(
                        temperatureType,
                        cityModel?.location ?: Location("")
                    )
                }
            }
        } else {
            viewModelScope.launch(exceptionHandler) {
                cityModel = chosenCityInteractor.loadChosenCityModel()
                Log.d("WeatherForecastViewModel", "loaded city = ${cityModel?.city}, lat = ${cityModel?.location?.latitude}, lon = ${cityModel?.location?.longitude}")
            }
            if (cityModel?.city.isNullOrBlank()) {
                _showErrorLiveData.postValue(app.applicationContext.getString(R.string.network_not_available_error_text))
            } else {
                downloadWeatherForecastFromDatabase(cityModel?.city ?: "")
            }
        }
    }

    /**
     * Request geo location permission, or if its granted - locate a city.
     */
    override fun onNoLocationPermission() {
        if (!hasPermissionForGeoLocation()) {
            _showStatusLiveData.postValue(app.applicationContext.getString(R.string.geo_location__permission_required))
            _requestPermissionLiveData.postValue(Unit)
        } else {
            _showStatusLiveData.postValue(app.applicationContext.getString(R.string.location_defining_text))
            _defineCurrentGeoLocationLiveData.postValue(chosenCity)    //TODO
        }
    }

    private fun hasPermissionForGeoLocation() =
        (ActivityCompat.checkSelfPermission(app.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED)

    /**
     * Retrieve and save to database if retrieval is successful of city weather forecast, using [temperatureType]
     * and [location], otherwise show error.
     */
    fun downloadWeatherForecast(temperatureType: TemperatureType, location: Location) {
        try {
            _showProgressBarLiveData.postValue(true)
            viewModelScope.launch(exceptionHandler) {
                showForecastDataOrProcessServerError(
                    weatherForecastRemoteInteractor.loadRemoteForecastForLocation(
                        temperatureType,
                        location.latitude,
                        location.longitude
                    )
                )
            }
        } catch (ex: Exception) {
            Log.e("WeatherForecastViewModel", ex.stackTraceToString())
            _showErrorLiveData.postValue(ex.message)
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
        weatherForecastLocalInteractor.saveForecast(result)
    }

    private fun downloadWeatherForecastFromDatabase(city: String) {
        viewModelScope.launch(exceptionHandler) {
            val result = weatherForecastLocalInteractor.loadForecast(city)
            _showWeatherForecastLiveData.postValue(result)
            Log.d("WeatherForecastViewModel", result.toString())
            _showErrorLiveData.postValue(app.applicationContext.getString(R.string.database_forecast_downloading))
        }
    }

    /**
     * City locating successful callback, receiving city name as [locality] and its latitude, longitude in [location].
     */
    fun onDefineCityByGeoLocationSuccess(locality: String, location: Location) {
        Log.d("WeatherForecastViewModel","Location defined successfully")
        Log.d("WeatherForecastViewModel", "city = $locality, location = $location")
        viewModelScope.launch(exceptionHandler) {
            chosenCityInteractor.saveChosenCity(locality, location)
        }
        _showGeoLocationAlertDialogLiveData.postValue(
            CityLocationModel(locality, location)
        )
    }

    /**
     * TODO
     */
    fun onDefineLocationByCitySuccess(locality: String, location: Location) {
        Log.d("WeatherForecastViewModel","City defined successfully")
        Log.d("WeatherForecastViewModel", "city = $locality, location = $location")
        viewModelScope.launch(exceptionHandler) {
            chosenCityInteractor.saveChosenCity(locality, location)
        }
        downloadWeatherForecast(TemperatureType.CELSIUS, location)
    }

    /**
     * Define a city be the succeeded geo location.
     */
    override fun onGeoLocationSuccess(location: Location, chosenCity: String) {
        if (chosenCity.isNotBlank()) {
            viewModelScope.launch(exceptionHandler) {
                chosenCityInteractor.saveChosenCity(chosenCity, location)
            }
            downloadWeatherForecast(TemperatureType.CELSIUS, location)
        } else {
            _defineCityByGeoLocationLiveData.postValue(location)
        }
    }

    /**
     * City locating failed callback. Informs user with a [errorMessage] message.
     */
    override fun onGeoLocationFail(errorMessage: String) {
        _showErrorLiveData.postValue(errorMessage)
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