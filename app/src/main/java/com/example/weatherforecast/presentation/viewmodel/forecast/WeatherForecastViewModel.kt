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
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.models.domain.CityLocationModel
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
    private val forecastRemoteInteractor: WeatherForecastRemoteInteractor
) : AbstractViewModel(coroutineDispatchers) {

    val dataModelState: MutableState<WeatherForecastDomainModel?> = mutableStateOf(null)

    //region livedata getters fields
    val onChosenCityBlankLiveData: LiveData<Unit>
        get() = _onChosenCityBlankLiveData

    val onChosenCityNotFoundLiveData: LiveData<String>
        get() = _onChosenCityNotFoundLiveData

    val onCityRequestFailedLiveData: LiveData<String>
        get() = _onCityRequestFailedLiveData

    val onGotoCitySelectionLiveData: LiveData<Unit>
        get() = _onGotoCitySelectionLiveData

    val onSaveForecastLiveData: LiveData<WeatherForecastDomainModel>
        get() = _onSaveForecastLiveData

    val onLoadLocalForecastLiveData: LiveData<WeatherForecastDomainModel>
        get() = _onLoadLocalForecastLiveData

    val onRemoteForecastFailLiveData: LiveData<String>
        get() = _onRemoteForecastFailLiveData
    //endregion livedata getters fields

    //region livedata fields
    private val _onChosenCityBlankLiveData = SingleLiveEvent<Unit>()
    private val _onChosenCityNotFoundLiveData = SingleLiveEvent<String>()
    private val _onCityRequestFailedLiveData = SingleLiveEvent<String>()
    private val _onGotoCitySelectionLiveData = SingleLiveEvent<Unit>()
    private val _onSaveForecastLiveData = SingleLiveEvent<WeatherForecastDomainModel>()
    private val _onLoadLocalForecastLiveData = SingleLiveEvent<WeatherForecastDomainModel>()
    private val _onRemoteForecastFailLiveData = SingleLiveEvent<String>()
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
                _onRemoteForecastFailLiveData.postValue(chosenCity.orEmpty())
            }

            is NoSuchDatabaseEntryException -> {
                showError(
                    R.string.database_forecast_for_city_not_found,
                    throwable.message.orEmpty()
                )
            }

            else -> {
                Log.e(TAG, throwable.message.orEmpty())
                Log.e(TAG, throwable.stackTraceToString())
                showError(throwable.message.toString())
                //In fact, defines location and loads forecast for it
                _onCityRequestFailedLiveData.postValue(chosenCity.orEmpty())    //TODO .orEmpty()
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
            _onChosenCityBlankLiveData.postValue(null)
        } else {
            chosenCity = city
            downloadWeatherForecastForCity(city)
        }
    }

    /**
     * Download weather forecast on a [city].
     */
    fun downloadWeatherForecastForCity(city: String) {
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            val result = forecastRemoteInteractor.loadForecastForCity(
                temperatureType,
                city
            )
            processServerResponse(result)
        }
    }

    fun downloadWeatherForecastForLocation(cityModel: CityLocationModel) {
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            var result = forecastRemoteInteractor.loadForecastForLocation(
                temperatureType,
                cityModel.location.latitude,
                cityModel.location.longitude
            )
            // City in response is different than city in request
            result = Result.success(result.getOrNull()!!.copy(city = cityModel.city))
            processServerResponse(result)
        }
    }

    private fun processServerResponse(result: Result<WeatherForecastDomainModel>) {
        result.getOrNull()?.let { forecastModel -> // Result is not null
            if (result.isSuccess) {
                Log.d(TAG, result.toString())
                showProgressBarState.value = false
                if (forecastModel.serverError.isBlank()) {
                    showRemoteForecastAndSave(forecastModel)
                } else {
                    showLocalForecastOrError(forecastModel)
                }
            } else {
                showError(forecastModel.serverError)
                Log.e(TAG, forecastModel.serverError)
                _onCityRequestFailedLiveData.postValue(forecastModel.city)
            }
        } ?: run {
            processResponseError("Server responded with $this")
        }
    }

    private fun showRemoteForecastAndSave(forecastModel: WeatherForecastDomainModel) {
        dataModelState.value = forecastModel
        showStatus(
            R.string.forecast_for_city,
            dataModelState.value?.city.orEmpty()
        )
        _onSaveForecastLiveData.postValue(forecastModel)
    }

    private fun showLocalForecastOrError(forecastModel: WeatherForecastDomainModel) {
        if (forecastModel.city.isBlank()) {
            processResponseError(forecastModel.serverError)
        } else { // When server error but city is NOT blank - local forecast loaded
            dataModelState.value = forecastModel
            showError(
                R.string.forecast_for_city_outdated,
                dataModelState.value?.city.orEmpty()
            )
        }
    }

    private fun processResponseError(error: String) {
        showError(error)
        _onLoadLocalForecastLiveData.call()
    }

    fun getLocalForecast(forecastModel: WeatherForecastDomainModel) {
        dataModelState.value = forecastModel
    }

    companion object {
        private const val TAG = "WeatherForecastViewModel"
    }
}