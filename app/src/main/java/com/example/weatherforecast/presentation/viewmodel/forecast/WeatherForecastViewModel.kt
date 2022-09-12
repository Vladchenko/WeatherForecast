package com.example.weatherforecast.presentation.viewmodel.forecast

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.data.api.customexceptions.CityNotFoundException
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastRemoteInteractor
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.network.NetworkUtils.isNetworkAvailable
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.presentation.viewmodel.SingleLiveEvent
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
    private val chosenCityInteractor: ChosenCityInteractor,
    private val weatherForecastRemoteInteractor: WeatherForecastRemoteInteractor
) : AbstractViewModel(app) {

    private var temperatureType: TemperatureType? = null

    //region livedata fields
    private val _onChosenCityNotFoundLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    private val _onCityRequestFailedLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    private val _onDefineCurrentGeoLocationLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _onDownloadLocalForecastLiveData: SingleLiveEvent<String> =
        SingleLiveEvent()
    private val _onGotoCitySelectionLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _onShowWeatherForecastLiveData: SingleLiveEvent<WeatherForecastDomainModel> =
        SingleLiveEvent()
    //endregion livedata fields

    //region livedata getters fields
    val onChosenCityNotFoundLiveData: LiveData<String>
        get() = _onChosenCityNotFoundLiveData

    val onCityRequestFailedLiveData: LiveData<String>
        get() = _onCityRequestFailedLiveData

    val onDefineCurrentGeoLocationLiveData: LiveData<Unit>
        get() = _onDefineCurrentGeoLocationLiveData

    val onDownloadLocalForecastLiveData: LiveData<String>
        get() = _onDownloadLocalForecastLiveData

    val onGotoCitySelectionLiveData: LiveData<Unit>
        get() = _onGotoCitySelectionLiveData

    val onGetWeatherForecastLiveData: LiveData<WeatherForecastDomainModel>
        get() = _onShowWeatherForecastLiveData
    //endregion livedata getters fields

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("WeatherForecastViewModel", throwable.message ?: "")
        if (throwable is CityNotFoundException) {
            _onShowErrorLiveData.postValue(throwable.message)
            _onChosenCityNotFoundLiveData.postValue(throwable.city)
        }
        if (throwable is NoInternetException) {
            _onShowErrorLiveData.postValue(throwable.cause.toString())
        }
        if (throwable is NoSuchDatabaseEntryException) {
            _onShowErrorLiveData.postValue(
                app.applicationContext.getString(
                    R.string.database_entry_for_city_not_found, throwable.message
                )
            )
        }
        throwable.stackTrace.forEach {
            Log.e("WeatherForecastViewModel", it.toString())
        }
    }

    /**
     * Download weather forecast on a [chosenCity].
     */
    fun getWeatherForecastForCity(chosenCity: String) {
        if (isNetworkAvailable(app.applicationContext)) {
            getForecastWhenNetworkAvailable(chosenCity)
        } else {
            getForecastWhenNetworkNotAvailable(chosenCity)
        }
    }

    private fun getForecastWhenNetworkAvailable(chosenCity: String) {
        if (chosenCity.isNotBlank()) {
            Log.d("WeatherForecastViewModel", "Chosen city is $chosenCity")
            downloadWeatherForecastForCityOrGeoLocation(chosenCity)
        } else {
            Log.d("WeatherForecastViewModel", "Chosen city is empty")
            // Try loading a city model from DB
            viewModelScope.launch(exceptionHandler) {
                val cityModel = chosenCityInteractor.loadChosenCityModel()
                Log.d(
                    "WeatherForecastViewModel",
                    app.applicationContext.getString(
                        R.string.database_city_loaded,
                        cityModel.city,
                        cityModel.location.latitude,
                        cityModel.location.longitude
                    )
                )
                // When there is no loaded city model from database,
                if (cityModel.city.isBlank()) {
                    // Define local city and try downloading a forecast for it
                    _onUpdateStatusLiveData.postValue(app.applicationContext.getString(R.string.current_location_defining_text))
                    _onShowProgressBarLiveData.postValue(true)
                    // Following row defines a city and displays an alert
                    _onDefineCurrentGeoLocationLiveData.postValue(Unit)
                } else {
                    downloadWeatherForecastForCityOrGeoLocation(
                        cityModel.city
                    )
                }
            }
        }
    }

    private fun getForecastWhenNetworkNotAvailable(chosenCity: String) {
        if (chosenCity.isBlank()) {
            // Try loading a city model from DB
            viewModelScope.launch(exceptionHandler) {
                val cityModel = chosenCityInteractor.loadChosenCityModel()
                Log.d(
                    "WeatherForecastViewModel",
                    app.applicationContext.getString(
                        R.string.database_entry_loaded,
                        cityModel.city,
                        cityModel.location.latitude,
                        cityModel.location.longitude
                    )
                )
                // If it is null, show error
                if (cityModel.city.isBlank()) {
                    _onShowErrorLiveData.postValue(app.applicationContext.getString(R.string.network_not_available_error_text))
                } else {
                    // Else download a forecast from database
                    _onDownloadLocalForecastLiveData.postValue(cityModel.city)
                }
            }
        } else {
            viewModelScope.launch(exceptionHandler) {
                _onDownloadLocalForecastLiveData.postValue(chosenCity)
            }
        }
    }

    fun getWeatherForecastForLocation(cityModel: CityLocationModel) {
        _onShowProgressBarLiveData.postValue(true)
        viewModelScope.launch(exceptionHandler) {
            var result = weatherForecastRemoteInteractor.loadRemoteForecastForLocation(
                temperatureType ?: TemperatureType.CELSIUS,
                cityModel.location.latitude,
                cityModel.location.longitude
            )
            // City in response is different than city in request
            result = result.copy(city = cityModel.city)
            processServerResponse(result)
        }
    }

    private fun downloadWeatherForecastForCityOrGeoLocation(city: String) {
        _onShowProgressBarLiveData.postValue(true)
        try {
            viewModelScope.launch(exceptionHandler) {
                val result = weatherForecastRemoteInteractor.loadRemoteForecastForCity(
                    temperatureType ?: TemperatureType.CELSIUS,
                    city
                )
                processServerResponse(result)
            }
        } catch (ex: Exception) {
            Log.e(
                "WeatherForecastViewModel",
                app.applicationContext.getString(R.string.forecast_downloading_for_city_succeeded)
            )
            Log.e("WeatherForecastViewModel", ex.stackTraceToString())
            _onShowErrorLiveData.postValue(ex.message)
            _onCityRequestFailedLiveData.postValue(city)    //In fact, defines location and loads forecast
        }
    }

    private fun processServerResponse(result: WeatherForecastDomainModel) {
        if (result.serverError.isBlank()) {
            Log.d("WeatherForecastViewModel", result.toString())
            _onShowWeatherForecastLiveData.postValue(result)
            // Forecast and chosen city saving is moved to PersistenceViewModel
        } else {
            _onShowErrorLiveData.postValue(result.serverError)
            Log.e("WeatherForecastViewModel8", result.serverError)
            // Try downloading a forecast by location
            _onCityRequestFailedLiveData.postValue(result.city)
        }
    }

    fun onNetworkNotAvailable(hasPermissionForGeoLocation: Boolean, city: String) {
        if (!isNetworkAvailable(app.applicationContext)) {
            if (!hasPermissionForGeoLocation) {
                _onShowErrorLiveData.postValue(app.applicationContext.getString(R.string.network_not_available_error_text))
            } else {
                getWeatherForecastForCity(city)
            }
        }
    }

    /**
     * Go to city selection screen.
     */
    fun onGotoCitySelection() {
        _onGotoCitySelectionLiveData.call()
    }

    /**
     * Set temperature type
     */
    fun setTemperatureType(temperatureType: TemperatureType) {
        this.temperatureType = temperatureType
    }
}