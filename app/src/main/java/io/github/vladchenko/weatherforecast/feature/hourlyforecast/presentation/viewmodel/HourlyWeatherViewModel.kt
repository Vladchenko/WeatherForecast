package io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.state.DataSource
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState.Error
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState.Loading
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState.Success
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.ui.status.StatusType
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain.HourlyWeatherInteractor
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain.model.HourlyWeather
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the UI state and business logic of the hourly weather forecast.
 *
 * This ViewModel orchestrates the data fetching process by delegating to [HourlyWeatherInteractor].
 * It handles remote data fetching with automatic local cache fallback, manages user preferences
 * (e.g., temperature unit), and emits state updates via [hourlyWeatherStateFlow].
 *
 * The ViewModel also handles various error scenarios (network errors, invalid city, etc.)
 * and communicates status updates to the UI layer via [StatusStateHolder].
 *
 * @property loggingService Centralized service for application logging.
 * @property resourceManager Provides access to Android string resources.
 * @property statusStateHolder Manages and broadcasts UI status messages (info, warnings, errors).
 * @property preferencesManager Manages user preferences such as temperature unit (Celsius/Fahrenheit).
 * @property hourlyWeatherInteractor Domain-level interactor responsible for loading hourly weather data.
 */
@HiltViewModel
class HourlyWeatherViewModel @Inject constructor(
    private val loggingService: LoggingService,
    private val resourceManager: ResourceManager,
    private val statusStateHolder: StatusStateHolder,
    private val preferencesManager: PreferencesManager,
    private val hourlyWeatherInteractor: HourlyWeatherInteractor,
) : ViewModel() {

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

    private var currentJob: Job? = null

    private val _hourlyWeatherStateFlow =
        MutableStateFlow<WeatherUiState<HourlyWeather>?>(null)

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        loggingService.logError(TAG, "Unexpected error in hourly weather loading", throwable)
        showError(throwable.message.toString())
    }

    /**
     * Downloads hourly weather forecast based on geographic [cityModel] location.
     *
     * @param cityModel contains city name and coordinates
     */
    fun loadHourlyWeatherForLocation(cityModel: CityLocationModel) {
        _hourlyWeatherStateFlow.value = Loading
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
        when (result) {
            is LoadResult.Remote -> {
                _hourlyWeatherStateFlow.value =
                    Success(result.data, DataSource.REMOTE)
                showStatus(
                    resourceManager.getString(
                        R.string.forecast_loaded_success,
                        result.data.city
                    )
                )
            }

            is LoadResult.Local -> {
                _hourlyWeatherStateFlow.value =
                    Success(result.data, DataSource.LOCAL)
                showWarning(
                    resourceManager.getString(
                        R.string.forecast_outdated, city
                    )
                )
            }

            is LoadResult.Error -> {
                showError(getErrorMessage(result))
                _hourlyWeatherStateFlow.value = Error(city, getErrorMessage(result))
            }

            LoadResult.Loading -> {
                showStatus(
                    resourceManager.getString(
                        R.string.forecast_hourly_loading
                    )
                )
            }
        }
    }

    private fun showError(errorMessage: String) {
        statusStateHolder.updateStatus(
            StatusType.Error(errorMessage)
        )
    }

    private fun showWarning(statusMessage: String) {
        statusStateHolder.updateStatus(
            StatusType.Warning(statusMessage)
        )
    }

    private fun showStatus(statusMessage: String) {
        statusStateHolder.updateStatus(
            StatusType.Info(statusMessage)
        )
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