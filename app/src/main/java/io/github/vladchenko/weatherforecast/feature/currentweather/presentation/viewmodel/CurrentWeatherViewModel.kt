package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel

import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.model.TemperatureType
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.toWeatherIconRes
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.feature.currentweather.domain.CurrentWeatherInteractor
import io.github.vladchenko.weatherforecast.feature.currentweather.domain.models.CurrentWeather
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.models.CurrentWeatherUi
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.converter.WeatherDomainToUiConverter
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.presentation.viewmodel.AbstractViewModel
import io.github.vladchenko.weatherforecast.core.ui.state.DataSource
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for weather forecast downloading.
 *
 * This ViewModel handles:
 * - Loading current weather data from remote or local sources
 * - Managing UI state via [forecastStateFlow]
 * - Persisting chosen city and its coordinates
 * - Handling errors and showing appropriate messages
 * - Using structured logging via [LoggingService] instead of direct [android.util.Log]
 *
 * @property connectivityObserver observes internet connectivity state
 * @property statusRenderer Displays loading, success, warning, or error statuses
 * @property loggingService centralized service for application logging
 * @property resourceManager provides access to Android resources (strings, etc.)
 * @property preferencesManager manages user preferences (e.g. temperature unit)
 * @property coroutineDispatchers configures dispatchers for coroutines
 * @property chosenCityInteractor handles persistence of the selected city
 * @property forecastRemoteInteractor loads current weather data
 * @property weatherDomainToUiConverter converts domain models to UI models
 */
