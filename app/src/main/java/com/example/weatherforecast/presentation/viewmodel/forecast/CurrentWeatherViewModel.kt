package com.example.weatherforecast.presentation.viewmodel.forecast

import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.preferences.PreferencesManager
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.CurrentWeatherInteractor
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.domain.CurrentWeather
import com.example.weatherforecast.models.domain.ForecastError
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.presentation.PresentationUtils.getWeatherTypeIcon
import com.example.weatherforecast.presentation.converter.WeatherDomainToUiConverter
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.utils.ResourceManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val forecastStateFlow: StateFlow<WeatherUiState>
        get() = _forecastStateFlow
    val chosenCityStateFlow: StateFlow<String>
        get() = _chosenCityStateFlow
    val chosenCityBlankStateFlow: SharedFlow<Unit>
        get() = _chosenCityBlankSharedFlow
    val chosenCityNotFoundStateFlow: SharedFlow<String>
        get() = _chosenCityNotFoundSharedFlow

    private val _chosenCityBlankSharedFlow = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )
    private val _forecastStateFlow = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    private val _chosenCityStateFlow = MutableStateFlow("")
    private val _chosenCityNotFoundSharedFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)

    //endregion flows

    private var chosenCity: String? = null
    private var currentJob: Job? = null

    private lateinit var temperatureType: TemperatureType

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        loggingService.logError(TAG, "Unexpected error in weather forecast loading", throwable)
        statusRenderer.showError(throwable.message.toString())
        showProgressBarState.value = false
    }

    init {
        viewModelScope.launch {
            preferencesManager.temperatureTypeStateFlow.collect { tempType ->
                loggingService.logDebugEvent(TAG, "Temperature unit changed: $tempType")
                temperatureType = tempType
            }
        }
    }

    /**
     * Launches weather forecast download using the provided [chosenCity].
     * If blank, attempts to load the last saved city from [chosenCityInteractor].
     *
     * @param chosenCity the city name selected by the user
     */
    fun launchWeatherForecast(chosenCity: String) {
        viewModelScope.launch(exceptionHandler) {
            val city = chosenCity.ifBlank {
                val cityModel = chosenCityInteractor.loadChosenCity()
                loggingService.logDebugEvent(
                    TAG,
                    "No chosen city from picker fragment. Loaded from database: " +
                            "city = ${cityModel.city}, lat = ${cityModel.location.latitude}, " +
                            "lon = ${cityModel.location.longitude}"
                )
                cityModel.city
            }
            loadWeatherForecast(city)
        }
    }

    /**
     * Loads weather forecast for the given [city], or emits a blank city event if empty.
     *
     * @param city the city name to load forecast for
     */
    private fun loadWeatherForecast(city: String) {
        if (city.isBlank()) {
            _chosenCityBlankSharedFlow.tryEmit(Unit)
        } else {
            chosenCity = city
            loadRemoteForecastForCity(city)
        }
    }

    /**
     * Initiates remote forecast loading for a specific [city].
     * Cancels any ongoing request to avoid multiple concurrent jobs.
     *
     * @param city the city name to fetch weather for
     */
    fun loadRemoteForecastForCity(city: String) {
        showProgressBarState.value = true
        if (currentJob?.isActive == true) return
        currentJob = viewModelScope.launch(exceptionHandler) {
            val result = forecastRemoteInteractor.loadWeatherForCity(
                temperatureType,
                city
            )
            processServerResponse(city, result)
        }
    }

    /**
     * Initiates remote forecast loading based on geographic [cityModel] location.
     *
     * @param cityModel contains city name and coordinates
     */
    fun loadRemoteForecastForLocation(cityModel: CityLocationModel) {
        showProgressBarState.value = true
        currentJob?.cancel()
        currentJob = viewModelScope.launch(exceptionHandler) {
            val result = forecastRemoteInteractor.loadWeatherForLocation(
                temperatureType,
                cityModel.location.latitude,
                cityModel.location.longitude
            )
            processServerResponse(cityModel.city, result)
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
                viewModelScope.launch {
                    showRemoteForecast(result.data.copy(city = city))
                    val location = getLocation(result)
                    chosenCityInteractor.saveChosenCity(
                        city = result.data.city,
                        location = location
                    )
                }
            }

            is LoadResult.Local -> {
                statusRenderer.showWarning(
                    resourceManager.getString(
                        R.string.forecast_for_city_outdated, city
                    )
                )
                showLocalForecast(result.data.copy(city = city))
            }

            is LoadResult.Error -> {
                when (result.error) {
                    is ForecastError.NoInternet -> statusRenderer.showError(
                        resourceManager.getString(
                            R.string.disconnected
                        )
                    )

                    is ForecastError.CityNotFound -> {
                        statusRenderer.showWarning(
                            resourceManager.getString(
                                R.string.no_selected_city_forecast,
                                city
                            )
                        )
                        _chosenCityNotFoundSharedFlow.tryEmit(city)
                    }

                    else -> {
                        loggingService.logError(
                            TAG,
                            "Error loading forecast for city: $city",
                            Exception(result.error.toString())
                        )
                        statusRenderer.showError(
                            resourceManager.getString(R.string.forecast_for_city_error)
                        )
                    }
                }
            }
        }
    }

    private fun getLocation(result: LoadResult.Remote<CurrentWeather>): Location {
        return Location(LocationManager.NETWORK_PROVIDER).apply {
            latitude = result.data.coordinate.latitude
            longitude = result.data.coordinate.longitude
        }
    }

    private fun showRemoteForecast(forecastModel: CurrentWeather) {
        _forecastStateFlow.value = WeatherUiState.Success(
            getUiModel(forecastModel),
            DataSource.REMOTE
        )
    }

    private fun showLocalForecast(forecastModel: CurrentWeather) {
        _forecastStateFlow.value = WeatherUiState.Success(
            getUiModel(forecastModel),
            DataSource.LOCAL
        )
    }

    private fun getUiModel(forecastModel: CurrentWeather) =
        weatherDomainToUiConverter.convert(
            model = forecastModel,
            defaultErrorMessage = resourceManager.getString(R.string.bad_date_format),
            getWeatherIconId = { weatherIconId ->
                getWeatherTypeIcon(weatherIconId)
            }
        )

    companion object {
        private const val TAG = "WeatherForecastViewModel"
    }
}