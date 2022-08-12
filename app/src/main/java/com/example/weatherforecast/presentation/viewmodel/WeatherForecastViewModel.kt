package com.example.weatherforecast.presentation.viewmodel

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
import com.example.weatherforecast.data.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.network.NetworkUtils.isNetworkAvailable
import com.example.weatherforecast.presentation.fragments.CurrentTimeForecastFragment
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

    private val sharedPreferences = app.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
    private val _getWeatherForecastLiveData: MutableLiveData<WeatherForecastDomainModel> = MutableLiveData()
    private val _showErrorLiveData: MutableLiveData<String> = MutableLiveData()
    private val _showStatusLiveData: MutableLiveData<String> = MutableLiveData()
    private val _showProgressBarLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val _onSuccessLocationLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()     // TODO onSuccessLocationLiveData
    private val _requestPermissionLiveData: MutableLiveData<Unit> = MutableLiveData()
    private val _locateCityLiveData: MutableLiveData<Unit> = MutableLiveData()

    val getWeatherForecastLiveData: LiveData<WeatherForecastDomainModel>
        get() = _getWeatherForecastLiveData

    val showErrorLiveData: LiveData<String>
        get() = _showErrorLiveData

    val showStatusLiveData: LiveData<String>
        get() = _showStatusLiveData

    val showProgressBarLiveData: LiveData<Boolean>
        get() = _showProgressBarLiveData

    val showAlertDialogLiveData: LiveData<Unit>
        get() = _onSuccessLocationLiveData

    val requestPermissionLiveData: LiveData<Unit>
        get() = _requestPermissionLiveData

    val locateCityLiveData: LiveData<Unit>
        get() = _locateCityLiveData

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("WeatherForecastViewModel", throwable.message!!)
        _showErrorLiveData.postValue(throwable.message!!)
    }

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
        }
    }

    private suspend fun saveForecastToDatabase(result: WeatherForecastDomainModel) {
        // Saving it to database
        weatherForecastLocalInteractor.saveForecast(result)
    }

    fun requestPermissionOrDownloadForecast() {
        if (isNetworkAvailable(app.applicationContext)) {
            requestLocationPermissionOrLocateCity()
        } else {
            val city = sharedPreferences.getString(CurrentTimeForecastFragment.CITY_ARGUMENT_KEY, "")
            downloadWeatherForecastFromDatabase(city)
        }
    }

    private fun requestLocationPermissionOrLocateCity() {
        if (ActivityCompat.checkSelfPermission(app.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            _requestPermissionLiveData.postValue(Unit)
        } else {
            locateCityOrDownloadForecastData()
        }
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

    fun locateCityOrDownloadForecastData() {
        val city = sharedPreferences.getString(CurrentTimeForecastFragment.CITY_ARGUMENT_KEY, "")
        Log.d("WeatherForecastViewModel2", "city = $city")
        if (city.isNullOrBlank()) {
            _showStatusLiveData.postValue(app.applicationContext.getString(R.string.location_defining_text))
            _locateCityLiveData.postValue(Unit)
        } else {
            _showStatusLiveData.postValue(app.applicationContext.getString(R.string.network_forecast_downloading_text))
            downloadWeatherForecast(
                TemperatureType.CELSIUS,
                city,
                Location("")  // localLocation   // FIXME
            )
        }
    }

    fun onGeoLocationSuccess() {
        _onSuccessLocationLiveData.postValue(Unit)
    }

    companion object {
        private const val SHARED_PREFERENCES_KEY = "Shared preferences key"
    }
}