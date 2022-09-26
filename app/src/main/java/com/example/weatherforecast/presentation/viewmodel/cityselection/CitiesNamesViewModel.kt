package com.example.weatherforecast.presentation.viewmodel.cityselection

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.presentation.PresentationUtils.REPEAT_INTERVAL
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
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

    val onGetCitiesNamesLiveData: LiveData<CitiesNamesDomainModel>
        get() = _onGetCitiesNamesLiveData

    private val _onGetCitiesNamesLiveData = MutableLiveData<CitiesNamesDomainModel>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("CitiesNamesViewModel", throwable.message ?: "")
        when (throwable) {
            is NoInternetException -> {
                _onShowErrorLiveData.postValue(throwable.message)
                viewModelScope.launch(Dispatchers.IO) {
                    delay(REPEAT_INTERVAL)
                    getCitiesNamesForMask(cityMask)
                }
            }
            is NoSuchDatabaseEntryException -> {
                _onShowErrorLiveData.postValue("Forecast for city ${throwable.message} is not present in database")
            }
            is Exception -> _onShowErrorLiveData.postValue(throwable.message)
        }
    }

    private lateinit var cityMask: String

    /**
     * Download a cities names beginning with string mask [city]
     */
    fun getCitiesNamesForMask(city: String) {
        Log.d("CitiesNamesViewModel", city)
        viewModelScope.launch(exceptionHandler) {
            val response = citiesNamesInteractor.loadRemoteCitiesNames(city)
            _onGetCitiesNamesLiveData.postValue(response)
            if (response.error.contains("Unable to resolve host")) {
                _onGetCitiesNamesLiveData.postValue(response)
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

    fun setCityMask(cityMask: String) {
        this.cityMask = cityMask
    }
}