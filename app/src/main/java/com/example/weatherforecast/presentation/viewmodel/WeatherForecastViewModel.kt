package com.example.weatherforecast.presentation.viewmodel

import android.app.Application
import android.location.Location
import android.util.Log
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
    private val _showProgressBarLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val _showAlertDialogLiveData: MutableLiveData<Unit> = MutableLiveData()     // onSuccessLocationLiveData
    private val _networkAvailableLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val isNetworkAvailableLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val getWeatherForecastLiveData: LiveData<WeatherForecastDomainModel>
        get() = _getWeatherForecastLiveData

    val showErrorLiveData: LiveData<String>
        get() = _showErrorLiveData

    val showProgressBarLiveData: LiveData<Boolean>
        get() = _showProgressBarLiveData

    val showAlertDialogLiveData: LiveData<Unit>
        get() = _showAlertDialogLiveData

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
            if (isNetworkAvailable(app)) {
                _showProgressBarLiveData.postValue(true)
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
                    if (result.serverError.isBlank()) {
                        // Saving it to database
                        weatherForecastLocalInteractor.saveForecast(result)
                        _getWeatherForecastLiveData.postValue(result)
                    } else {
                        _showErrorLiveData.postValue(result.serverError)
                    }
                }
            } else {
                // When network is not available,
                viewModelScope.launch(exceptionHandler) {
                    // Download forecast from database
                    val result = weatherForecastLocalInteractor.loadForecast(city ?: "")
                    _getWeatherForecastLiveData.postValue(result)
                    Log.d("WeatherForecastViewModel", result.toString())
                    _showErrorLiveData.postValue(app.applicationContext.getString(R.string.database_downloading))
                }
            }
        } catch (ex: Exception) {
            _showErrorLiveData.postValue(ex.message)
        }
    }

    fun notifyAboutNetworkAvailability(callback: suspend () -> Unit) {
        if (isNetworkAvailableLiveData.value == true) {
            viewModelScope.launch {
                callback.invoke()
            }
            _networkAvailableLiveData.postValue(true)
        } else {
            _showErrorLiveData.value = ("Network not available")
        }
    }

    fun showAlertDialog() {
        _showAlertDialogLiveData.postValue(Unit)
    }
}