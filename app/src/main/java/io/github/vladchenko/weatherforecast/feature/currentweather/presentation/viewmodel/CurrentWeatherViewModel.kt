package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.model.TemperatureType
import io.github.vladchenko.weatherforecast.core.network.NetworkStateHolder
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.ui.status.StatusType.Error
import io.github.vladchenko.weatherforecast.core.ui.status.StatusType.Info
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.CurrentWeatherInteractor
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.outputmapper.WeatherOutputMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.models.CurrentWeatherUi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the current weather forecast UI and business logic.
 *
 * This ViewModel orchestrates the complete weather data lifecycle:
 * - Loading weather data from remote API with local cache fallback
 * - Managing UI state transitions (Loading → Success/Error)
 * - Handling city selection and persistence
 * - Processing various error scenarios (network, API, local data corruption)
 * - Supporting temperature unit preferences (Celsius/Fahrenheit)
 *
 * The ViewModel implements a "remote-first, cache-fallback" strategy:
 * 1. Attempts to fetch fresh data from the remote API
 * 2. If remote fetch fails, loads cached data and shows a warning
 * 3. If both fail, displays an appropriate error message
 *
 * @property loggingService Centralized service for structured application logging
 * @property resourceManager Provides access to Android string resources for UI messages
 * @property statusStateHolder Manages and broadcasts UI status messages (info, warnings, errors)
 * @property networkStateHolder Manages network connectivity (connect or disconnect)
 * @property preferencesManager Manages user preferences (e.g., temperature unit: Celsius/Fahrenheit)
 * @property weatherOutputMapper Processes raw weather data loading results and maps them to a UI-ready structure
 * @property chosenCityInteractor Handles persistence and retrieval of the selected city
 * @property forecastInteractor Loads weather data from the remote API with local cache fallback
 */
