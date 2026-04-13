package com.example.weatherforecast.presentation.viewmodel.cityselection

import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.domain.recentcities.RecentCitiesInteractor
import com.example.weatherforecast.models.domain.CitiesNames
import com.example.weatherforecast.models.domain.CityDomainModel
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.models.domain.RecentCities
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.DataSource
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherUiState
import com.example.weatherforecast.utils.ResourceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing UI state and business logic in the city selection screen.
 *
 * ## Responsibilities
 * - Manages user input for city name search with debounced query processing
 * - Loads and filters available city names based on user input
 * - Handles navigation events (back, select city)
 * - Displays status messages (errors, loading states) via [StatusRenderer]
 * - Responds to network connectivity changes inherited from [AbstractViewModel]
 *
 * ## State Flows
 * - [_cityMaskStateFlow]: Tracks current text input in the search field
 * - [_cityPredictions]: Holds filtered list of cities matching the query
 * - [_navigationEventFlow]: Emits navigation commands to the UI layer
 *
 * ## Event Handling
 * Uses a sealed class [CitySelectionEvent] to handle all user interactions in a unidirectional data flow manner.
 * This ensures predictable state changes and simplifies testing.
 *
 * ## Search Behavior
 * Implements debounce (1 second) on user input to avoid excessive database or API calls.
 * Only triggers search when:
 * - Input is not blank
 * - Value has changed since last emission
 *
 * ## Error Handling
 * Uses [CoroutineExceptionHandler] to catch and process exceptions during city lookup:
 * - [NoSuchDatabaseEntryException]: Shows localized error about missing default city
 * - Other exceptions: Display raw message via [StatusRenderer]
 *
 * ## Thread Safety
 * All coroutines are launched in [viewModelScope], ensuring automatic cancellation on ViewModel destruction.
 * State updates occur on the main thread, safe for UI observation.
 *
 * @param connectivityObserver Monitors network state; inherited base functionality from [AbstractViewModel]
 * @property loggingService Logs errors and debug information
 * @property statusRenderer Displays status messages to the user
 * @property resourceManager Provides access to string resources for dynamic UI content
 * @property citiesNamesInteractor Business logic layer for loading and filtering city names
 * @property recentCitiesInteractor Operates recently used cities
 */
