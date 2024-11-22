package com.example.weatherforecast.presentation.viewmodel.forecast

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.data.api.customexceptions.CityNotFoundException
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.presentation.viewmodel.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for weather forecast downloading.
 *
 * @property temperatureType type of temperature
 * @property coroutineDispatchers for coroutines
 * @property chosenCityInteractor city chosen by user persistence interactor
 * @property forecastRemoteInteractor remote forecast data provider
 */
@HiltViewModel
class WeatherForecastViewModel @Inject constructor(
    private val temperatureType: TemperatureType,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val forecastLocalInteractor: WeatherForecastLocalInteractor,
    private val forecastRemoteInteractor: WeatherForecastRemoteInteractor
) : AbstractViewModel(coroutineDispatchers) {

    val dataModelState: MutableState<WeatherForecastDomainModel?> = mutableStateOf(null)

//    private val _chosenCityStateFlow = MutableStateFlow(Unit)
//    val chosenCityStateFlow:StateFlow<Unit> = _chosenCityStateFlow

    //region livedata getters fields
    val onChosenCityBlankLiveData: LiveData<Unit>
        get() = _onChosenCityBlankLiveData

    val onChosenCityNotFoundLiveData: LiveData<String>
        get() = _onChosenCityNotFoundLiveData

    val onCityRequestFailedLiveData: LiveData<String>
        get() = _onCityRequestFailedLiveData

    val onGotoCitySelectionLiveData: LiveData<Unit>
        get() = _onGotoCitySelectionLiveData
    //endregion livedata getters fields

    //region livedata fields
    private val _onChosenCityBlankLiveData = SingleLiveEvent<Unit>()
    private val _onChosenCityNotFoundLiveData = SingleLiveEvent<String>()
    private val _onCityRequestFailedLiveData = SingleLiveEvent<String>()
    private val _onGotoCitySelectionLiveData = SingleLiveEvent<Unit>()
    //endregion livedata fields

    private var chosenCity: String? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, throwable.message.orEmpty())
        when (throwable) {
            is CityNotFoundException -> {
                showError(throwable.message)
                _onCityRequestFailedLiveData.postValue(throwable.city)
            }

            is NoInternetException -> {
                showError(throwable.message.toString())
                downloadLocalForecastForCity(
                    chosenCity.orEmpty(),   // TODO Is it ok that orEmpty ?
                    throwable.message.toString()
                )
            }

            is NoSuchDatabaseEntryException -> {
                showError(
                    R.string.database_forecast_for_city_not_found,
                    throwable.message.orEmpty()
                )
            }

            is NumberFormatException -> {
                showError(throwable.message.orEmpty())
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
     * Go to city selection screen.
     */
    fun gotoCitySelection() {
        _onGotoCitySelectionLiveData.call()
    }

    /**
     * Launch weather forecast downloading, having a [chosenCity] provided from a city picker fragment.
     */
    fun launchWeatherForecast(chosenCity: String) =
        viewModelScope.launch(exceptionHandler) {
            // If chosenCity is blank, then return cityModel.city
            val city = chosenCity.ifBlank {
                val cityModel = chosenCityInteractor.loadChosenCityModel()
                Log.d(
                    TAG,
                    "No chosen city from picker fragment. Downloaded from database: city = ${cityModel.city}, " +
                            "lat = ${cityModel.location.latitude}, lon = ${cityModel.location.longitude}"
                )
                cityModel.city
            }
            downloadWeatherForecast(city)
        }

    /**
     * Download a forecast or call blank chosen city callback, depending on a presence of a [city]
     */
    private fun downloadWeatherForecast(city: String) {
        if (city.isBlank()) {
            _onChosenCityBlankLiveData.postValue(Unit)
//            _chosenCityStateFlow.value = Unit
        } else {
            chosenCity = city
            downloadRemoteForecastForCity(city)
        }
    }

    /**
     * Download weather forecast on a [city].
     */
    fun downloadRemoteForecastForCity(city: String) {
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            val result = forecastRemoteInteractor.loadForecastForCity(
                temperatureType,
                city
            )
            processServerResponse(result)
        }
    }

    /**
     * Download weather forecast on a [city], providing an [remoteError] on why remote request failed.
     */
    private fun downloadLocalForecastForCity(city: String, remoteError: String) {
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            val result = forecastLocalInteractor.loadForecast(
                city, remoteError
            )
            processServerResponse(result)
        }
    }

    fun downloadRemoteForecastForLocation(cityModel: CityLocationModel) {
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            val result = forecastRemoteInteractor.loadForecastForLocation(
                temperatureType,
                cityModel.location.latitude,
                cityModel.location.longitude
            )
            processServerResponseForLocation(cityModel.city, result)
        }
    }

    private fun processServerResponseForLocation(
        city: String,
        result: LoadResult<WeatherForecastDomainModel>
    ) {
        showProgressBarState.value = false
        when (result) {
            is LoadResult.Remote -> {
                // City in response is different than city in request
                showRemoteForecast(result.data.copy(city = city))
            }

            is LoadResult.Local -> {
                // City in response is different than city in request
                showLocalForecast(result.data.copy(city = city), result.remoteError)
            }

            is LoadResult.Fail -> {
                showError(result.exception)
            }
        }
    }

    private fun processServerResponse(result: LoadResult<WeatherForecastDomainModel>) {
        showProgressBarState.value = false
        when (result) {
            is LoadResult.Remote -> {
                showRemoteForecast(result.data)
            }

            is LoadResult.Local -> {
                showLocalForecast(result.data, result.remoteError)
            }

            is LoadResult.Fail -> {
                showError(result.exception)
            }
        }
    }

    private fun showRemoteForecast(forecastModel: WeatherForecastDomainModel) {
        dataModelState.value = forecastModel
        showStatus(
            R.string.forecast_for_city,
            dataModelState.value?.city.orEmpty()
        )
    }

    private fun showLocalForecast(forecastModel: WeatherForecastDomainModel, error: String) {
        dataModelState.value = forecastModel
        showWarning(
            R.string.forecast_for_city_outdated,
            dataModelState.value?.city.orEmpty()
        )
    }

    companion object {
        private const val TAG = "WeatherForecastViewModel"
    }
}