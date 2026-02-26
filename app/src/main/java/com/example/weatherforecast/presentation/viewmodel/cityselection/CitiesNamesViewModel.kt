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

    val navigationEvent: SharedFlow<CityNavigationEvent?>
        get() = _navigationEvent
    /**
     * StateFlow representing the current city name mask entered by the user.
     *
     * Used to track user input for auto-completion. Updates trigger city name lookups.
     */
    val cityMaskStateFlow: StateFlow<String>
        get() = _cityMaskStateFlow
    /**
     * Current list of city names matching the input mask.
     *
     * Nullable — `null` indicates no search has been performed yet or results were cleared.
     */
    val citiesNamesStateFlow: StateFlow<CitiesNames?>
        get() = _citiesNamesStateFlow

    private val _navigationEvent = MutableSharedFlow<CityNavigationEvent?>()

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
            _navigationEvent.emit(event)
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

    /**
     * Deletes all stored city name entries from the local database.
     *
     * Useful for clearing cache or resetting data. Executes in a background coroutine.
     */
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