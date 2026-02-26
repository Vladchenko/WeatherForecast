package com.example.weatherforecast.presentation.viewmodel.cityselection

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.models.domain.CitiesNames
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
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
 * ViewModel for managing the city name search and selection UI state.
 *
 * This ViewModel handles:
 * - Observing user input (city name mask)
 * - Fetching matching city names from the domain layer
 * - Managing UI state for city suggestions
 * - Error handling during data retrieval
 * - Clearing cached data on demand
 *
 * It uses [CitiesNamesInteractor] to retrieve data and respects internet connectivity
 * via [ConnectivityObserver]. All coroutines are launched in [viewModelScope]
 * with proper exception handling.
 *
 * @property connectivityObserver Observes network connectivity state
 * @property coroutineDispatchers Provides dispatchers for coroutine execution
 * @property citiesNamesInteractor Business logic handler for city name operations
 */
@FlowPreview
@HiltViewModel
class CitiesNamesViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val citiesNamesInteractor: CitiesNamesInteractor,
) : AbstractViewModel(connectivityObserver, coroutineDispatchers) {

    /**
     * A [SharedFlow] that emits navigation events based on user actions.
     *
     * Emits values such as:
     * - [CityNavigationEvent.NavigateUp] — when the user requests to go back
     * - [CityNavigationEvent.OpenWeatherFor] — when a city is selected
     *
     * The UI layer should collect this flow to perform navigation actions.
     * Events are emitted one-off and should be consumed immediately.
     */
    val navigationEventFlow: SharedFlow<CityNavigationEvent?>
        get() = _navigationEventFlow

    /**
     * A [StateFlow] that emits the current user input for city name search.
     *
     * This value is updated via [onEvent] with [CitySelectionEvent.UpdateQuery].
     * Used internally to trigger debounced city name lookups.
     */
    val cityMaskStateFlow: StateFlow<String>
        get() = _cityMaskStateFlow

    /**
     * A [StateFlow] that holds the latest list of cities matching the current search query.
     *
     * Value is `null` if no search has been performed or results were cleared.
     * Updated automatically after a successful call to [fetchCities].
     */
    val citiesNamesStateFlow: StateFlow<CitiesNames?>
        get() = _citiesNamesStateFlow

    private val _navigationEventFlow = MutableSharedFlow<CityNavigationEvent?>()
    private val _cityMaskStateFlow: MutableStateFlow<String> = MutableStateFlow("")
    private val _citiesNamesStateFlow: MutableStateFlow<CitiesNames?> = MutableStateFlow(null)

    init {
        startDebouncedSearch()
        showMessage(R.string.city_selection_title)
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, throwable.message.orEmpty(), throwable)
        when (throwable) {
            is NoSuchDatabaseEntryException -> {
                showError(R.string.default_city_absent)
                Log.d(TAG, "Default city not found in database", throwable)
            }
            is Exception -> {
                showError(throwable.message.toString())
                Log.e(TAG, "Unexpected error in city name loading", throwable)
            }
        }
    }

    @FlowPreview
    private fun startDebouncedSearch() {
        viewModelScope.launch {
            _cityMaskStateFlow
                .debounce(1000)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collect { query ->
                    fetchCities(query)
                }
        }
    }

    /**
     * Handles incoming UI events related to city selection.
     *
     * Supported events:
     * - [CitySelectionEvent.UpdateQuery]: Updates the search query and triggers a debounced search.
     * - [CitySelectionEvent.ClearQuery]: Clears the current query and removes suggestion results.
     * - [CitySelectionEvent.SelectCity]: Navigates to the weather screen for the selected city.
     * - [CitySelectionEvent.NavigateUp]: Requests navigation back to the previous screen.
     *
     * @param event the user action to process
     */
    fun onEvent(event: CitySelectionEvent) {
        when (event) {
            is CitySelectionEvent.NavigateUp -> sendNavigationEvent(CityNavigationEvent.NavigateUp)

            is CitySelectionEvent.SelectCity -> sendNavigationEvent(CityNavigationEvent.OpenWeatherFor(event.city))

            is CitySelectionEvent.ClearQuery -> {
                _cityMaskStateFlow.value = ""
                _citiesNamesStateFlow.value = null
            }

            is CitySelectionEvent.UpdateQuery -> {
                _cityMaskStateFlow.value = event.mask
            }
        }
    }

    private fun sendNavigationEvent(event: CityNavigationEvent) {
        viewModelScope.launch {
            _navigationEventFlow.emit(event)
        }
    }

    private suspend fun fetchCities(query: String) {
        try {
            val response = citiesNamesInteractor.loadCitiesNames(query)
            _citiesNamesStateFlow.value = response
            if (response.error.isNotBlank()) {
                showError(response.error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cities for query: $query", e)
            showError(e.message.toString())
        }
    }

    private fun deleteAllCitiesNames() {
        Log.d(TAG, "Deleting all city names from database")
        viewModelScope.launch(coroutineDispatchers.io + exceptionHandler) {
            citiesNamesInteractor.deleteAllCitiesNames()
        }
    }

    companion object {
        private const val TAG = "CitiesNamesViewModel"
    }
}