package io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.state.DataSource
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.CitySearchInteractor
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CitySearch
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.event.CitySelectionEvent
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.RecentCitiesInteractor
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.model.RecentCities
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.presentation.viewmodel.AbstractViewModel
import io.github.vladchenko.weatherforecast.presentation.viewmodel.cityselection.CityNavigationEvent
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
 *    Display raw message via [StatusRenderer]
 *
 * ## Thread Safety
 * All coroutines are launched in [viewModelScope], ensuring automatic cancellation on ViewModel destruction.
 * State updates occur on the main thread, safe for UI observation.
 *
 * @param connectivityObserver Monitors network state; inherited base functionality from [AbstractViewModel]
 * @property loggingService Logs errors and debug information
 * @property statusRenderer Displays status messages to the user
 * @property resourceManager Provides access to string resources for dynamic UI content
 * @property citySearchInteractor Business logic layer for loading and filtering city names
 * @property recentCitiesInteractor Operates recently used cities
 */
@FlowPreview
@HiltViewModel
class CitySearchViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    private val loggingService: LoggingService,
    private val statusRenderer: StatusRenderer,
    private val resourceManager: ResourceManager,
    private val citySearchInteractor: CitySearchInteractor,
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
     * Contains a [CitySearch] object with:
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
        statusRenderer.showError(throwable.message.toString())
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
     * Handles deletion of all recent cities.
     *
     * Launches a coroutine to:
     * - Clear stored recent cities via [RecentCitiesInteractor.deleteRecentCities]
     * - Re-fetch the updated list from the data source using [fetchRecentCities]
     *
     * This ensures that the UI state reflects the actual data in the database,
     * maintaining consistency and handling potential errors during reload.
     * Unlike direct state emission, this approach respects the single source of truth (database)
     * and supports proper error propagation and loading states.
     *
     * @note Always re-fetches recent cities after deletion instead of manually emitting an empty state
     *       to avoid inconsistencies if the deletion fails or is partial.
     */
    fun deleteRecents() {
        viewModelScope.launch {
            recentCitiesInteractor.deleteRecentCities()
            fetchRecentCities()
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
     * Executes suspended call to [citySearchInteractor.loadCitiesNames].
     * Updates [_cityPredictions] with result.
     * Shows error via [statusRenderer] if response contains an error message.
     *
     * @param query The city name substring to search for
     */
    private suspend fun fetchCities(query: String) {
        try {
            val response = citySearchInteractor.loadCitiesNames(query)
            updateCityPredictions(query, response)
        } catch (e: Exception) {
            loggingService.logError(TAG, "Error loading cities for query: $query", e)
            statusRenderer.showError(e.message.toString())
        }
    }

    private fun updateCityPredictions(city: String, result: LoadResult<CitySearch>?) {
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