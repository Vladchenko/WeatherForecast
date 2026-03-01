package com.example.weatherforecast.presentation.viewmodel.forecast

import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.preferences.PreferencesManager
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.HourlyWeatherInteractor
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.domain.ForecastError
import com.example.weatherforecast.models.domain.HourlyWeatherDomainModel
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.utils.ResourceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for hourly weather forecast.
 *
 * This ViewModel handles:
 * - Loading hourly weather data from remote or local sources
 * - Managing UI state via [hourlyWeatherStateFlow]
 * - Responding to city name or location-based requests
 * - Displaying success, warning, and error messages
 * - Using structured logging via [LoggingService] instead of direct [android.util.Log]
 *
 * @param connectivityObserver observes internet connectivity state
 * @param coroutineDispatchers configures dispatchers for coroutines
 * @property statusRenderer displays status messages to the user
 * @property loggingService centralized service for application logging
 * @property resourceManager provides access to Android resources (strings, etc.)
 * @property preferencesManager manages user preferences (e.g. temperature unit)
 * @property chosenCityInteractor handles retrieval of the selected city
 * @property hourlyWeatherInteractor loads hourly weather data
 */
@HiltViewModel
class HourlyWeatherViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    coroutineDispatchers: CoroutineDispatchers,
    private val statusRenderer: StatusRenderer,
    private val loggingService: LoggingService,
    private val resourceManager: ResourceManager,
    private val preferencesManager: PreferencesManager,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val hourlyWeatherInteractor: HourlyWeatherInteractor,
) : AbstractViewModel(connectivityObserver) {

    val hourlyWeatherStateFlow: StateFlow<HourlyWeatherDomainModel?>
        get() = _hourlyWeatherStateFlow
    private val _hourlyWeatherStateFlow = MutableStateFlow<HourlyWeatherDomainModel?>(null)

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        loggingService.logError(TAG, "Unexpected error in hourly weather loading", throwable)
        statusRenderer.showError(throwable.message.toString())
        showProgressBarState.value = false
    }

    /**
     * Downloads hourly weather forecast for the specified [city].
     *
     * @param city the name of the city to fetch weather for
     */
    fun getHourlyWeatherForCity(city: String) {
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            val temperatureType = preferencesManager.temperatureTypeStateFlow.first()
            val result = hourlyWeatherInteractor.loadHourlyForecastForCity(
                temperatureType,
                city
            )
            processServerResponse(city, result)
        }
    }

    /**
     * Downloads hourly weather forecast based on geographic [cityModel] location.
     *
     * @param cityModel contains city name and coordinates
     */
    fun getHourlyWeatherForLocation(cityModel: CityLocationModel) {
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            val temperatureType = preferencesManager.temperatureTypeStateFlow.first()
            val result = hourlyWeatherInteractor.loadHourlyWeatherForLocation(
                cityModel.city,
                temperatureType,
                cityModel.location.latitude,
                cityModel.location.longitude
            )
            processServerResponse(cityModel.city, result)
        }
    }

    private fun processServerResponse(city: String, result: LoadResult<HourlyWeatherDomainModel>) {
        showProgressBarState.value = false
        when (result) {
            is LoadResult.Remote -> {
                _hourlyWeatherStateFlow.value = result.data
                statusRenderer.showStatus(
                    resourceManager.getString(
                        R.string.forecast_loaded_success,
                        result.data.city
                    )
                )
            }

            is LoadResult.Local -> {
                _hourlyWeatherStateFlow.value = result.data
                statusRenderer.showWarning(
                    resourceManager.getString(
                        R.string.forecast_outdated, city
                    )
                )
            }

            is LoadResult.Error -> {
                statusRenderer.showError(
                    when (result.error) {
                        is ForecastError.NoInternet ->
                            resourceManager.getString(R.string.network_disconnected)

                        else -> {
                            loggingService.logError(
                                TAG,
                                "Error loading hourly forecast for city: $city",
                                Exception(result.error.toString())
                            )
                            resourceManager.getString(R.string.forecast_load_error)
                        }
                    }
                )
            }
        }
    }

    companion object {
        private const val TAG = "HourlyWeatherViewModel"
    }
}