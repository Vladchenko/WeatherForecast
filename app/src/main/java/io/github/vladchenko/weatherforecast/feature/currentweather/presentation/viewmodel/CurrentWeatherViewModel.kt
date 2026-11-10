package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.domain.model.Coordinate
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.network.NetworkStateHolder
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.ui.event.CityErrorEventBus
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityRepository
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.CurrentWeatherInteractor
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.outputmapper.WeatherOutputMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.models.CurrentWeatherUi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing UI and business logic of the current weather forecast.
 *
 * This ViewModel manages the full lifecycle of weather data:
 * - Loading weather data from remote API with fallback to local cache
 * - Managing UI state transitions (Loading → Success/Error)
 * - Processing city selection and saving
 * - Handling various error scenarios (network, API, local data corruption)
 * - Supporting temperature unit preferences (Celsius/Fahrenheit)
 *
 * The ViewModel implements a "remote first, fallback cache" strategy:
 * 1. Attempts to fetch fresh data from the remote API
 * 2. On remote fetch failure, loads cached data and shows a warning
 * 3. On both methods failing, displays the appropriate error message
 *
 * @property loggingService centralized service for structured application logging
 * @property cityErrorEventBus unified event bus for transmitting city-related errors
 * @property statusStateHolder manages and transmits UI status messages (info, warnings, errors)
 * @property networkStateHolder manages network connectivity (connected or disconnected)
 * @property preferencesManager manages user preferences (e.g., temperature unit)
 * @property weatherOutputMapper processes raw weather data load results and renders them into UI-ready structure
 * @property chosenCityRepository handles saving and retrieving the chosen city
 * @property forecastInteractor loads weather data from remote API with fallback to local cache
 */
@HiltViewModel
class CurrentWeatherViewModel @Inject constructor(
    private val loggingService: LoggingService,
    private val cityErrorEventBus: CityErrorEventBus,
    private val statusStateHolder: StatusStateHolder,
    private val networkStateHolder: NetworkStateHolder,
    private val preferencesManager: PreferencesManager,
    private val weatherOutputMapper: WeatherOutputMapper,
    private val chosenCityRepository: ChosenCityRepository,
    private val forecastInteractor: CurrentWeatherInteractor,
) : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        loggingService.logError(TAG, "Unexpected error in weather forecast loading", throwable)
        statusStateHolder.updateErrorStatus(throwable.message.toString())
    }

    //region flows
    private val scope = CoroutineScope(viewModelScope.coroutineContext + exceptionHandler)

    private val _weatherStateFlow =
        MutableStateFlow<WeatherUiState<CurrentWeatherUi>>(WeatherUiState.Loading(false))

    /**
     * Public read-only flow that emits the current UI state of the weather forecast.
     * Observers receive updates as [WeatherUiState.Loading], [WeatherUiState.Success], or error states.
     */
    val weatherStateFlow: StateFlow<WeatherUiState<CurrentWeatherUi>> = _weatherStateFlow
        .stateIn(
            scope, SharingStarted.WhileSubscribed(5000L),
            WeatherUiState.Loading(false)
        )

    //endregion flows

    private var currentJob: Job? = null

    init {
        scope.launch {
            networkStateHolder.networkStateFlow.drop(1).collect { state ->
                when (state) {
                    false -> {} // Do nothing
                    true -> {
                        refreshWeather(false)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        scope.cancel()
    }

    /**
     * Launches weather forecast download, using the provided [cityModel].
     * If blank, attempts to load the last saved city from [chosenCityRepository].
     *
     * @param cityModel to provide a weather for
     */
    fun launchWeatherForecast(cityModel: CityLocationModel) {
        currentJob?.cancel()
        currentJob = scope.launch {
            showLoadingStatusFor(cityModel.city)
            val model = if (cityModel.city.isBlank()) {
                val savedModel = chosenCityRepository.loadChosenCity()
                if (savedModel.city.isBlank()) {
                    cityErrorEventBus.send(CityErrorEvent.CityBlank)
                    return@launch
                }
                savedModel
            } else {
                cityModel
            }
            loadForecastForLocation(model)
        }
    }

    /**
     * Refreshes weather data using the currently saved city.
     * Reuses the last known city location to fetch updated weather information.
     *
     * If [isPullToRefresh] is true, sets the refreshing state to true to indicate
     * a manual pull-to-refresh action (UI can show refresh indicator).
     *
     * @param isPullToRefresh true when triggered by user pull-to-refresh gesture
     */
    fun refreshWeather(isPullToRefresh: Boolean) {
        _weatherStateFlow.tryEmit(WeatherUiState.Loading(isManualRefresh = isPullToRefresh))
        scope.launch {
            val city = chosenCityRepository.loadChosenCity()
            launchWeatherForecast(city)
        }
    }

    /**
     * Loads remote forecast for [cityModel].
     */
    private suspend fun loadForecastForLocation(cityModel: CityLocationModel) {
        val result = forecastInteractor.loadWeatherForLocation(
            cityModel.city,
            cityModel.coordinate.latitude,
            cityModel.coordinate.longitude,
            preferencesManager.temperatureUnit.value
        )
        processServerResponse(cityModel, result)
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
                    _weatherStateFlow.value = response.uiState
                    scope.launch {
                        saveCity(cityModel, response.uiState)
                    }
                }

                is WeatherUiState.Loading -> {
                    _weatherStateFlow.value = WeatherUiState.Loading()
                }

                is WeatherUiState.Error -> {
                    _weatherStateFlow.value = response.uiState
                }

                WeatherUiState.Idle -> {
                    _weatherStateFlow.value = WeatherUiState.Idle
                }

                null -> {}
            }
        }
    }

    private suspend fun saveCity(
        cityModel: CityLocationModel,
        uiState: WeatherUiState.Success<CurrentWeatherUi>
    ) {
        chosenCityRepository.saveChosenCity(
            CityLocationModel(
                cityModel.city,
                Coordinate(
                    uiState.data.coordinate.latitude,
                    uiState.data.coordinate.longitude
                )
            )
        )
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
            statusStateHolder.updateInfoStatus(R.string.forecast_downloading)
        } else {
            statusStateHolder.updateInfoStatus(
                R.string.forecast_loading,
                city
            )
        }
    }

    companion object {
        private const val TAG = "WeatherForecastViewModel"
    }
}