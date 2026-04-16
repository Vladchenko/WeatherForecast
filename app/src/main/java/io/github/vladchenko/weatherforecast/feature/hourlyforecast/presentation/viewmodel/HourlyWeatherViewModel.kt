package io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain.HourlyWeatherInteractor
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain.model.HourlyWeather
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.presentation.viewmodel.AbstractViewModel
import io.github.vladchenko.weatherforecast.core.ui.state.DataSource
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
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
     * [loadHourlyWeatherForLocation]. It can hold the following states:
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
    val hourlyWeatherStateFlow: StateFlow<WeatherUiState<HourlyWeather>?>
        get() = _hourlyWeatherStateFlow
    private val _hourlyWeatherStateFlow =
        MutableStateFlow<WeatherUiState<HourlyWeather>?>(null)

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        loggingService.logError(TAG, "Unexpected error in hourly weather loading", throwable)
        statusRenderer.showError(throwable.message.toString())
        showProgressBarState.value = false
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

    private fun processServerResponse(city: String, result: LoadResult<HourlyWeather>) {
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