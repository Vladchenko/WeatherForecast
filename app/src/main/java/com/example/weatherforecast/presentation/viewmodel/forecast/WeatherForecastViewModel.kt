package com.example.weatherforecast.presentation.viewmodel.forecast

import android.app.Application
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
import com.example.weatherforecast.presentation.PresentationUtils.REPEAT_INTERVAL
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.presentation.viewmodel.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for weather forecast downloading.
 *
 * @param app custom [Application] implementation for Hilt
 * @param coroutineDispatchers geo location helper class
 * @param chosenCityInteractor city chosen by user persistence interactor
 * @param forecastRemoteInteractor remote forecast data provider
 */
@HiltViewModel
class WeatherForecastViewModel @Inject constructor(
    private val app: Application,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val forecastRemoteInteractor: WeatherForecastRemoteInteractor
) : AbstractViewModel(app, coroutineDispatchers) {

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
    //endregion livedata getters fields

    //region livedata fields
    private val _onChosenCityBlankLiveData = SingleLiveEvent<Unit>()
    private val _onChosenCityNotFoundLiveData = SingleLiveEvent<String>()
    private val _onCityRequestFailedLiveData = SingleLiveEvent<String>()
    private val _onGotoCitySelectionLiveData = SingleLiveEvent<Unit>()
    private val _onSaveForecastLiveData = SingleLiveEvent<WeatherForecastDomainModel>()
    private val _onLoadLocalForecastLiveData = SingleLiveEvent<WeatherForecastDomainModel>()
    //endregion livedata fields

    private var chosenCity: String? = null
    private var weatherForecastDownloadJob: Job
    private lateinit var temperatureType: TemperatureType

    init {
        showStatus(R.string.forecast_is_loading)
        weatherForecastDownloadJob =
            viewModelScope.launch(coroutineDispatchers.io, CoroutineStart.LAZY) {
                downloadWeatherForecast(chosenCity.orEmpty())
            }
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, throwable.message.orEmpty())
        when (throwable) {
            is CityNotFoundException -> {
                showError(throwable.message)
                _onCityRequestFailedLiveData.postValue(throwable.city)
            }

            is NoInternetException -> {
                showError(throwable.message.toString())
                viewModelScope.launch(coroutineDispatchers.io) {
                    delay(REPEAT_INTERVAL)
                    weatherForecastDownloadJob.start()
                }
            }

            is NoSuchDatabaseEntryException -> {
                showError(
                    app.applicationContext.getString(
                        R.string.database_entry_for_city_not_found, throwable.message
                    )
                )
            }

            else -> {
                Log.e(
                    TAG,
                    app.applicationContext.getString(R.string.forecast_downloading_for_city_failed)
                )
                Log.e(TAG, throwable.stackTraceToString())
                showError(throwable.message.toString())
                //In fact, defines location and loads forecast for it
                _onCityRequestFailedLiveData.postValue(chosenCity.orEmpty())    //TODO .orEmpty()
            }
        }
        throwable.stackTrace.forEach {
            Log.e(TAG, it.toString())
        }
    }

    /**
     * Go to city selection screen.
     */
    fun gotoCitySelection() {
        weatherForecastDownloadJob.cancel()
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
            _onChosenCityBlankLiveData.call()
        } else {
            downloadWeatherForecastForCity(city)
        }
    }

    /**
     * Download weather forecast on a [city].
     */
    fun downloadWeatherForecastForCity(city: String) {
        Log.d(
            TAG,
            app.applicationContext.getString(R.string.forecast_downloading_for_city_text, city)
        )
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
        Log.d(
            TAG,
            app.applicationContext.getString(R.string.forecast_downloading_for_location_text)
        )
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            var result = forecastRemoteInteractor.loadForecastForLocation(
                temperatureType,
                cityModel.location.latitude,
                cityModel.location.longitude
            )
            // City in response is different than city in request
            result = Result.success(result.getOrNull()!!.copy(city = cityModel.city))
            Log.d(
                TAG,
                app.applicationContext.getString(R.string.forecast_downloading_for_location_text)
            )
            processServerResponse(result)
        }
    }

    private fun processServerResponse(result: Result<WeatherForecastDomainModel>) {
        result.getOrNull()?.let { forecastModel -> // Result is not null
            if (result.isSuccess) {
                Log.d(TAG, result.toString())
                showProgressBarState.value = false
                if (forecastModel.serverError.isNotBlank()) {
                    processResponseError(forecastModel.serverError)
                } else {
                    dataModelState.value = forecastModel
                    showStatus(
                        R.string.forecast_for_city,
                        dataModelState.value?.city.orEmpty()
                    )
                    _onSaveForecastLiveData.postValue(forecastModel)
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

    private fun processResponseError(error: String) {
        showError(error)
        //     if (error.contains("Unable to resolve host")) {
        //         throw NoInternetException(app.applicationContext.getString(R.string.database_forecast_downloading))
        //     }
        _onLoadLocalForecastLiveData.call()
    }

    fun getLocalForecast(forecastModel: WeatherForecastDomainModel) {
        dataModelState.value = forecastModel
    }

    /**
     * Set temperature type
     */
    fun setTemperatureType(temperatureType: TemperatureType) {
        this.temperatureType = temperatureType
    }

    companion object {
        private const val TAG = "WeatherForecastViewModel"
    }
}