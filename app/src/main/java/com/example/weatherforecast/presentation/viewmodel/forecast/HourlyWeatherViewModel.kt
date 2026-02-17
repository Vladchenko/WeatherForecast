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
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.data.preferences.PreferencesManager
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.HourlyWeatherLocalInteractor
import com.example.weatherforecast.domain.forecast.HourlyWeatherRemoteInteractor
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.domain.HourlyWeatherDomainModel
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Viewmodel for hourly weather forecast.
 *
 * @constructor
 * @param connectivityObserver observes internet connectivity state.
 * @param coroutineDispatchers dispatchers coroutines.
 * @property chosenCityInteractor interactor to get chosen city.
 * @property forecastLocalInteractor interactor to get saved hourly forecast.
 * @property forecastRemoteInteractor interactor to get remote hourly forecast.
 */
@HiltViewModel
class HourlyWeatherViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    coroutineDispatchers: CoroutineDispatchers,
    private val preferencesManager: PreferencesManager,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val forecastLocalInteractor: HourlyWeatherLocalInteractor,
    private val forecastRemoteInteractor: HourlyWeatherRemoteInteractor,
) : AbstractViewModel(connectivityObserver, coroutineDispatchers) {

    val hourlyForecastState: MutableState<HourlyWeatherDomainModel?> = mutableStateOf(null)
    val remoteRequestFailedFlow: SharedFlow<String>
        get() = _remoteRequestFailedFlow
    private val _remoteRequestFailedFlow = MutableSharedFlow<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, throwable.message.orEmpty())
        when (throwable) {
            is CityNotFoundException -> {
                showError(throwable.message)
                _remoteRequestFailedFlow.tryEmit(throwable.city)
            }
            is NoInternetException, is NetworkTimeoutException -> {
                showError(throwable.message.toString())
                _remoteRequestFailedFlow.tryEmit(throwable.message.toString())
            }
            is NoSuchDatabaseEntryException -> {
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
    fun getHourlyForecastForCity(city: String) {
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            val temperatureType = preferencesManager.temperatureType.first()
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
    fun getHourlyForecastForLocation(cityModel: CityLocationModel) {
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            val temperatureType = preferencesManager.temperatureType.first()
            val result = forecastRemoteInteractor.loadHourlyForecastForLocation(
                temperatureType,
                cityModel.location.latitude,
                cityModel.location.longitude
            )
            processServerResponse(result)
        }
    }

    /**
     * Download saved(local) city
     */
    fun getLocalCity() {
        viewModelScope.launch(exceptionHandler) {
            val cityModel = chosenCityInteractor.loadChosenCity()
            val temperatureType = preferencesManager.temperatureType.first()
            if (cityModel.city.isBlank()) {
                showError(R.string.default_city_absent)
            } else {
                forecastLocalInteractor.loadForecast(cityModel.city, temperatureType, "")
            }
        }
    }

    private fun processServerResponse(result: LoadResult<HourlyWeatherDomainModel>) {
        showProgressBarState.value = false
        when (result) {
            is LoadResult.Remote -> {
                hourlyForecastState.value = result.data
                showMessage(
                    R.string.forecast_for_city_success,
                    result.data.city
                )
            }
            is LoadResult.Local -> {
                // Not used for hourly forecast
            }
            is LoadResult.Fail -> {
                showError(result.exception.toString())
            }
        }
    }

    companion object {
        private const val TAG = "HourlyForecastViewModel"
    }
} 