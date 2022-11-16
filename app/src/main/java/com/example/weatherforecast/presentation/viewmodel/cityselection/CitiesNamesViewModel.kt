package com.example.weatherforecast.presentation.viewmodel.cityselection

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.presentation.PresentationUtils.REPEAT_INTERVAL
import com.example.weatherforecast.presentation.fragments.cityselection.CityItem
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * View model (MVVM component) for cities names presentation.
 *
 * @property app custom [Application] implementation for Hilt.
 * @property citiesNamesInteractor provides domain layer data.
 */
@HiltViewModel
class CitiesNamesViewModel @Inject constructor(
    private val app: Application,
    private val citiesNamesInteractor: CitiesNamesInteractor,
) : AbstractViewModel(app) {

    val citiesNamesState: MutableState<CitiesNamesDomainModel?> = mutableStateOf(null)
    val cityMaskState: MutableStateFlow<CityItem> = MutableStateFlow(CityItem(""))

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("CitiesNamesViewModel", throwable.message ?: "")
        when (throwable) {
            is NoInternetException -> {
                onShowError(throwable.message.toString())
                viewModelScope.launch(Dispatchers.IO) {
                    delay(REPEAT_INTERVAL)
                    getCitiesNamesForMask(cityMask)
                }
            }
            is NoSuchDatabaseEntryException -> {
                onShowError("Forecast for city ${throwable.message} is not present in database")
            }
            is Exception -> onShowError(throwable.message.toString())
        }
    }

    private lateinit var cityMask: String

    init {
        toolbarSubtitleState.value = app.getString(R.string.city_selection_title)
    }

    /**
     * Download a cities names beginning with string mask [city]
     */
    fun getCitiesNamesForMask(city: String) {
        Log.d("CitiesNamesViewModel", city)
        viewModelScope.launch(exceptionHandler) {
            val response = citiesNamesInteractor.loadRemoteCitiesNames(city)
            citiesNamesState.value = response
            if (response.error.contains("Unable to resolve host")) {
                Log.d("CitiesNamesViewModel", response.cities[0].toString())
                throw NoInternetException(app.applicationContext.getString(R.string.database_city_downloading))
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
}