@FlowPreview
@HiltViewModel
class CitiesNamesViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    private val loggingService: LoggingService,
    private val statusRenderer: StatusRenderer,
    private val resourceManager: ResourceManager,
    private val citiesNamesInteractor: CitiesNamesInteractor,
    private val recentCitiesInteractor: RecentCitiesInteractor
) : AbstractViewModel(connectivityObserver) {

    /**
     * Flow of navigation events triggered by user actions.
     *
     * Emits one-time events such as:
     * - Navigating back up
     * - Selecting a specific city to view its weather
     *
     * Consumers should collect this flow and trigger corresponding navigation actions.
     * Events are nullable to allow resetting state if needed.
     */
    val navigationEventFlow: SharedFlow<CityNavigationEvent>
        get() = _navigationEventFlow

    /**
     * State flow representing the current user input in the city search field.
     *
     * Updated via [onEvent] with [CitySelectionEvent.UpdateQuery].
     * Used to trigger debounced city name lookups.
     */
    val cityMaskStateFlow: StateFlow<String>
        get() = _cityMaskStateFlow

    /**
     * State flow holding the result of the latest city name search.
     *
     * Contains a [CitiesNames] object with:
     * - List of cities matching the current query
     * - Optional error message from data layer
     *
     * Null until first successful search.
     */
    val cityPredictions: StateFlow<WeatherUiState<List<CityDomainModel>>?>
        get() = _cityPredictions

    /**
     * State flow that emits the current state of recently used cities.
     *
     * Holds a [WeatherUiState] object wrapping the result of loading recent cities, which can be:
     * - [WeatherUiState.Success] with a [LoadResult<RecentCities>] containing the list of recent cities
     * - [WeatherUiState.Loading] during data retrieval (not actively set here, but conceptually possible)
     * - [WeatherUiState.Error] if an exception occurs during loading
     *
     * The data is loaded from the local database via [RecentCitiesInteractor] when [CitySelectionEvent.LoadRecentCities]
     * is triggered (e.g., on screen start or refresh).
     *
     * Consumers should observe this flow to display the list of recently searched cities in the UI,
     * typically shown when the search query is empty.
     */
    val recentCitiesNamesFlow: StateFlow<WeatherUiState<RecentCities>?>
        get() = _recentCitiesNamesFlow

    private val _cityMaskStateFlow = MutableStateFlow("")
    private val _cityPredictions = MutableStateFlow<WeatherUiState<List<CityDomainModel>>?>(null)
    private val _navigationEventFlow = MutableSharedFlow<CityNavigationEvent>()
    private val _recentCitiesNamesFlow = MutableStateFlow<WeatherUiState<RecentCities>?>(null)

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        loggingService.logError(TAG, throwable.message.orEmpty(), throwable)
        when (throwable) {
            is NoSuchDatabaseEntryException -> {
                statusRenderer.showError(resourceManager.getString(R.string.forecast_default_city_absent))
            }

            else -> {
                statusRenderer.showError(throwable.message.toString())
            }
        }
    }

    init {
        startDebouncedSearch()
        statusRenderer.showCitySelectionStatus()
    }

    /**
     * Starts observing user input with debounce and filtering.
     *
     * Launches a coroutine that:
     * - Waits 1 second after each input change
     * - Ignores blank inputs
     * - Skips duplicate values
     * - Triggers city fetch for valid queries
     *
     * Ensures efficient use of resources by minimizing unnecessary lookups.
     */
    private fun startDebouncedSearch() {
        viewModelScope.launch(exceptionHandler) {
            _cityMaskStateFlow
                .debounce(1000)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collect { query ->
                    _cityPredictions.value = WeatherUiState.Loading
                    fetchCities(query)
                }
        }
    }

    /**
     * Processes incoming UI events and updates state accordingly.
     *
     * Dispatches behavior based on event type:
     * - [CitySelectionEvent.NavigateUp]: Sends navigation up command
     * - [CitySelectionEvent.SelectCity]: Navigates to weather screen for selected city and adds it to recents
     * - [CitySelectionEvent.ClearQuery]: Resets search query and clears results
     * - [CitySelectionEvent.UpdateQuery]: Updates search mask and triggers debounced search
     *
     * @param event The user action to process
     */
    fun onEvent(event: CitySelectionEvent) {
        when (event) {
            is CitySelectionEvent.NavigateUp -> sendNavigationEvent(CityNavigationEvent.NavigateUp)
            is CitySelectionEvent.SelectCity -> {
                sendNavigationEvent(
                    CityNavigationEvent.OpenWeatherFor(
                        event.city
                    )
                )
                viewModelScope.launch {
                    recentCitiesInteractor.addCityToRecents(event.city)
                }
            }

            is CitySelectionEvent.ClearQuery -> {
                _cityMaskStateFlow.value = ""
                _cityPredictions.value = null
            }

            is CitySelectionEvent.UpdateQuery -> _cityMaskStateFlow.value = event.mask

            is CitySelectionEvent.LoadRecentCities -> {
                viewModelScope.launch {
                    fetchRecentCities()
                }
            }
        }
    }

    /**
     * Emits a navigation event to the [navigationEventFlow].
     *
     * Used to communicate one-shot navigation commands to the UI controller.
     * Launches in [viewModelScope] to ensure lifecycle safety.
     *
     * @param event The navigation command to emit
     */
    private fun sendNavigationEvent(event: CityNavigationEvent) {
        viewModelScope.launch {
            _navigationEventFlow.emit(event)
        }
    }

    /**
     * Fetches cities matching the given query from the interactor layer.
     *
     * Executes suspended call to [citiesNamesInteractor.loadCitiesNames].
     * Updates [_cityPredictions] with result.
     * Shows error via [statusRenderer] if response contains an error message.
     *
     * @param query The city name substring to search for
     */
    private suspend fun fetchCities(query: String) {
        try {
            val response = citiesNamesInteractor.loadCitiesNames(query)
            updateCityPredictions(query, response)
        } catch (e: Exception) {
            loggingService.logError(TAG, "Error loading cities for query: $query", e)
            statusRenderer.showError(e.message.toString())
        }
    }

    private fun updateCityPredictions(city: String, result: LoadResult<CitiesNames>?) {
        when (result) {
            is LoadResult.Remote -> {
                statusRenderer.showStatus(resourceManager.getString(R.string.city_predictions_provided))
                _cityPredictions.value =
                    WeatherUiState.Success(data = result.data.cities, DataSource.REMOTE)
            }

            is LoadResult.Local -> {
                statusRenderer.showWarning(resourceManager.getString(R.string.city_predictions_from_cache))
                _cityPredictions.value =
                    WeatherUiState.Success(data = result.data.cities, DataSource.LOCAL)
            }

            is LoadResult.Error -> {
                val errorMessage = result.error.toString()
                statusRenderer.showError(errorMessage)
                WeatherUiState.Error(
                    city = city,
                    errorMessage
                )
            }

            null -> {
                WeatherUiState.Error(
                    city = city,
                    ""
                )
            }
        }
    }

    private suspend fun fetchRecentCities() {
        try {
            when (val response = recentCitiesInteractor.loadRecentCities()) {
                is LoadResult.Error -> {
                    loggingService.logError(TAG, response.error.toString())
                }
                is LoadResult.Local -> {
                    loggingService.logInfoEvent(TAG, response.data.cities.toString())
                    _recentCitiesNamesFlow.emit(
                        WeatherUiState.Success(
                            response.data,
                            DataSource.LOCAL
                        )
                    )
                }

                is LoadResult.Remote -> {
                    loggingService.logInfoEvent(TAG, response.data.cities.toString())
                    _recentCitiesNamesFlow.emit(
                        WeatherUiState.Success(
                            response.data,
                            DataSource.REMOTE
                        )
                    )
                }
            }
        } catch (e: Exception) {
            loggingService.logError(TAG, "Error loading recent cities", e)
            statusRenderer.showError(e.message.toString())
        }
    }

    companion object {
        private const val TAG = "CitiesNamesViewModel"
    }
}