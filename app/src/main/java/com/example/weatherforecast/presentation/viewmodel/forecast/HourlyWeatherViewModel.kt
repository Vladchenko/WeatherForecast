package com.example.weatherforecast.presentation.viewmodel.forecast

import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.preferences.PreferencesManager
import com.example.weatherforecast.data.util.LoggingService
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
import kotlinx.coroutines.Job
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
    private val statusRenderer: StatusRenderer,
    private val loggingService: LoggingService,
    private val resourceManager: ResourceManager,
    private val preferencesManager: PreferencesManager,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val hourlyWeatherInteractor: HourlyWeatherInteractor,
) : AbstractViewModel(connectivityObserver) {

    private var currentJob: Job? = null

    /**
     * StateFlow that emits the current UI state for the hourly weather forecast.
     *
     * This flow is updated in response to data loading (success or failure) triggered by calls to
     * [loadHourlyWeatherForCity] or [loadHourlyWeatherForLocation]. It can hold the following states:
     *
     * - `null` — initial state, before any data has been loaded.
     * - [WeatherUiState.Loading] — displayed during data loading (set only when location-based request
     *   is made, as city name lookup does not explicitly show loading).
     * - [WeatherUiState.Success] — successfully loaded weather data, containing the domain model and
     *   the data source ([DataSource.LOCAL] or [DataSource.REMOTE]).
     * - [WeatherUiState.Error] — indicates a failure in loading, containing the city name and error message.
     *
     * This flow is observed in the UI layer to reactively update the interface based on the current
     * state of the hourly weather data loading process.
     */
    val hourlyWeatherStateFlow: StateFlow<WeatherUiState<HourlyWeatherDomainModel>?>
        get() = _hourlyWeatherStateFlow
    private val _hourlyWeatherStateFlow =
        MutableStateFlow<WeatherUiState<HourlyWeatherDomainModel>?>(null)

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
    fun loadHourlyWeatherForCity(city: String) {
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
    fun loadHourlyWeatherForLocation(cityModel: CityLocationModel) {
        showProgressBarState.value = true
        _hourlyWeatherStateFlow.value = WeatherUiState.Loading
        currentJob?.cancel()
        currentJob = viewModelScope.launch(exceptionHandler) {
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
                _hourlyWeatherStateFlow.value =
                    WeatherUiState.Success(result.data, DataSource.REMOTE)
                statusRenderer.showSuccessStatusFor(
                    result.data.city
                )
            }

            is LoadResult.Local -> {
                _hourlyWeatherStateFlow.value =
                    WeatherUiState.Success(result.data, DataSource.LOCAL)
                statusRenderer.showWarning(
                    resourceManager.getString(
                        R.string.forecast_outdated, city
                    )
                )
            }

            is LoadResult.Error -> {
                statusRenderer.showError(getErrorMessage(result))
                _hourlyWeatherStateFlow.value = WeatherUiState.Error(city, result.error.toString())
            }
        }
    }

    private fun getErrorMessage(result: LoadResult.Error): String =
        when (val error = result.error) {
            is ForecastError.NetworkError -> when (error.type) {
                ForecastError.NetworkError.Type.ConnectionFailed ->
                    resourceManager.getString(R.string.connection_refused)

                ForecastError.NetworkError.Type.NoInternet ->
                    resourceManager.getString(R.string.network_disconnected)

                ForecastError.NetworkError.Type.Timeout ->
                    resourceManager.getString(R.string.request_timeout)

                ForecastError.NetworkError.Type.SecurityError ->
                    resourceManager.getString(R.string.ssl_error)

                else ->
                    resourceManager.getString(R.string.network_error_generic)
            }

            is ForecastError.ApiKeyInvalid ->
                resourceManager.getString(R.string.api_key_invalid)

            is ForecastError.CityNotFound ->
                resourceManager.getString(R.string.city_not_found, error.city)

            is ForecastError.NoDataAvailable ->
                resourceManager.getString(R.string.no_weather_data_available)

            is ForecastError.LocalDataCorrupted ->
                resourceManager.getString(R.string.local_data_corrupted)

            is ForecastError.UncategorizedError ->
                resourceManager.getString(R.string.unexpected_error)
        }

    companion object {
        private const val TAG = "HourlyWeatherViewModel"
    }
}