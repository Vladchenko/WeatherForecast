package com.example.weatherforecast.presentation.viewmodel.forecast

import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.api.customexceptions.CityNotFoundException
import com.example.weatherforecast.data.api.customexceptions.NetworkTimeoutException
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.data.preferences.PreferencesManager
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityInteractor
import com.example.weatherforecast.domain.forecast.CurrentWeatherLocalInteractor
import com.example.weatherforecast.domain.forecast.CurrentWeatherRemoteInteractor
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.domain.CurrentWeather
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
 * @property forecastLocalInteractor local forecast interactor
 * @property forecastRemoteInteractor remote forecast interactor
 */
@HiltViewModel
class CurrentWeatherViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    private val resourceManager: ResourceManager,
    private val preferencesManager: PreferencesManager,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val forecastLocalInteractor: CurrentWeatherLocalInteractor,
    private val forecastRemoteInteractor: CurrentWeatherRemoteInteractor,
    private val weatherDomainToUiConverter: WeatherDomainToUiConverter,
) : AbstractViewModel(connectivityObserver, coroutineDispatchers) {

    //region flows
    val forecastState: StateFlow<WeatherUiState>
        get() = _forecastState
    val chosenCityFlow: StateFlow<String>
        get() = _chosenCityStateFlow
    val chosenCityBlankFlow: SharedFlow<Unit>
        get() = _chosenCityBlankFlow
    val chosenCityNotFoundFlow: SharedFlow<String>
        get() = _chosenCityNotFoundFlow
    val gotoCitySelectionFlow: SharedFlow<Unit>
        get() = _gotoCitySelectionFlow

    private val _chosenCityBlankFlow = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1 // Collector is not alive when flow emits value, so buffer is needed
    )
    private val _forecastState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    private val _chosenCityStateFlow = MutableStateFlow("")
    private val _chosenCityNotFoundFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val _gotoCitySelectionFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    //endregion flows

    private var chosenCity: String? = null
    private var currentJob: Job? = null

    private lateinit var temperatureType: TemperatureType

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, throwable.message.orEmpty())
        when (throwable) {
            is CityNotFoundException -> {
                _chosenCityNotFoundFlow.tryEmit(throwable.city)
            }

            is NoInternetException -> {
                showError(throwable.message.toString())
                downloadLocalForecastForCity(
                    chosenCity.orEmpty(),   // TODO Is it ok that orEmpty ?
                    throwable.message.toString()
                )
            }

            is NetworkTimeoutException -> {
                showError(throwable.message.toString())
                // Try to get local data as fallback
                downloadLocalForecastForCity(
                    chosenCity.orEmpty(),
                    throwable.message.toString()
                )
            }

            is NoSuchDatabaseEntryException -> {
                showError(
                    R.string.database_forecast_for_city_not_found,
                    chosenCity.orEmpty()
                )
            }

            is NumberFormatException -> {
                showError(throwable.message.orEmpty())
            }

            else -> {
                Log.e(TAG, throwable.message.orEmpty())
                Log.e(TAG, throwable.stackTraceToString())
                showError(throwable.message.toString())
            }
        }
        showProgressBarState.value = false
        throwable.stackTrace.forEach {
            Log.e(TAG, it.toString())
        }
    }

    init {
        viewModelScope.launch {
            preferencesManager.temperatureType.collect { tempType ->
                Log.d(TAG, "Temperature unit changed: $tempType")
                temperatureType = tempType
            }
        }
    }

    /**
     * Go to city selection screen.
     */
    fun gotoCitySelection() {
        _gotoCitySelectionFlow.tryEmit(Unit)
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
            downloadWeatherForecast(city)
        }
    }

    /** Update a state of a chosen city with [city]. */
    fun updateChosenCityState(city: String) {
        _chosenCityStateFlow.value = city
    }

    /**
     * Download a forecast or call blank chosen city callback, depending on a presence of a [city]
     */
    private fun downloadWeatherForecast(city: String) {
        if (city.isBlank()) {
            _chosenCityBlankFlow.tryEmit(Unit)
        } else {
            chosenCity = city
            downloadRemoteForecastForCity(city)
        }
    }

    /**
     * Download weather forecast on a [city].
     */
    fun downloadRemoteForecastForCity(city: String) {
        showProgressBarState.value = true
        if (currentJob?.isActive == true) return    // Return if there is a job already running
        currentJob = viewModelScope.launch(exceptionHandler) {
            val result = forecastRemoteInteractor.loadForecastForCity(
                temperatureType,
                city
            )
            processServerResponse(result)
        }
    }

    /**
     * Download weather forecast on a [city], providing a [error] on why remote request failed.
     */
    private fun downloadLocalForecastForCity(city: String, error: String) {
        showProgressBarState.value = true
        viewModelScope.launch(exceptionHandler) {
            val result = forecastLocalInteractor.loadForecast(
                city, temperatureType, error
            )
            processServerResponse(result)
        }
    }

    /**
     * Download remote weather forecast on a [cityModel] for location.
     */
    fun downloadRemoteForecastForLocation(cityModel: CityLocationModel) {
        showProgressBarState.value = true
        currentJob?.cancel() // Отменяем предыдущий запрос
        currentJob = viewModelScope.launch(exceptionHandler) {
            val result = forecastRemoteInteractor.loadForecastForLocation(
                temperatureType,
                cityModel.location.latitude,
                cityModel.location.longitude
            )
            processServerResponseForLocation(cityModel.city, result)
        }
    }

    private fun processServerResponseForLocation(
        city: String,
        result: LoadResult<CurrentWeather>
    ) {
        showProgressBarState.value = false
        when (result) {
            is LoadResult.Remote -> {
                // City in response is different than city in request
                showRemoteForecast(result.data.copy(city = city))
            }

            is LoadResult.Local -> {
                // City in response is different than city in request
                showLocalForecast(result.data.copy(city = city))
            }

            is LoadResult.Error -> {
                showError(result.exception.message.toString())
            }
        }
    }

    private fun processServerResponse(result: LoadResult<CurrentWeather>) {
        showProgressBarState.value = false
        when (result) {
            is LoadResult.Remote -> {
                viewModelScope.launch {
                    showRemoteForecast(result.data)
                    val location = getLocation(result)
                    chosenCityInteractor.saveChosenCity(
                        city = result.data.city,
                        location = location
                    )
                }
            }

            is LoadResult.Local -> {
                showLocalForecast(result.data)
            }

            is LoadResult.Error -> {
                showError(result.exception.message.toString())
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
        _forecastState.value = WeatherUiState.Success(
            getUiModel(forecastModel),
            DataSource.REMOTE
        )
    }

    private fun showLocalForecast(forecastModel: CurrentWeather) {
        _forecastState.value = WeatherUiState.Success(
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