@HiltViewModel
class CurrentWeatherViewModel @Inject constructor(
    private val loggingService: LoggingService,
    private val resourceManager: ResourceManager,
    private val statusStateHolder: StatusStateHolder,
    private val networkStateHolder: NetworkStateHolder,
    private val preferencesManager: PreferencesManager,
    private val weatherOutputMapper: WeatherOutputMapper,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val forecastInteractor: CurrentWeatherInteractor,
) : ViewModel() {

    //region flows
    /**
     * Public read-only flow that emits the current UI state of the weather forecast.
     * Observers receive updates as [WeatherUiState.Loading], [WeatherUiState.Success], or error states.
     */
    val weatherStateFlow: StateFlow<WeatherUiState<CurrentWeatherUi>>
        get() = _forecastStateFlow

    /**
     * SharedFlow that emits city-related error events when the requested city cannot be loaded.
     *
     * Used to notify the UI layer about failures to resolve or fetch the selected city.
     * Emits [CityErrorEvent] instances indicating either a missing city input or a city that could not be found.
     */
    val cityErrorEventFlow: SharedFlow<CityErrorEvent>
        get() = _cityErrorEventFlow

    private val _cityErrorEventFlow = MutableSharedFlow<CityErrorEvent>(
        extraBufferCapacity = 1
    )
    private val _forecastStateFlow =
        MutableStateFlow<WeatherUiState<CurrentWeatherUi>>(WeatherUiState.Loading(false))
    //endregion flows

    private var currentJob: Job? = null

    private lateinit var temperatureType: TemperatureType

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        loggingService.logError(TAG, "Unexpected error in weather forecast loading", throwable)
        statusStateHolder.updateStatus(
            Error(throwable.message.toString())
        )
    }
    val scope = CoroutineScope(viewModelScope.coroutineContext + exceptionHandler)

    init {
        scope.launch {
            loadSavedCity()
        }
        scope.launch {
            preferencesManager.temperatureTypeStateFlow.collect { tempType ->
                loggingService.logDebugEvent(TAG, "Temperature unit changed: $tempType")
                temperatureType = tempType
            }
        }
        scope.launch {
            networkStateHolder.networkStateFlow.collect { state ->
                when (state) {
                    false -> {} // Do nothing
                    true -> {
                        refreshWeather(false)
                    }
                }
            }
        }
    }

    /**
     * Launches weather forecast download, using the provided [cityModel].
     * If blank, attempts to load the last saved city from [chosenCityInteractor].
     *
     * @param cityModel to provide a weather for
     */
    fun launchWeatherForecast(cityModel: CityLocationModel) {
        scope.launch {
            showLoadingStatusFor(cityModel.city)
            val model = if (cityModel.city.isBlank()) {
                val savedModel = chosenCityInteractor.loadChosenCity()
                if (savedModel.city.isBlank()) {
                    _cityErrorEventFlow.tryEmit(CityErrorEvent.CityBlank)
                    return@launch
                }
                savedModel
            } else {
                cityModel
            }

            loadRemoteForecastForLocation(model)
        }
    }

    /**
     * Refreshes weather data using the currently saved city.
     * Reuses the last known city location to fetch updated weather information.
     *
     * If [isPullToRefresh] is true, sets the refreshing state to true to indicate
     * a manual pull-to-refresh action (UI can show refresh indicator).
     *
     * No-op if no city is currently saved (chosenCityStateFlow is null).
     * In this case, user should first select a city via city search.
     *
     * @param isPullToRefresh true when triggered by user pull-to-refresh gesture
     */
    fun refreshWeather(isPullToRefresh: Boolean) {
        _forecastStateFlow.tryEmit(WeatherUiState.Loading(isManualRefresh = isPullToRefresh))
        scope.launch {
            val city = chosenCityInteractor.loadChosenCity()
            launchWeatherForecast(city)
        }
    }

    /**
     * Loads remote forecast for [cityModel].
     */
    private fun loadRemoteForecastForLocation(cityModel: CityLocationModel) {
        currentJob?.cancel()
        currentJob = scope.launch {
            val result = forecastInteractor.loadWeatherForLocation(
                cityModel.city,
                temperatureType,
                cityModel.location.latitude,
                cityModel.location.longitude
            )
            processServerResponse(cityModel, result)
        }
    }

    /**
     * Processes the server response and updates UI accordingly.
     * Handles remote, local, and error cases.
     *
     * @param cityModel data to provide weather for
     * @param result the result from the interactor
     */
    private fun processServerResponse(
        cityModel: CityLocationModel,
        result: LoadResult<CurrentWeather>
    ) {
        val processedResponse = weatherOutputMapper.mapToUi(
            cityModel = cityModel,
            loadResult = result,
        )
        processedResponse.let { response ->
            when (response.uiState) {
                is WeatherUiState.Success -> {
                    _forecastStateFlow.value = response.uiState
                }

                is WeatherUiState.Loading -> {
                    _forecastStateFlow.value = WeatherUiState.Loading()
                }

                is WeatherUiState.Error -> {
                    _forecastStateFlow.value = response.uiState
                }

                null -> {}
            }
            response.cityModelToSave?.let {
                scope.launch {
                    chosenCityInteractor.saveChosenCity(response.cityModelToSave)
                }
            }
            response.cityError?.let {
                _cityErrorEventFlow.tryEmit(it)
            }
        }
    }

    /**
     * Shows a downloading/loading status for a specific city.
     *
     * If [city] is blank, shows generic loading message.
     * Otherwise, formats message using [R.string.forecast_loading] with city name.
     *
     * @param city Name of the city being loaded
     */
    private fun showLoadingStatusFor(city: String) {
        if (city.isBlank()) {
            statusStateHolder.updateStatus(
                Info(
                    resourceManager.getString(R.string.forecast_downloading)
                )
            )
        } else {
            statusStateHolder.updateStatus(
                Info(
                    resourceManager.getString(
                        R.string.forecast_loading,
                        city
                    )
                )
            )
        }
    }

    private suspend fun loadSavedCity() {
        val savedModel = chosenCityInteractor.loadChosenCity()
        if (savedModel.city.isNotBlank()) {
            loggingService.logDebugEvent(
                TAG,
                "Loaded saved city from interactor: ${savedModel.city}"
            )
        } else {
            loggingService.logDebugEvent(TAG, "No saved city found in interactor")
        }
    }

    companion object {
        private const val TAG = "WeatherForecastViewModel"
    }
}