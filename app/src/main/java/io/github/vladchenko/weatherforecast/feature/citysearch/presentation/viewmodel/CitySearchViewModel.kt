package io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityModel
import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.ui.state.DataSource
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState.Error
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState.Success
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.CitySearchInteractor
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CitySearch
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.event.CitySelectionEvent
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.RecentCitiesInteractor
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.model.RecentCities
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing UI state and business logic for city search.
 *
 * This ViewModel handles:
 * - User input tracking for city name search with debounced query processing
 * - Loading, filtering, and displaying city predictions
 * - Managing recently searched cities (loading, clearing)
 * - Displaying status messages (errors, loading states) via [StatusStateHolder]
 *
 * ## State Management
 * - [_cityMaskStateFlow]: Tracks current text input in the search field
 * - [_cityPredictions]: Holds filtered list of cities matching the query
 * - [_recentCitiesNamesFlow]: Manages state of recently searched cities
 *
 * ## Event Handling
 * Uses [CitySelectionEvent] sealed class to handle UI interactions in a unidirectional
 * data flow manner, ensuring predictable state updates and simplifying testing.
 *
 * ## Search Behavior
 * Implements a 1-second debounce on user input to minimize unnecessary database/remote calls.
 * Search triggers only when input is non-empty and differs from the previous value.
 *
 * ## Error Handling
 * Utilizes [CoroutineExceptionHandler] for uncaught exceptions and dedicated methods
 * ([showError], [showStatus]) for displaying feedback via [StatusStateHolder].
 *
 * @property loggingService Centralized logging service for errors and debug info
 * @property statusStateHolder Manages and broadcasts UI status updates (loading, errors, info)
 * @property citySearchInteractor Handles domain logic for fetching and filtering city names
 * @property recentCitiesInteractor Manages recently searched cities persistence and loading
 */
@HiltViewModel
class CitySearchViewModel @Inject constructor(
    private val loggingService: LoggingService,
    private val statusStateHolder: StatusStateHolder,
    private val citySearchInteractor: CitySearchInteractor,
    private val recentCitiesInteractor: RecentCitiesInteractor
) : ViewModel() {

    /**
     * State flow representing the current user input in the city search field.
     *
     * Updated via [onCitySelectionEvent] with [CitySelectionEvent.UpdateQuery].
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
    val cityPredictions: StateFlow<WeatherUiState<ImmutableList<CityModel>>?>
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
    private val _cityPredictions =
        MutableStateFlow<WeatherUiState<ImmutableList<CityModel>>?>(null)
    private val _recentCitiesNamesFlow = MutableStateFlow<WeatherUiState<RecentCities>?>(null)

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        loggingService.logError(TAG, throwable.message.orEmpty(), throwable)
        showError(throwable.message.toString())
    }

    init {
        startDebouncedSearch()
        statusStateHolder.updateInfoStatus(R.string.city_selection_title)
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
    @OptIn(FlowPreview::class)
    private fun startDebouncedSearch() {
        viewModelScope.launch(exceptionHandler) {
            _cityMaskStateFlow
                .debounce(1000)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collect { query ->
                    _cityPredictions.value = WeatherUiState.Loading(false)
                    fetchCities(query)
                }
        }
    }

    /**
     * Processes incoming UI events and updates state accordingly.
     *
     * Dispatches behavior based on event type:
     * - [CitySelectionEvent.ClearQuery]: Resets search query and clears results
     * - [CitySelectionEvent.UpdateQuery]: Updates search mask and triggers debounced search
     * - [CitySelectionEvent.LoadRecentCities]: Triggers loading of recently searched cities
     * - [CitySelectionEvent.ClearRecentCities]: Clears recently searched cities from database
     *
     * @param event The user action to process
     */
    fun onCitySelectionEvent(event: CitySelectionEvent) {
        when (event) {
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

            is CitySelectionEvent.SaveCityToRecents -> {
                viewModelScope.launch {
                    recentCitiesInteractor.addCityToRecents(event.cityModel)
                }
            }

            is CitySelectionEvent.ClearRecentCities -> {
                deleteRecents()
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
     * Fetches cities matching the given query from the interactor layer.
     *
     * Executes suspended call to [CitySearchInteractor.loadCitiesNames].
     * Updates [_cityPredictions] with result.
     * Shows error via [statusStateHolder] if response contains an error message.
     *
     * @param query The city name substring to search for
     */
    private suspend fun fetchCities(query: String) {
        try {
            val response = citySearchInteractor.loadCitiesNames(query)
            updateCityPredictions(query, response)
        } catch (e: Exception) {
            loggingService.logError(TAG, "Error loading cities for query: $query", e)
            showError(e.message.toString())
        }
    }

    private fun updateCityPredictions(city: String, result: LoadResult<CitySearch>) {
        when (result) {
            is LoadResult.Remote -> {
                showStatus(R.string.city_predictions_provided)
                _cityPredictions.value =
                    Success(data = result.data.cities, DataSource.REMOTE)
            }

            is LoadResult.Local -> {
                statusStateHolder.updateWarningStatus(R.string.city_predictions_from_cache)
                _cityPredictions.value =
                    Success(data = result.data.cities, DataSource.LOCAL)
            }

            is LoadResult.Error -> {
                val errorMessage = when (result.error) {
                    is ForecastError.NoDataAvailable -> {
                        // TODO Resolve error message without comparing with hardcoded string
                        if (result.error.message.lowercase().contains("unable to resolve host")) {
                            "Server doesn't respond"
                        } else {
                            result.error.message
                        }
                    }

                    else -> result.error.toString()
                }
                showError(errorMessage)
                _cityPredictions.value = Error(
                    city = city,
                    message = errorMessage,
                    messageId = null
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
                        Success(
                            response.data,
                            DataSource.LOCAL
                        )
                    )
                }

                is LoadResult.Remote -> {
                    loggingService.logInfoEvent(TAG, response.data.cities.toString())
                    _recentCitiesNamesFlow.emit(
                        Success(
                            response.data,
                            DataSource.REMOTE
                        )
                    )
                }
            }
        } catch (e: Exception) {
            loggingService.logError(TAG, "Error loading recent cities", e)
            showError(e.message.toString())
        }
    }

    private fun showError(errorMessage: String) {
        statusStateHolder.updateErrorStatus(errorMessage)
    }

    private fun showStatus(statusMessage: Int) {
        statusStateHolder.updateInfoStatus(statusMessage)
    }

    companion object {
        private const val TAG = "CitiesNamesViewModel"
    }
}