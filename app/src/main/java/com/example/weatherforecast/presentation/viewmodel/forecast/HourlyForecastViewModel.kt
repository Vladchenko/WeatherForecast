package com.example.weatherforecast.presentation.viewmodel.forecast

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.api.customexceptions.CityNotFoundException
import com.example.weatherforecast.data.api.customexceptions.NetworkTimeoutException
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.HourlyForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.HourlyForecastRemoteInteractor
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.domain.HourlyForecastDomainModel
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.presentation.viewmodel.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Viewmodel for hourly weather forecast.
 *
 * @param connectivityObserver observes internet connectivity state.
 * @param coroutineDispatchers dispatchers coroutines.
 * @property temperatureType current temperature type.
 * @property chosenCityInteractor interactor to get chosen city.
 * @property forecastLocalInteractor interactor to get saved hourly forecast.
 * @property forecastRemoteInteractor interactor to get remote hourly forecast.
 * @constructor
 */
@HiltViewModel
class HourlyForecastViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    coroutineDispatchers: CoroutineDispatchers,
    private val temperatureType: TemperatureType,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val forecastLocalInteractor: HourlyForecastLocalInteractor,
    private val forecastRemoteInteractor: HourlyForecastRemoteInteractor,
) : AbstractViewModel(connectivityObserver, coroutineDispatchers) {

    val hourlyForecastState: MutableState<HourlyForecastDomainModel?> = mutableStateOf(null)
    val onRemoteCityRequestFailedLiveData: LiveData<String>
        get() = _onRemoteCityRequestFailedLiveData
    private val _onRemoteCityRequestFailedLiveData = SingleLiveEvent<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, throwable.message.orEmpty())
        when (throwable) {
            is CityNotFoundException -> {
                showError(throwable.message)
                _onRemoteCityRequestFailedLiveData.postValue(throwable.city)
            }
            is NoInternetException, is NetworkTimeoutException -> {
                showError(throwable.message.toString())
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
            if (cityModel.city.isBlank()) {
                showError("Local city not found")
            } else {
                forecastLocalInteractor.loadForecast(cityModel.city, "")
            }
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