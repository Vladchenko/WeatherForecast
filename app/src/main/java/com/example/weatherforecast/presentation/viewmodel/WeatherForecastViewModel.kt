package com.example.weatherforecast.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.data.util.Resource
import com.example.weatherforecast.data.models.WeatherForecastResponse
import com.example.weatherforecast.domain.WeatherForecastInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * View model, as MVVM component
 *
 * @property app custom [Application] implementation for Hilt
 * @property weatherForecastInteractor provides domain layer data
 */
class WeatherForecastViewModel(
    private val app: Application,
    private val weatherForecastInteractor: WeatherForecastInteractor
) : AndroidViewModel(app) {

    private val _getWeatherForecastLiveData: MutableLiveData<Resource<WeatherForecastResponse>> = MutableLiveData()

    val getWeatherForecastLiveData: LiveData<Resource<WeatherForecastResponse>>
        get() = _getWeatherForecastLiveData

    fun getWeatherForecast(city: String) {
        _getWeatherForecastLiveData.postValue(Resource.Loading)
        try {
            if (isNetworkAvailable(app)) {
                viewModelScope.launch(Dispatchers.IO) {
                    val result = weatherForecastInteractor.execute(city)
                    _getWeatherForecastLiveData.postValue(result)
                }
            } else {
                _getWeatherForecastLiveData.postValue(Resource.Error(Exception("Internet is not available")))
            }
        } catch (ex: Exception) {
            _getWeatherForecastLiveData.postValue(Resource.Error(ex))
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