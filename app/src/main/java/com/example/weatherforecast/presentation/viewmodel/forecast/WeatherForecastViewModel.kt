package com.example.weatherforecast.presentation.viewmodel.forecast

import android.app.Application
import android.location.Location
import android.util.Log
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
import com.example.weatherforecast.network.NetworkConnectionListener
import com.example.weatherforecast.network.NetworkMonitor
import com.example.weatherforecast.network.NetworkUtils.isNetworkAvailable
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.presentation.viewmodel.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
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
) : AbstractViewModel(app), NetworkConnectionListener {

    private var savedCity = ""
    private var chosenCity = ""
    private var permissionRequests = 0
    private var chosenLocation = Location("")
    private var temperatureType: TemperatureType? = null

    //region livedata fields
    private val _onChosenCityNotFoundLiveData = SingleLiveEvent<String>()
    private val _onCityRequestFailedLiveData = SingleLiveEvent<String>()
    private val _onDefineCityByCurrentGeoLocationLiveData = SingleLiveEvent<Location>()
    private val _onDefineCityByCurrentGeoLocationSuccessLiveData = SingleLiveEvent<String>()
    private val _onGotoCitySelectionLiveData = SingleLiveEvent<Unit>()
    private val _onRequestPermissionLiveData = SingleLiveEvent<Unit>()
    private val _onRequestPermissionDeniedLiveData = SingleLiveEvent<Unit>()
    private val _onShowLocationPermissionAlertDialogLiveData = SingleLiveEvent<Unit>()
    private val _onShowWeatherForecastLiveData = SingleLiveEvent<WeatherForecastDomainModel>()
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

    val onGetWeatherForecastLiveData: LiveData<WeatherForecastDomainModel>
        get() = _onShowWeatherForecastLiveData

    val onRequestPermissionLiveData: LiveData<Unit>
        get() = _onRequestPermissionLiveData

    val onRequestPermissionDeniedLiveData: LiveData<Unit>
        get() = _onRequestPermissionDeniedLiveData

    val onShowGeoLocationAlertDialogLiveData: LiveData<String>
        get() = _onDefineCityByCurrentGeoLocationSuccessLiveData

    val onShowLocationPermissionAlertDialogLiveData: LiveData<Unit>
        get() = _onShowLocationPermissionAlertDialogLiveData
    //endregion livedata getters fields

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("WeatherForecastViewModel", throwable.message ?: "")
        if (throwable is CityNotFoundException) {
            _onShowErrorLiveData.postValue(throwable.message)
            // Try downloading a forecast by location
            _onCityRequestFailedLiveData.postValue(throwable.city)
        }
        if (throwable is NoInternetException) {
            _onShowErrorLiveData.postValue(throwable.message.toString())
        }
        if (throwable is NoSuchDatabaseEntryException) {
            _onShowErrorLiveData.postValue(
                app.applicationContext.getString(
                    R.string.database_entry_for_city_not_found, throwable.message
                )
            )
        }
        if (throwable is Exception) {   //TODO Ask about it
            Log.e(
                "WeatherForecastViewModel",
                app.applicationContext.getString(R.string.forecast_downloading_for_city_failed)
            )
            Log.e("WeatherForecastViewModel", throwable.stackTraceToString())
            _onShowErrorLiveData.postValue(throwable.message)
            //In fact, defines location and loads forecast for it
            _onCityRequestFailedLiveData.postValue(chosenCity)
        }
        throwable.stackTrace.forEach {
            Log.e("WeatherForecastViewModel", it.toString())
        }
    }

    override fun onNetworkConnectionAvailable() {
        Log.d("NetworkConnectionViewModel", "onNetworkConnectionAvailable")
        _onUpdateStatusLiveData.postValue(app.applicationContext.getString(R.string.network_available_text))
        requestGeoLocationPermissionOrDownloadWeatherForecast()
    }

    override fun onNetworkConnectionLost() {
        Log.d("NetworkConnectionViewModel", "onNetworkConnectionLost")
        _onShowErrorLiveData.postValue(app.applicationContext.getString(R.string.network_not_available_error_text))
        val city = chosenCity.ifBlank {
            savedCity
        }
        downloadWeatherForecastForCityOrGeoLocation(city)
    }

    /**
     * City defining by current geo location successful callback.
     */
    fun onDefineCityByCurrentGeoLocationSuccess(city: String) {
        Log.d(
            "WeatherForecastViewModel",
            "City defined successfully by CURRENT geo location, city = $city, location = $chosenLocation"
        )
        saveChosenCity(CityLocationModel(city, chosenLocation))
        _onDefineCityByCurrentGeoLocationSuccessLiveData.postValue(city)
        chosenCity = city
    }

    /**
     * Callback for successful geo location.
     * Save a [city] and its [location] and download its forecast.
     */
    fun onDefineGeoLocationByCitySuccess(city: String, location: Location) {
        Log.d(
            "WeatherForecastViewModel",
            "Geo location defined successfully by city = $city, location = $location"
        )
        try {
            val cityModel = CityLocationModel(city, location)
            saveChosenCity(cityModel)
            Log.d("WeatherForecastViewModel", "City and its location saved successfully.")
            downloadWeatherForecastForLocation(cityModel)
        } catch (ex: Exception) {
            _onShowErrorLiveData.postValue(ex.message)
        }
    }

    /**
     * Go to city selection screen.
     */
    fun onGotoCitySelection() {
        _onGotoCitySelectionLiveData.call()
    }

    /**
     * Proceed with a geo location permission result, having [isGranted] flag as a permission result,
     * [chosenCity] as a previously chosen city on city selection screen, or a [savedCity] loaded
     * from database.
     */
    fun onPermissionResolution(isGranted: Boolean, chosenCity: String) {
        if (isGranted) {
            Log.d(
                "WeatherForecastViewModel",
                "Permission granted callback. Chosen city = $chosenCity, saved city = $savedCity"
            )
            loadWeatherForecastForChosenOrSavedCity(chosenCity)
        } else {
            _onShowErrorLiveData.postValue(app.applicationContext.getString(R.string.geo_location_no_permission))
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
                "WeatherForecastViewModel",
                "Chosen city loaded from database = ${cityModel.city}, lat = ${cityModel.location.latitude}, lon = ${cityModel.location.longitude}"
            )
            savedCity = cityModel.city
            NetworkMonitor(app.applicationContext, this@WeatherForecastViewModel)
            // Since NetworkMonitor doesn't check if app started with no inet, following check is required
            if (!isNetworkAvailable(app.applicationContext)) {  //TODO Ask about it
                requestGeoLocationPermissionOrDownloadWeatherForecast()
            }
        }

    /**
     * Download weather forecast on a [city].
     */
    fun downloadWeatherForecastForCityOrGeoLocation(city: String) {
        Log.d(
            "WeatherForecastViewModel",
            app.applicationContext.getString(R.string.forecast_downloading_for_city_text)
        )
        _onShowProgressBarLiveData.postValue(true)
        viewModelScope.launch(exceptionHandler) {
            val result = weatherForecastRemoteInteractor.loadForecastForCity(
                temperatureType ?: TemperatureType.CELSIUS,
                city
            )
            processServerResponse(result)
        }
    }

    private fun downloadWeatherForecastForLocation(cityModel: CityLocationModel) {
        Log.e(
            "WeatherForecastViewModel",
            app.applicationContext.getString(R.string.forecast_downloading_for_location_text)
        )
        _onShowProgressBarLiveData.postValue(true)
        viewModelScope.launch(exceptionHandler) {
            var result = weatherForecastRemoteInteractor.loadRemoteForecastForLocation(
                temperatureType ?: TemperatureType.CELSIUS,
                cityModel.location.latitude,
                cityModel.location.longitude
            )
            // City in response is different than city in request
            result = result.copy(city = cityModel.city)
            Log.e(
                "WeatherForecastViewModel",
                app.applicationContext.getString(R.string.forecast_downloading_for_location_text)
            )
            processServerResponse(Result.success(result))
        }
    }

    private fun processServerResponse(result: Result<WeatherForecastDomainModel>) {
        if (result.isSuccess) {
            Log.d("WeatherForecastViewModel", result.toString())
            _onShowWeatherForecastLiveData.postValue(result.getOrNull())
            val error = result.getOrNull()?.serverError
            if (error?.isNotBlank() == true) {
                _onShowErrorLiveData.postValue(error)
            } else {
                Log.d(
                    "WeatherForecastViewModel",
                    app.applicationContext.getString(R.string.forecast_downloading_for_city_succeeded)
                )
            }
            viewModelScope.launch {
                weatherForecastLocalInteractor.saveForecast(result.getOrNull()!!)
                chosenCityInteractor.saveChosenCity(
                    result.getOrNull()?.city ?: "",
                    getLocationByLatLon(
                        result.getOrNull()?.coordinate?.latitude ?: 0.0,
                        result.getOrNull()?.coordinate?.longitude ?: 0.0
                    )
                )
            }
        } else {
            _onShowErrorLiveData.postValue(result.getOrNull()?.serverError)
            Log.e(
                "WeatherForecastViewModel",
                result.getOrNull()?.serverError ?: "No error description"
            )
            // Try downloading a forecast by location
            _onCityRequestFailedLiveData.postValue(result.getOrNull()?.city ?: "")
        }
    }

    /**
     * Requests a geo location or downloads a forecast, depending on a presence of a [chosenCity]
     * or [savedCity], having [temperatureType] provided.
     */
    private fun requestGeoLocationPermissionOrDownloadWeatherForecast() {
        Log.d("WeatherForecastViewModel", "chosenCity = $chosenCity, savedCity = $savedCity")
        if (chosenCity.isBlank()
            && savedCity.isBlank()
        ) {
            requestGeoLocationPermissionOrLoadForecast()
        } else {
            val city = chosenCity.ifBlank {
                savedCity
            }
            downloadWeatherForecastForCityOrGeoLocation(city)
        }
    }

    /**
     * Download a forecast for a [chosenCity] or a savedCity.
     */
    private fun loadWeatherForecastForChosenOrSavedCity(chosenCity: String) {
        if (chosenCity.isNotBlank()) {
            // Get weather forecast, when there is a chosen city (from a city selection fragment)
            downloadWeatherForecastForCityOrGeoLocation(chosenCity)
        } else {
            if (savedCity.isNotBlank()) {
                // Get weather forecast, when there is a saved city (from a database)
                downloadWeatherForecastForCityOrGeoLocation(savedCity)
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
            _onUpdateStatusLiveData.postValue(app.applicationContext.getString(R.string.geo_location_permission_required))
            permissionRequests++
            if (permissionRequests > 2) {
                _onRequestPermissionDeniedLiveData.call()
            } else {
                _onRequestPermissionLiveData.postValue(Unit)
                Log.d("WeatherForecastViewModel", "Geo location permission requested")
            }
        } else {
            if (chosenCity.isBlank()) {
                defineCurrentGeoLocation()
            } else {
                downloadWeatherForecastForCityOrGeoLocation(chosenCity)
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
            Log.d("WeatherForecastViewModel", "Chosen city saved to database: ${locationModel.city}")
        }
    }

    private fun defineCurrentGeoLocation() {
        _onUpdateStatusLiveData.postValue(app.applicationContext.getString(R.string.current_location_defining_text))
        geoLocator.defineCurrentLocation(object : GeoLocationListener {
            override fun onCurrentGeoLocationSuccess(location: Location) {
                this@WeatherForecastViewModel.chosenLocation = location
                _onDefineCityByCurrentGeoLocationLiveData.postValue(location)
                _onShowProgressBarLiveData.postValue(false)
            }

            override fun onCurrentGeoLocationFail(errorMessage: String) {
                Log.e("WeatherForecastViewModel", errorMessage)
                // Since exception is not informative enough for user, replace it with a standard error one.
                if (errorMessage.contains("permission")) {
                    _onShowErrorLiveData.postValue(app.getString(R.string.geo_location_permission_required))
                } else {
                    _onShowErrorLiveData.postValue(errorMessage)
                }
            }

            override fun onNoGeoLocationPermission() {
                Log.e("WeatherForecastViewModel", "No geo location permission - requesting it")
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
    }
}