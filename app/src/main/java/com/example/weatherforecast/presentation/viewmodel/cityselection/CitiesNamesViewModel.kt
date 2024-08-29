package com.example.weatherforecast.presentation.viewmodel.cityselection

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.presentation.PresentationUtils.REPEAT_INTERVAL
import com.example.weatherforecast.presentation.fragments.cityselection.CityItem
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * View model (MVVM component) for cities names presentation.
 *
 * @property coroutineDispatchers dispatchers for coroutines
 * @property citiesNamesInteractor provides domain layer data.
 */
@HiltViewModel
class CitiesNamesViewModel @Inject constructor(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val citiesNamesInteractor: CitiesNamesInteractor,
) : AbstractViewModel(coroutineDispatchers) {

    private val _cityMaskState: MutableStateFlow<CityItem> = MutableStateFlow(CityItem(""))
    private val _citiesNamesState: MutableState<CitiesNamesDomainModel?> = mutableStateOf(null)

    val cityMaskState: StateFlow<CityItem>
        get() = _cityMaskState
    val citiesNamesState: State<CitiesNamesDomainModel?>
        get() = _citiesNamesState

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("CitiesNamesViewModel", throwable.message.orEmpty())
        when (throwable) {
            is NoInternetException -> {
                showError(throwable.message.toString())
                viewModelScope.launch(coroutineDispatchers.io) {
                    delay(REPEAT_INTERVAL)
                    getCitiesNamesForMask(cityMask)
                }
            }
            is NoSuchDatabaseEntryException -> {
                showError("City with a name ${throwable.message} is not present in database")
            }
            is Exception -> showError(throwable.message.toString())
        }
    }

    private lateinit var cityMask: String

    init {
        showStatus(R.string.city_selection_title)
    }

    /**
     * Download a cities names beginning with string mask [city]
     */
    fun getCitiesNamesForMask(city: String) {
        Log.d("CitiesNamesViewModel", city)
        viewModelScope.launch(exceptionHandler) {
            val response = citiesNamesInteractor.loadRemoteCitiesNames(city)
            _citiesNamesState.value = response
            if (response.error.contains("Unable to resolve host")) {
                Log.d("CitiesNamesViewModel", response.cities[0].toString())
                showError(R.string.city_mask_entries_error)
                throw NoInternetException(response.error)
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
    fun emptyCityMask() {
        _cityMaskState.value = CityItem("")
    }

    /**
     * Empty a list of cities that are matching a mask.
     */
    fun emptyCitiesNames() {
        _citiesNamesState.value = null
    }
}