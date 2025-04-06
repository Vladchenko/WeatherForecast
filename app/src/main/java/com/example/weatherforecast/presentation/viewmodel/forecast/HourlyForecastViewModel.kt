package com.example.weatherforecast.presentation.viewmodel.forecast

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.api.customexceptions.CityNotFoundException
import com.example.weatherforecast.data.api.customexceptions.NetworkTimeoutException
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.HourlyForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.HourlyForecastRemoteInteractor
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.domain.HourlyForecastDomainModel
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HourlyForecastViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    private val temperatureType: TemperatureType,
    coroutineDispatchers: CoroutineDispatchers,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val forecastLocalInteractor: HourlyForecastLocalInteractor,
    private val forecastRemoteInteractor: HourlyForecastRemoteInteractor,
) : AbstractViewModel(connectivityObserver, coroutineDispatchers) {

    val hourlyForecastState: MutableState<HourlyForecastDomainModel?> = mutableStateOf(null)

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, throwable.message.orEmpty())
        when (throwable) {
            is CityNotFoundException -> {
                showError(throwable.message)
            }
            is NoInternetException -> {
                showError(throwable.message.toString())
            }
            is NetworkTimeoutException -> {
                showError(throwable.message.toString())
            }
            else -> {
                Log.e(TAG, throwable.message.orEmpty())
                Log.e(TAG, throwable.stackTraceToString())
                showError(throwable.message.toString())
            }
        }
        showProgressBarState.value = false
        throwable.stackTrace.forEach {
            Log.e(TAG, it.toString())
        }
    }

    /**
     * Download hourly weather forecast on a [city].
     */
    fun loadHourlyForecastForCity(city: String) {
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            val result = forecastRemoteInteractor.loadHourlyForecastForCity(
                temperatureType,
                city
            )
            processServerResponse(result)
        }
    }

    /**
     * Download hourly weather forecast on a [cityModel].
     */
    fun loadHourlyForecastForLocation(cityModel: CityLocationModel) {
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            val result = forecastRemoteInteractor.loadHourlyForecastForLocation(
                temperatureType,
                cityModel.location.latitude,
                cityModel.location.longitude
            )
            processServerResponse(result)
        }
    }

    private fun processServerResponse(result: LoadResult<HourlyForecastDomainModel>) {
        showProgressBarState.value = false
        when (result) {
            is LoadResult.Remote -> {
                hourlyForecastState.value = result.data
                showStatus(
                    R.string.forecast_for_city,
                    result.data.city
                )
            }
            is LoadResult.Local -> {
                // Not used for hourly forecast
            }
            is LoadResult.Fail -> {
                showError(result.exception)
            }
        }
    }

    companion object {
        private const val TAG = "HourlyForecastViewModel"
    }
} 