@HiltViewModel
class CurrentWeatherViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    private val statusRenderer: StatusRenderer,
    private val loggingService: LoggingService,
    private val resourceManager: ResourceManager,
    private val preferencesManager: PreferencesManager,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val forecastRemoteInteractor: CurrentWeatherInteractor,
    private val weatherDomainToUiConverter: WeatherDomainToUiConverter,
) : AbstractViewModel(connectivityObserver) {

    //region flows
    /**
     * Emits true when a refresh operation is in progress, false otherwise.
     * Can be observed to show/hide swipe-to-refresh indicators.
     */
    val refreshingStateFlow: StateFlow<Boolean>
        get() = _refreshingStateFlow

    /**
     * Public read-only flow that emits the current UI state of the weather forecast.
     * Observers receive updates as [WeatherUiState.Loading], [WeatherUiState.Success], or error states.
     */
    val forecastStateFlow: StateFlow<WeatherUiState<CurrentWeatherUi>>
        get() = _forecastStateFlow

    /**
     * Public read-only flow that emits the currently selected city with its coordinates.
     * Returns null if no city has been selected yet.
     */
    val chosenCityStateFlow: StateFlow<CityLocationModel?>
        get() = _chosenCityStateFlow

    /**
     * SharedFlow that emits a unit event when the user attempts to load forecast
     * with an empty city name and no saved city exists.
     * Used to trigger UI actions like showing a dialog or navigating to city selection.
     */
    val chosenCityBlankStateFlow: SharedFlow<Unit>
        get() = _chosenCityBlankSharedFlow

    /**
     * SharedFlow that emits the name of the city when it was not found during forecast loading.
     * Used to display a warning message or prompt the user to check the city name.
     */
    val chosenCityNotFoundStateFlow: SharedFlow<String>
        get() = _chosenCityNotFoundSharedFlow
    private val _chosenCityBlankSharedFlow = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )
    private val _forecastStateFlow =
        MutableStateFlow<WeatherUiState<CurrentWeatherUi>>(WeatherUiState.Loading)
    private val _chosenCityStateFlow = MutableStateFlow<CityLocationModel?>(null)
    private val _chosenCityNotFoundSharedFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val _refreshingStateFlow = MutableStateFlow(false)
    //endregion flows

    private var currentJob: Job? = null

    private lateinit var temperatureType: TemperatureType

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        loggingService.logError(TAG, "Unexpected error in weather forecast loading", throwable)
        statusRenderer.showError(throwable.message.toString())
        showProgressBarState.value = false
    }

    init {
        viewModelScope.launch(exceptionHandler) {
            loadSavedCity()
        }
        viewModelScope.launch {
            preferencesManager.temperatureTypeStateFlow.collect { tempType ->
                loggingService.logDebugEvent(TAG, "Temperature unit changed: $tempType")
                temperatureType = tempType
            }
        }
    }

    /**
     * Launches weather forecast download using the provided [city].
     * If blank, attempts to load the last saved city from [chosenCityInteractor].
     *
     * @param city the city name selected by the user
     * @param latitude latitude of the chosen city
     * @param longitude longitude of the chosen city
     */
    fun launchWeatherForecast(city: String, latitude: String, longitude: String) {
        viewModelScope.launch(exceptionHandler) {
            statusRenderer.showLoadingStatusFor(city)
            val (cityName, latValue, lonValue) = if (city.isBlank()) {
                val savedModel = chosenCityInteractor.loadChosenCity()
                if (savedModel.city.isBlank()) {
                    _chosenCityBlankSharedFlow.tryEmit(Unit)
                    _refreshingStateFlow.value = false
                    return@launch
                }
                Triple(
                    savedModel.city,
                    savedModel.location.latitude,
                    savedModel.location.longitude
                )
            } else {
                val lat = latitude.toDoubleOrNull() ?: run {
                    statusRenderer.showError("Invalid latitude")
                    _refreshingStateFlow.value = false
                    return@launch
                }
                val lon = longitude.toDoubleOrNull() ?: run {
                    statusRenderer.showError("Invalid longitude")
                    _refreshingStateFlow.value = false
                    return@launch
                }
                Triple(city, lat, lon)
            }

            loadRemoteForecastForLocation(cityName, latValue.toString(), lonValue.toString())
        }
    }

    /**
     * Starts weather forecast loading triggered by pull-to-refresh.
     * Sets the refreshing state before launching the actual forecast load.
     */
    fun launchWeatherForecastFromPullToRefresh(city: String, latitude: String, longitude: String) {
        _refreshingStateFlow.value = true
        viewModelScope.launch(exceptionHandler) {
            launchWeatherForecast(city, latitude, longitude)
        }
    }

    /**
     * Loads remote forecast for location - [latitude] and [longitude] of [city].
     */
    private fun loadRemoteForecastForLocation(city: String, latitude: String, longitude: String) {
        showProgressBarState.value = true
        currentJob?.cancel()
        currentJob = viewModelScope.launch(exceptionHandler) {
            val result = forecastRemoteInteractor.loadWeatherForLocation(
                city,
                temperatureType,
                latitude.toDouble(),
                longitude.toDouble()
            )
            processServerResponse(city, result)
        }
    }

    /**
     * Processes the server response and updates UI accordingly.
     * Handles remote, local, and error cases.
     *
     * @param city the requested city name
     * @param result the result from the interactor
     */
    private fun processServerResponse(
        city: String,
        result: LoadResult<CurrentWeather>
    ) {
        showProgressBarState.value = false
        when (result) {
            is LoadResult.Remote -> {
                viewModelScope.launch(exceptionHandler) {
                    statusRenderer.showSuccessStatusFor(city)
                    showRemoteForecast(result.data.copy(city = city))
                    val cityLocationModel = CityLocationModel(city, createLocation(result))
                    chosenCityInteractor.saveChosenCity(cityLocationModel)
                    _chosenCityStateFlow.tryEmit(cityLocationModel)
                    loggingService.logDebugEvent(
                        TAG,
                        "Chosen city saved to database: $city"
                    )
                    _refreshingStateFlow.value = false
                }
            }

            is LoadResult.Local -> {
                statusRenderer.showWarning(
                    resourceManager.getString(
                        R.string.forecast_outdated, city
                    )
                )
                showLocalForecast(result.data.copy(city = city))
                _refreshingStateFlow.value = false
            }

            is LoadResult.Error -> {
                _refreshingStateFlow.value = false
                when (val error = result.error) {
                    is ForecastError.ApiKeyInvalid -> {
                        showError(city, resourceManager.getString(R.string.api_key_invalid))
                    }

                    is ForecastError.CityNotFound -> {
                        statusRenderer.showWarning(
                            resourceManager.getString(R.string.city_not_found, error.city)
                        )
                        _chosenCityNotFoundSharedFlow.tryEmit(error.city)
                    }

                    is ForecastError.LocalDataCorrupted -> {
                        showError(city, resourceManager.getString(R.string.local_data_corrupted))
                    }

                    is ForecastError.NetworkError -> when (error.type) {
                        ForecastError.NetworkError.Type.ConnectionFailed ->
                            showError(city, resourceManager.getString(R.string.connection_refused))

                        ForecastError.NetworkError.Type.NoInternet ->
                            showError(
                                city,
                                resourceManager.getString(R.string.network_disconnected)
                            )

                        ForecastError.NetworkError.Type.Timeout ->
                            showError(city, resourceManager.getString(R.string.request_timeout))

                        ForecastError.NetworkError.Type.SecurityError ->
                            showError(city, resourceManager.getString(R.string.ssl_error))

                        else ->
                            showError(
                                city,
                                resourceManager.getString(R.string.network_error_generic)
                            )
                    }

                    is ForecastError.NoDataAvailable -> {
                        showError(
                            city,
                            resourceManager.getString(R.string.no_weather_data_available)
                        )
                    }

                    is ForecastError.UncategorizedError -> {
                        if (error.cause != null) {
                            loggingService.logError(TAG, "Unexpected error", error.cause)
                        } else {
                            loggingService.logError(TAG, "Unexpected error: ${error.message}")
                        }
                        showError(city, resourceManager.getString(R.string.unexpected_error))
                    }
                }
            }
        }
    }

    private fun showError(city: String, errorMessage: String) {
        statusRenderer.showError(errorMessage)
        _forecastStateFlow.value = WeatherUiState.Error(city, errorMessage)
    }

    private fun createLocation(result: LoadResult.Remote<CurrentWeather>): Location {
        return Location(LocationManager.NETWORK_PROVIDER).apply {
            latitude = result.data.coordinate.latitude
            longitude = result.data.coordinate.longitude
        }
    }

    private fun showRemoteForecast(forecastModel: CurrentWeather) {
        _forecastStateFlow.value = WeatherUiState.Success(
            toUiModel(forecastModel),
            DataSource.REMOTE
        )
    }

    private fun showLocalForecast(forecastModel: CurrentWeather) {
        _forecastStateFlow.value = WeatherUiState.Success(
            toUiModel(forecastModel),
            DataSource.LOCAL
        )
    }

    private fun toUiModel(forecastModel: CurrentWeather) =
        weatherDomainToUiConverter.convert(
            model = forecastModel,
            defaultErrorMessage = resourceManager.getString(R.string.bad_date_format),
            toWeatherIconRes = { weatherIconId ->
                toWeatherIconRes(weatherIconId)
            }
        )

    private suspend fun loadSavedCity() {
        val savedModel = chosenCityInteractor.loadChosenCity()
        if (savedModel.city.isNotBlank()) {
            _chosenCityStateFlow.value = savedModel
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