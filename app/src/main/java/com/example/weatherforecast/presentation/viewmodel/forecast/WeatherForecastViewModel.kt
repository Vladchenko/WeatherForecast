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
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.presentation.PresentationUtils.REPEAT_INTERVAL
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.presentation.viewmodel.SingleLiveEvent
import com.example.weatherforecast.presentation.viewmodel.geolocation.getLocationByLatLon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * View model (MVVM component) for weather forecast presentation.
 *
 * @param app custom [Application] implementation for Hilt
 * @param geoLocator geo location helper class
 * @param chosenCityInteractor city chosen by user persistence interactor
 * @param weatherForecastLocalInteractor local forecast data provider
 * @param weatherForecastRemoteInteractor local forecast data provider
 */
@HiltViewModel
class WeatherForecastViewModel @Inject constructor(
    private val app: Application,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val weatherForecastLocalInteractor: WeatherForecastLocalInteractor,
    private val weatherForecastRemoteInteractor: WeatherForecastRemoteInteractor
) : AbstractViewModel(app) {

    val dataModelState: MutableState<WeatherForecastDomainModel?> = mutableStateOf(null)

    private var savedCity: String? = null
    private var chosenCity: String? = null
    private var weatherForecastDownloadJob: Job
    private lateinit var temperatureType: TemperatureType

    //region livedata fields
    private val _onChosenAndSavedCitiesBlankLiveData = SingleLiveEvent<Unit>()
    private val _onChosenCityNotFoundLiveData = SingleLiveEvent<String>()
    private val _onCityRequestFailedLiveData = SingleLiveEvent<String>()
    private val _onGotoCitySelectionLiveData = SingleLiveEvent<Unit>()
    //endregion livedata fields

    //region livedata getters fields
    val onChosenAndSavedCitiesBlankLiveData: LiveData<Unit>
        get() = _onChosenAndSavedCitiesBlankLiveData

    val onChosenCityNotFoundLiveData: LiveData<String>
        get() = _onChosenCityNotFoundLiveData

    val onCityRequestFailedLiveData: LiveData<String>
        get() = _onCityRequestFailedLiveData

    val onGotoCitySelectionLiveData: LiveData<Unit>
        get() = _onGotoCitySelectionLiveData
    //endregion livedata getters fields

    init {
        onShowStatus(R.string.forecast_is_loading)
        weatherForecastDownloadJob = viewModelScope.launch(Dispatchers.IO, CoroutineStart.LAZY) {
            downloadWeatherForecast(chosenCity.orEmpty())
        }
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, throwable.message.orEmpty())
        when (throwable) {
            is CityNotFoundException -> {
                onShowError(throwable.message)
                // Try downloading a forecast by location
                _onCityRequestFailedLiveData.postValue(throwable.city)
            }

            is NoInternetException -> {
                onShowError(throwable.message.toString())
                viewModelScope.launch(Dispatchers.IO) {
                    delay(REPEAT_INTERVAL)
                    weatherForecastDownloadJob.start()
                }
            }

            is NoSuchDatabaseEntryException -> {
                onShowError(
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
                onShowError(throwable.message.toString())
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
    fun onGotoCitySelection() {
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
                    "Downloaded from database: city = ${cityModel.city}, " +
                        "lat = ${cityModel.location.latitude}, lon = ${cityModel.location.longitude}"
                )
                cityModel.city
            }
            if (city.isBlank()) {
                _onChosenAndSavedCitiesBlankLiveData.call()
            } else {
                downloadWeatherForecast(city)
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
            val result = weatherForecastRemoteInteractor.loadForecastForCity(
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
            var result = weatherForecastRemoteInteractor.loadForecastForLocation(
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
        result.getOrNull()?.let {   // Result is not null
            if (result.isSuccess) {
                Log.d(TAG, result.toString())
                dataModelState.value = it
                showProgressBarState.value = false
                onShowStatus(
                    R.string.forecast_for_city,
                    dataModelState.value?.city.orEmpty()
                )
                result.getOrNull()?.serverError?.let { error ->
                    processError(error)
                }
                viewModelScope.launch {
                    launch {
                        weatherForecastLocalInteractor.saveForecast(it)
                    }
                    launch {
                        saveChosenCity(it, result)
                    }
                }
            } else {
                onShowError(it.serverError)
                Log.e(TAG, it.serverError)
                _onCityRequestFailedLiveData.postValue(it.city)
            }
        } ?: run {
            onShowError("Server responded with $this")
        }
    }

    private suspend fun saveChosenCity(
        it: WeatherForecastDomainModel,
        result: Result<WeatherForecastDomainModel>
    ) {
        chosenCityInteractor.saveChosenCity(
            it.city,
            getLocationByLatLon(
                result.getOrNull()?.coordinate?.latitude ?: 0.0,
                result.getOrNull()?.coordinate?.longitude ?: 0.0
            )
        )
    }

    private fun processError(error: String) {
        onShowError(error)
        if (error.contains("Unable to resolve host")) {
            throw NoInternetException(app.applicationContext.getString(R.string.database_forecast_downloading))
        }
    }

    /**
     * Requests a geo location or downloads a forecast, depending on a presence of a [chosenCity]
     * or [savedCity], having [temperatureType] provided.
     */
    private fun downloadWeatherForecast(city: String) {
        if (city.isBlank()) {
            _onChosenAndSavedCitiesBlankLiveData.call()
        } else {
            downloadWeatherForecastForCity(city)
        }
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