package com.example.weatherforecast.presentation.viewmodel.forecast

import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.preferences.PreferencesManager
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
 * @param connectivityObserver internet connectivity observer
 * @property temperatureType type of temperature
 * @property resourceManager to get android specific resources
 * @property coroutineDispatchers for coroutines
 * @property chosenCityInteractor city chosen by user persistence interactor
 * @property forecastRemoteInteractor remote forecast interactor
 */
@HiltViewModel
class CurrentWeatherViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    private val resourceManager: ResourceManager,
    private val preferencesManager: PreferencesManager,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val forecastRemoteInteractor: CurrentWeatherInteractor,
    private val weatherDomainToUiConverter: WeatherDomainToUiConverter,
) : AbstractViewModel(connectivityObserver, coroutineDispatchers) {

    //region flows
    val forecastStateFlow: StateFlow<WeatherUiState>
        get() = _forecastStateFlow
    val chosenCityStateFlow: StateFlow<String>
        get() = _chosenCityStateFlow
    val chosenCityBlankStateFlow: SharedFlow<Unit>
        get() = _chosenCityBlankSharedFlow
    val chosenCityNotFoundStateFlow: SharedFlow<String>
        get() = _chosenCityNotFoundSharedFlow
    val gotoCitySelectionStateFlow: SharedFlow<Unit>
        get() = _gotoCitySelectionSharedFlow

    private val _chosenCityBlankSharedFlow = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1 // Collector is not alive when flow emits value, so buffer is needed
    )
    private val _forecastStateFlow = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    private val _chosenCityStateFlow = MutableStateFlow("")
    private val _chosenCityNotFoundSharedFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val _gotoCitySelectionSharedFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    //endregion flows

    private var chosenCity: String? = null
    private var currentJob: Job? = null

    private lateinit var temperatureType: TemperatureType

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, throwable.stackTraceToString())
        showError(throwable.message.toString())
        showProgressBarState.value = false
    }

    init {
        viewModelScope.launch {
            preferencesManager.temperatureTypeStateFlow.collect { tempType ->
                Log.d(TAG, "Temperature unit changed: $tempType")
                temperatureType = tempType
            }
        }
    }

    /**
     * Go to city selection screen.
     */
    fun gotoCitySelection() {
        _gotoCitySelectionSharedFlow.tryEmit(Unit)
    }

    /**
     * Launch weather forecast downloading, having a [chosenCity] provided from a city picker fragment.
     */
    fun launchWeatherForecast(chosenCity: String) {
        viewModelScope.launch(exceptionHandler) {
            // If chosenCity is blank, then return cityModel.city
            val city = chosenCity.ifBlank {
                val cityModel = chosenCityInteractor.loadChosenCity()
                Log.d(
                    TAG,
                    "No chosen city from picker fragment. Downloaded from database: city = ${cityModel.city}, " +
                            "lat = ${cityModel.location.latitude}, lon = ${cityModel.location.longitude}"
                )
                cityModel.city
            }
            loadWeatherForecast(city)
        }
    }

    /** Update a state of a chosen city with [city]. */
    fun updateChosenCityState(city: String) {
        _chosenCityStateFlow.value = city
    }

    /**
     * Download a forecast or call blank chosen city callback, depending on a presence of a [city]
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
     * Download weather forecast on a [city].
     */
    fun loadRemoteForecastForCity(city: String) {
        showProgressBarState.value = true
        if (currentJob?.isActive == true) return    // Return if there is a job already running
        currentJob = viewModelScope.launch(exceptionHandler) {
            val result = forecastRemoteInteractor.loadWeatherForCity(
                temperatureType,
                city
            )
            processServerResponse(city, result)
        }
    }

    /**
     * Download remote weather forecast on a [cityModel] for location.
     */
    fun loadRemoteForecastForLocation(cityModel: CityLocationModel) {
        showProgressBarState.value = true
        currentJob?.cancel() // Cancel previous job
        currentJob = viewModelScope.launch(exceptionHandler) {
            val result = forecastRemoteInteractor.loadWeatherForLocation(
                temperatureType,
                cityModel.location.latitude,
                cityModel.location.longitude
            )
            processServerResponse(cityModel.city, result)
        }
    }

    private fun processServerResponse(
        city: String,
        result: LoadResult<CurrentWeather>
    ) {
        showProgressBarState.value = false
        when (result) {
            is LoadResult.Remote -> {
                viewModelScope.launch {
                    // City in response is different to city in request
                    showRemoteForecast(result.data.copy(city = city))
                    val location = getLocation(result)
                    chosenCityInteractor.saveChosenCity(
                        city = result.data.city,
                        location = location
                    )
                }
            }

            is LoadResult.Local -> {
                showWarning(
                    resourceManager.getString(
                        R.string.forecast_for_city_outdated, city
                    )
                )
                // City in response is different to city in request
                showLocalForecast(result.data.copy(city = city))
            }

            is LoadResult.Error -> {
                when (result.error) {
                    is ForecastError.NoInternet -> showError(R.string.disconnected)
                    is ForecastError.CityNotFound -> {
                        showWarning(
                            resourceManager.getString(
                                R.string.no_selected_city_forecast,
                                city
                            )
                        )
                        // TODO Call loadWeatherForLocation and if it fails, call _chosenCityNotFoundFlow.tryEmit(city)
                        // TODO Maybe call it in repository
                        _chosenCityNotFoundSharedFlow.tryEmit(city)
                    }

                    else -> showError(resourceManager.getString(R.string.forecast_for_city_error))
                }
            }
        }
    }

    private fun getLocation(result: LoadResult.Remote<CurrentWeather>): Location {
        val location = Location(LocationManager.NETWORK_PROVIDER)
        location.latitude = result.data.coordinate.latitude
        location.longitude = result.data.coordinate.longitude
        return location
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