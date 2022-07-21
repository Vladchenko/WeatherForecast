package com.example.weatherforecast.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.data.models.WeatherForecastDomainModel
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.domain.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.WeatherForecastRemoteInteractor
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

/**
 * View model, as MVVM component
 *
 * @property app custom [Application] implementation for Hilt
 * @property weatherForecastRemoteInteractor provides domain layer data
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

    fun getWeatherForecast(temperatureType: TemperatureType, city: String) {
        try {
            if (isNetworkAvailable(app)) {
                viewModelScope.launch(exceptionHandler) {
                    // Downloading a weather forecast from network
                    val result = weatherForecastRemoteInteractor.loadForecast(temperatureType, city)
                    // Saving it to database
                    weatherForecastLocalInteractor.saveForecast(result)
                    _getWeatherForecastLiveData.postValue(result)
                }
            } else {
                // When network is not available,
                viewModelScope.launch(exceptionHandler) {
                    // Download forecast from database
                    val result = weatherForecastLocalInteractor.loadForecast(city)
                    _getWeatherForecastLiveData.postValue(result)
                    _showErrorLiveData.postValue("No internet connection, outdated forecast has been loaded from database")
                }
            }
        } catch (ex: Exception) {
            _showErrorLiveData.postValue(ex.message)
        }
    }

    private fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    return true
                }
            }
        }
        return false
    }
}