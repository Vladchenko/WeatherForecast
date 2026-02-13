package com.example.weatherforecast.presentation.viewmodel.cityselection

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.presentation.view.fragments.cityselection.CityItem
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
@HiltViewModel
class CitiesNamesViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val citiesNamesInteractor: CitiesNamesInteractor,
) : AbstractViewModel(connectivityObserver, coroutineDispatchers) {

    /**
     * StateFlow representing the current city name mask entered by the user.
     *
     * Used to track user input for auto-completion. Updates trigger city name lookups.
     */
    val cityMaskStateFlow: StateFlow<CityItem>
        get() = _cityMaskStateFlow

    /**
     * Current list of city names matching the input mask.
     *
     * Nullable — `null` indicates no search has been performed yet or results were cleared.
     */
    val citiesNamesStateFlow: StateFlow<CitiesNamesDomainModel?>
        get() = _citiesNamesStateFlow

    private val _cityMaskStateFlow: MutableStateFlow<CityItem> = MutableStateFlow(CityItem(""))
    private val _citiesNamesStateFlow: MutableStateFlow<CitiesNamesDomainModel?> = MutableStateFlow(null)

    init {
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

    /**
     * Requests a list of cities whose names start with the given [city] prefix.
     *
     * Launches a coroutine to load data via [CitiesNamesInteractor].
     * Updates [citiesNamesStateFlow] with the result or shows an error if applicable.
     *
     * @param city Prefix string to filter city names (e.g., "Kaz")
     */
    fun getCitiesNamesForMask(city: String) {
        Log.d(TAG, "Fetching cities for mask: $city")
        viewModelScope.launch(coroutineDispatchers.io + exceptionHandler) {
            val response = citiesNamesInteractor.loadCitiesNames(city)
            _citiesNamesStateFlow.value = response
            if (response.error.isNotBlank()) {
                Log.d(TAG, "Error from interactor: ${response.error}")
                showError(response.error)
            }
        }
    }

    /**
     * Deletes all stored city name entries from the local database.
     *
     * Useful for clearing cache or resetting data. Executes in a background coroutine.
     */
    fun deleteAllCitiesNames() {
        Log.d(TAG, "Deleting all city names from database")
        viewModelScope.launch(coroutineDispatchers.io + exceptionHandler) {
            citiesNamesInteractor.deleteAllCitiesNames()
        }
    }

    /**
     * Clears the current city name mask input.
     *
     * Resets the search query, typically used when the user wants to start over.
     */
    fun clearCityMask() {
        _cityMaskStateFlow.value = CityItem("")
    }

    /**
     * Clears the list of suggested city names.
     *
     * Does not affect the input mask. Used to reset suggestion UI independently.
     */
    fun clearCitiesNames() {
        _citiesNamesStateFlow.value = null
    }

    companion object {
        private const val TAG = "CitiesNamesViewModel"
    }
}