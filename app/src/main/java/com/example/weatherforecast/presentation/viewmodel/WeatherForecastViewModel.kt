package com.example.weatherforecast.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.data.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.presentation.viewmodel.NetworkUtils.isNetworkAvailable
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

    private val _getWeatherForecastLiveData: MutableLiveData<WeatherForecastDomainModel> = MutableLiveData()
    private val _showErrorLiveData: MutableLiveData<String> = MutableLiveData()

    val getWeatherForecastLiveData: LiveData<WeatherForecastDomainModel>
        get() = _getWeatherForecastLiveData

    val showErrorLiveData: LiveData<String>
        get() = _showErrorLiveData

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("WeatherForecastViewModel", throwable.message!!)
        _showErrorLiveData.postValue(throwable.message!!)
    }

    /**
     * Retrieve and save to database if retrieval is successful of city weather forecast, using [temperatureType],
     * [city] and [location], otherwise try to download it from database.
     */
    fun getWeatherForecast(temperatureType: TemperatureType, city: String?, location: Location?) {
        try {
            if (isNetworkAvailable(app)) {
                viewModelScope.launch(exceptionHandler) {
                    // Downloading a weather forecast from network
                    val result: WeatherForecastDomainModel =
                        if (!city.isNullOrBlank()) {
                            weatherForecastRemoteInteractor.loadForecastForCity(temperatureType, city)
                        } else {
                            weatherForecastRemoteInteractor.loadRemoteForecastForLocation(
                                temperatureType,
                                location?.latitude ?: 0.0,
                                location?.longitude ?: 0.0
                            )
                        }
                    // Saving it to database
                    weatherForecastLocalInteractor.saveForecast(result)
                    _getWeatherForecastLiveData.postValue(result)
                }
            } else {
                // When network is not available,
                viewModelScope.launch(exceptionHandler) {
                    // Download forecast from database
                    val result = weatherForecastLocalInteractor.loadForecast(city ?: "")
                    _getWeatherForecastLiveData.postValue(result)
                    _showErrorLiveData.postValue("No internet connection, outdated forecast has been loaded from database")
                }
            }
        } catch (ex: Exception) {
            _showErrorLiveData.postValue(ex.message)
        }
    }
}