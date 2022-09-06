package com.example.weatherforecast.presentation.viewmodel.persistence

import android.app.Application
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.presentation.viewmodel.SingleLiveEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

/**
 * View model (MVVM component) for weather forecast presentation.
 *
 * @property app custom [Application] implementation for Hilt.
 * @property weatherForecastLocalInteractor provides domain layer data from local storage.
 */
class PersistenceViewModel(
    private val app: Application,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val weatherForecastLocalInteractor: WeatherForecastLocalInteractor
) : AbstractViewModel(app) {

    val onForecastDownloadedLiveData: LiveData<WeatherForecastDomainModel>
        get() = _onForecastDownloadedLiveData
    val onCityDownloadedLiveData: LiveData<CityLocationModel>
        get() = _onCityDownloadedLiveData

    private val _onForecastDownloadedLiveData: SingleLiveEvent<WeatherForecastDomainModel> =
        SingleLiveEvent()
    private val _onCityDownloadedLiveData: SingleLiveEvent<CityLocationModel> = SingleLiveEvent()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("PersistenceViewModel", throwable.message ?: "")
        if (throwable is NoInternetException) {
            _onShowErrorLiveData.postValue(throwable.cause.toString())
        }
        if (throwable is NoSuchDatabaseEntryException) {
            _onShowErrorLiveData.postValue(
                app.applicationContext.getString(
                    R.string.database_record_for_city_not_found, throwable.message
                )
            )
        }
        throwable.stackTrace.forEach {
            Log.e("WeatherForecastViewModel", it.toString())
        }
    }

    /**
     * TODO
     */
    fun saveForecastToDatabase(result: WeatherForecastDomainModel) {
        Log.d("PersistenceViewModel", "Saving forecast to database $result")
        viewModelScope.launch(exceptionHandler) {
            weatherForecastLocalInteractor.saveForecast(result)
        }
        Log.d("PersistenceViewModel", "Forecast saved successfully")
    }

    /**
     * TODO
     */
    fun downloadLocalForecast(city: String) {
        viewModelScope.launch(exceptionHandler) {
            val result = weatherForecastLocalInteractor.loadForecast(city)
            _onForecastDownloadedLiveData.postValue(result)
            Log.d("PersistenceViewModel", result.toString())
            _onShowErrorLiveData.postValue(app.applicationContext.getString(R.string.database_forecast_downloading))
        }
    }

    /**
     * TODO
     */
    fun saveChosenCity(result: WeatherForecastDomainModel) {
        viewModelScope.launch(exceptionHandler) {
            chosenCityInteractor.saveChosenCity(
                result.city,
                getLocationByLatLon(
                    result.coordinate.latitude,
                    result.coordinate.longitude
                )
            )
            Log.d("PersistenceViewModel", "Chosen city saved to database: ${result.city}")
        }
    }

    /**
     * TODO
     */
    fun saveChosenCity(locationModel: CityLocationModel) {
        viewModelScope.launch(exceptionHandler) {
            chosenCityInteractor.saveChosenCity(
                locationModel.city,
                locationModel.location
            )
            Log.d("PersistenceViewModel", "Chosen city saved to database: ${locationModel.city}")
        }
    }

    private fun getLocationByLatLon(latitude: Double, longitude: Double): Location {
        val location = Location(LocationManager.NETWORK_PROVIDER)
        location.latitude = latitude
        location.longitude = longitude
        return location
    }

    /**
     * TODO
     */
    fun loadSavedCity() =
        viewModelScope.launch(exceptionHandler) {
            val cityModel = chosenCityInteractor.loadChosenCityModel()
            Log.d(
                "WeatherForecastViewModel",
                "Chosen city loaded from database = ${cityModel.city}, lat = ${cityModel.location.latitude}, lon = ${cityModel.location.longitude}"
            )
            _onCityDownloadedLiveData.postValue(cityModel)
        }

    /**
     * Remove chosen city from storage.
     */
    fun removeChosenCity() =
        viewModelScope.launch(exceptionHandler) {
            chosenCityInteractor.removeCity()
        }
}