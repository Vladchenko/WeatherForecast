package com.example.weatherforecast.presentation.viewmodel.persistence

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.WeatherForecastLocalInteractor
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.presentation.viewmodel.SingleLiveEvent
import com.example.weatherforecast.presentation.viewmodel.geolocation.getLocationByLatLon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Saving and loading forecast and chosen city
 *
 * @property coroutineDispatchers geo location helper class
 * @property chosenCityInteractor city chosen by user persistence interactor
 * @property forecastLocalInteractor local forecast data provider
 */
@HiltViewModel
class PersistenceViewModel @Inject constructor(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val forecastLocalInteractor: WeatherForecastLocalInteractor,
) : AbstractViewModel(coroutineDispatchers) {

    val onLocalForecastSuccessLiveData: LiveData<WeatherForecastDomainModel>
        get() = _onLocalForecastLoadSuccessLiveData

    private val _onLocalForecastLoadSuccessLiveData = SingleLiveEvent<WeatherForecastDomainModel>()

    /**
     * Load local weather forecast from [domainModel]
     */
    fun loadLocalWeatherForecast(domainModel: WeatherForecastDomainModel) = viewModelScope.launch {
        _onLocalForecastLoadSuccessLiveData.postValue(forecastLocalInteractor.loadForecast(domainModel.city))
        showProgressBarState.value = false
    }

    /**
     * Load local weather forecast for [chosenCity]
     */
    fun loadLocalWeatherForecast(chosenCity: String) = viewModelScope.launch {
        _onLocalForecastLoadSuccessLiveData.postValue(forecastLocalInteractor.loadForecast(chosenCity))
        showProgressBarState.value = false
    }

    /**
     * Save weather forecast [domainModel] and previously chosen city to data base
     */
    fun saveForecastAndChosenCity(
        domainModel: WeatherForecastDomainModel
    ) = viewModelScope.launch {
        launch {
            forecastLocalInteractor.saveForecast(domainModel)
        }
        launch {
            saveChosenCity(
                domainModel.city,
                domainModel.coordinate.latitude,
                domainModel.coordinate.longitude
            )
        }
    }

    private suspend fun saveChosenCity(
        city: String,
        latitude: Double,
        longitude: Double,
    ) {
        chosenCityInteractor.saveChosenCity(
            city,
            getLocationByLatLon(
                latitude,
                longitude
            )
        )
    }
}