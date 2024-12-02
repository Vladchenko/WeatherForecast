package com.example.weatherforecast.presentation.viewmodel.cityselection

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.presentation.fragments.cityselection.CityItem
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * View model (MVVM component) for cities names presentation.
 *
 * @param connectivityObserver internet connectivity observer
 * @property coroutineDispatchers dispatchers for coroutines
 * @property citiesNamesInteractor provides domain layer data.
 */
@HiltViewModel
class CitiesNamesViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val citiesNamesInteractor: CitiesNamesInteractor,
) : AbstractViewModel(connectivityObserver, coroutineDispatchers) {

    private val _cityMaskState: MutableStateFlow<CityItem> = MutableStateFlow(CityItem(""))
    private val _citiesNamesState: MutableState<CitiesNamesDomainModel?> = mutableStateOf(null)

    val cityMaskState: StateFlow<CityItem>
        get() = _cityMaskState
    val citiesNamesState: State<CitiesNamesDomainModel?>
        get() = _citiesNamesState

    init {
        showStatus(R.string.city_selection_title)
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("CitiesNamesViewModel", throwable.message.orEmpty())
        when (throwable) {
            is NoSuchDatabaseEntryException -> {
                showError("City with a name ${throwable.message} is not present in database")
            }
            is Exception -> showError(throwable.message.toString())
        }
    }

    private lateinit var cityMask: String

    /**
     * Download a cities names beginning with string mask [city]
     */
    fun getCitiesNamesForMask(city: String) {
        Log.d("CitiesNamesViewModel", city)
        viewModelScope.launch(exceptionHandler) {
            val response = citiesNamesInteractor.loadCitiesNames(city)
            _citiesNamesState.value = response
            if (response.error.isNotBlank()) {
                Log.d("CitiesNamesViewModel", response.error)
                showError(response.error)
            }
        }
    }

    /**
     * Delete all cities names. Method is used on demand.
     */
    fun deleteAllCitiesNames() {
        viewModelScope.launch(exceptionHandler) {
            citiesNamesInteractor.deleteAllCitiesNames()
        }
    }

    /**
     * Empty a mask that provides several cities names that match it.
     */
    fun clearCityMask() {
        _cityMaskState.value = CityItem("")
    }

    /**
     * Empty a list of cities that are matching a mask.
     */
    fun emptyCitiesNames() {
        _citiesNamesState.value = null
    }
}