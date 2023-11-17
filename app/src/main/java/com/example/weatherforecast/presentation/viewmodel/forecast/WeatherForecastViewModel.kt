package com.example.weatherforecast.presentation.viewmodel.forecast

import android.app.Application
import android.location.Location
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
import com.example.weatherforecast.geolocation.GeoLocationListener
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator
import com.example.weatherforecast.geolocation.hasPermissionForGeoLocation
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.presentation.PresentationUtils.REPEAT_INTERVAL
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.presentation.viewmodel.SingleLiveEvent
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
    private val geoLocator: WeatherForecastGeoLocator,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val weatherForecastLocalInteractor: WeatherForecastLocalInteractor,
    private val weatherForecastRemoteInteractor: WeatherForecastRemoteInteractor
) : AbstractViewModel(app) {

    private var permissionRequests = 0
    private lateinit var savedCity: String
    private lateinit var chosenCity: String
    private var chosenLocation = Location("")
    private lateinit var weatherForecastDownloadJob: Job
    private lateinit var temperatureType: TemperatureType

    val dataModelState: MutableState<WeatherForecastDomainModel?> = mutableStateOf(null)

    //region livedata fields
    private val _onChosenCityNotFoundLiveData = SingleLiveEvent<String>()
    private val _onCityRequestFailedLiveData = SingleLiveEvent<String>()
    private val _onDefineCityByCurrentGeoLocationLiveData = SingleLiveEvent<Location>()
    private val _onDefineCityByCurrentGeoLocationSuccessLiveData = SingleLiveEvent<String>()
    private val _onGotoCitySelectionLiveData = SingleLiveEvent<Unit>()
    private val _onRequestPermissionLiveData = SingleLiveEvent<Unit>()
    private val _onRequestPermissionDeniedLiveData = SingleLiveEvent<Unit>()
    private val _onShowLocationPermissionAlertDialogLiveData = SingleLiveEvent<Unit>()
    //endregion livedata fields

    //region livedata getters fields
    val onChosenCityNotFoundLiveData: LiveData<String>
        get() = _onChosenCityNotFoundLiveData

    val onCityRequestFailedLiveData: LiveData<String>
        get() = _onCityRequestFailedLiveData

    val onDefineCityByCurrentGeoLocationLiveData: LiveData<Location>
        get() = _onDefineCityByCurrentGeoLocationLiveData

    val onGotoCitySelectionLiveData: LiveData<Unit>
        get() = _onGotoCitySelectionLiveData

    val onRequestPermissionLiveData: LiveData<Unit>
        get() = _onRequestPermissionLiveData

    val onRequestPermissionDeniedLiveData: LiveData<Unit>
        get() = _onRequestPermissionDeniedLiveData

    val onShowGeoLocationAlertDialogLiveData: LiveData<String>
        get() = _onDefineCityByCurrentGeoLocationSuccessLiveData

    val onShowLocationPermissionAlertDialogLiveData: LiveData<Unit>
        get() = _onShowLocationPermissionAlertDialogLiveData
    //endregion livedata getters fields

    init {
        weatherForecastDownloadJob = viewModelScope.launch(Dispatchers.IO, CoroutineStart.LAZY) {
            delay(REPEAT_INTERVAL)
            requestGeoLocationPermissionOrDownloadWeatherForecast(false)
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
                weatherForecastDownloadJob.start()
            }

            is NoSuchDatabaseEntryException -> {
                onShowError(
                    app.applicationContext.getString(
                        R.string.database_entry_for_city_not_found, throwable.message
                    )
                )
            }

            else -> {
                Log.e(TAG, app.applicationContext.getString(R.string.forecast_downloading_for_city_failed))
                Log.e(TAG, throwable.stackTraceToString())
                onShowError(throwable.message.toString())
                //In fact, defines location and loads forecast for it
                _onCityRequestFailedLiveData.postValue(chosenCity)
            }
        }
        throwable.stackTrace.forEach {
            Log.e(TAG, it.toString())
        }
    }

    init {
        toolbarSubtitleState.value = app.getString(R.string.forecast_is_loading)
    }

    /**
     * City defining by current geo location successful callback.
     */
    fun onDefineCityByCurrentGeoLocationSuccess(city: String) {
        Log.d(TAG, "City defined successfully by CURRENT geo location, city = $city, location = $chosenLocation")
        saveChosenCity(CityLocationModel(city, chosenLocation))
        _onDefineCityByCurrentGeoLocationSuccessLiveData.postValue(city)
        chosenCity = city
    }

    /**
     * Callback for successful geo location.
     * Save a [city] and its [location] and download its forecast.
     */
    fun onDefineGeoLocationByCitySuccess(city: String, location: Location) {
        Log.d(TAG, "Geo location defined successfully by city = $city, location = $location")
        try {
            val cityModel = CityLocationModel(city, location)
            saveChosenCity(cityModel)
            Log.d(TAG, "City and its location saved successfully.")
            downloadWeatherForecastForLocation(cityModel)
        } catch (ex: Exception) {
            onShowError(ex.message.toString())
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
     * Proceed with a geo location permission result, having [isGranted] flag as a permission result,
     * [chosenCity] as a previously chosen city on city selection screen, or a [savedCity] loaded
     * from database.
     */
    fun onPermissionResolution(isGranted: Boolean, chosenCity: String) {
        if (isGranted) {
            Log.d(TAG, "Permission granted callback. Chosen city = $chosenCity, saved city = $savedCity")
            loadWeatherForecastForCity(chosenCity)
        } else {
            onShowError(app.applicationContext.getString(R.string.geo_location_no_permission))
            _onShowLocationPermissionAlertDialogLiveData.call()
        }
    }

    /**
     * Initialize weather forecast downloading.
     */
    fun initWeatherForecast() =
        viewModelScope.launch(exceptionHandler) {
            val cityModel = chosenCityInteractor.loadChosenCityModel()
            Log.d(
                TAG,
                "Chosen city loaded from database = ${cityModel.city}," +
                    " lat = ${cityModel.location.latitude}," +
                    " lon = ${cityModel.location.longitude}"
            )
            savedCity = cityModel.city
            requestGeoLocationPermissionOrDownloadWeatherForecast(true)
        }

    /**
     * Download weather forecast on a [city].
     */
    fun downloadWeatherForecastForCityOrGeoLocation(city: String, showProgress: Boolean) {
        Log.d(TAG, app.applicationContext.getString(R.string.forecast_downloading_for_city_text, city))
        if (showProgress) {
            showProgressBarState.value = true
        }
        viewModelScope.launch(exceptionHandler) {
            val result = weatherForecastRemoteInteractor.loadForecastForCity(
                temperatureType,
                city
            )
            processServerResponse(result)
        }
    }

    private fun downloadWeatherForecastForLocation(cityModel: CityLocationModel) {
        Log.d(TAG, app.applicationContext.getString(R.string.forecast_downloading_for_location_text))
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            var result = weatherForecastRemoteInteractor.loadForecastForLocation(
                temperatureType,
                cityModel.location.latitude,
                cityModel.location.longitude
            )
            // City in response is different than city in request
            result = result.copy(city = cityModel.city)
            Log.d(TAG, app.applicationContext.getString(R.string.forecast_downloading_for_location_text))
            processServerResponse(Result.success(result))
        }
    }

    private fun processServerResponse(result: Result<WeatherForecastDomainModel>) {
        result.getOrNull()?.let {   // Result is not null
            if (result.isSuccess) {
                Log.d(TAG, result.toString())
                dataModelState.value = result.getOrNull()
                showProgressBarState.value = false
                onShowStatus(app.getString(R.string.forecast_for_city, dataModelState.value?.city))
                processError(result.getOrNull()?.serverError)
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
                // Try downloading a forecast by location
                _onCityRequestFailedLiveData.postValue(it.city)
            }
        } ?: run {// Result is null
            onShowError("Result is null")
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

    private fun processError(error: String?) {
        if (error?.isNotBlank() == true) {
            onShowError(error)
            if (error.contains("Unable to resolve host")) {
                throw NoInternetException(app.applicationContext.getString(R.string.database_forecast_downloading))
            }
        } else {
            Log.d(TAG, app.applicationContext.getString(R.string.forecast_downloading_for_city_succeeded))
        }
    }

    /**
     * Requests a geo location or downloads a forecast, depending on a presence of a [chosenCity]
     * or [savedCity], having [temperatureType] provided.
     */
    private fun requestGeoLocationPermissionOrDownloadWeatherForecast(showProgress: Boolean) {
        Log.d(TAG, "chosenCity = $chosenCity, savedCity = $savedCity")
        if (chosenCity.isBlank()
            && savedCity.isBlank()
        ) {
            requestGeoLocationPermissionOrLoadForecast()
        } else {
            val city = chosenCity.ifBlank {
                savedCity
            }
            downloadWeatherForecastForCityOrGeoLocation(city, showProgress)
        }
    }

    /**
     * Download a forecast for a [chosenCity] or a savedCity.
     */
    private fun loadWeatherForecastForCity(chosenCity: String) {
        if (chosenCity.isNotBlank()) {
            // Get weather forecast, when there is a chosen city (from a city selection fragment)
            downloadWeatherForecastForCityOrGeoLocation(chosenCity, true)
        } else {
            if (savedCity.isNotBlank()) {
                // Get weather forecast, when there is a saved city (from a database)
                downloadWeatherForecastForCityOrGeoLocation(savedCity, true)
            } else {
                // Else show alert dialog on a city defined by current geo location
                defineCurrentGeoLocation()
            }
        }
    }

    /**
     * Request geo location permission, when it is not granted.
     */
    fun requestGeoLocationPermissionOrLoadForecast() {
        if (!hasPermissionForGeoLocation(app.applicationContext)) {
            onShowStatus(app.applicationContext.getString(R.string.geo_location_permission_required))
            permissionRequests++
            if (permissionRequests > 2) {
                _onRequestPermissionDeniedLiveData.postValue(Unit)
            } else {
                _onRequestPermissionLiveData.postValue(Unit)
                Log.d(TAG, "Geo location permission requested")
            }
        } else {
            if (chosenCity.isBlank()) {
                defineCurrentGeoLocation()
            } else {
                downloadWeatherForecastForCityOrGeoLocation(chosenCity, true)
            }
        }
    }

    /**
     * Save chosen city with data from [locationModel]
     */
    private fun saveChosenCity(locationModel: CityLocationModel) {
        viewModelScope.launch(exceptionHandler) {
            chosenCityInteractor.saveChosenCity(
                locationModel.city,
                locationModel.location
            )
            Log.d(TAG, "Chosen city saved to database: ${locationModel.city}")
        }
    }

    private fun defineCurrentGeoLocation() {
        onShowStatus(app.applicationContext.getString(R.string.current_location_defining_text))
        geoLocator.defineCurrentLocation(app.applicationContext, object : GeoLocationListener {
            override fun onCurrentGeoLocationSuccess(location: Location) {
                this@WeatherForecastViewModel.chosenLocation = location
                _onDefineCityByCurrentGeoLocationLiveData.postValue(location)
                showProgressBarState.value = false
            }

            override fun onCurrentGeoLocationFail(errorMessage: String) {
                Log.e(TAG, errorMessage)
                // Since exception is not informative enough for user, replace it with a standard error one.
                if (errorMessage.contains("permission")) {
                    onShowError(app.getString(R.string.geo_location_permission_required))
                } else {
                    onShowError(errorMessage)
                }
            }

            override fun onNoGeoLocationPermission() {
                Log.e(TAG, "No geo location permission - requesting it")
                requestGeoLocationPermissionOrLoadForecast()
            }
        })
    }

    /**
     * Set temperature type
     */
    fun setTemperatureType(temperatureType: TemperatureType) {
        this.temperatureType = temperatureType
    }

    /**
     * Set previously chosen city with [city]
     */
    fun setChosenCity(city: String) {
        chosenCity = city
        toolbarSubtitleState.value = app.getString(R.string.forecast_downloading_for_city_text, city)
    }

    companion object {
        private const val TAG = "WeatherForecastViewModel"
    }
}