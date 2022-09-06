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
import com.example.weatherforecast.network.NetworkUtils.isNetworkAvailable
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

/**
 * View model (MVVM component) for cities names presentation.
 *
 * @property app custom [Application] implementation for Hilt.
 * @property citiesNamesInteractor provides domain layer data.
 */
class CitiesNamesViewModel(
    private val app: Application,
    private val citiesNamesInteractor: CitiesNamesInteractor,
) : AbstractViewModel(app) {
    
    private val _onGetCitiesNamesLiveData = MutableLiveData<CitiesNamesDomainModel>()

    val onGetCitiesNamesLiveData: LiveData<CitiesNamesDomainModel>
        get() = _onGetCitiesNamesLiveData

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("CitiesNamesViewModel", throwable.message ?: "")
        if (throwable is NoInternetException) {
            _onShowErrorLiveData.postValue(throwable.cause.toString())
        }
        if (throwable is NoSuchDatabaseEntryException) {
            _onShowErrorLiveData.postValue("Forecast for city ${throwable.message} is not present in database")
        }
        _onShowErrorLiveData.postValue(throwable.message ?: "")
    }

    /**
     * Download a cities names matching string mask [city]
     */
    fun getCitiesNamesForMask(city: String) {
        Log.d("CitiesNamesViewModel1", city)
        try {
            if (isNetworkAvailable(app)) {
                viewModelScope.launch(exceptionHandler) {
                    val response = citiesNamesInteractor.loadRemoteCitiesNames(city)
                    _onGetCitiesNamesLiveData.postValue(response)
                }
            } else {
                _onShowErrorLiveData.postValue(app.applicationContext.getString(R.string.database_forecast_downloading))
                // Trying to download a chosen city from database
                viewModelScope.launch(exceptionHandler) {
                    val result = CitiesNamesDomainModel(
                        citiesNamesInteractor.loadLocalCitiesNames(city).toList()
                    )
                    Log.d("CitiesNamesViewModel3", result.cities.size.toString())
                    if (result.cities.isNotEmpty()) {
                        _onGetCitiesNamesLiveData.postValue(result)
                        Log.d("CitiesNamesViewModel4", result.cities[0].toString())
                    } else {
                        _onShowErrorLiveData.postValue(
                            app.applicationContext.getString(
                                R.string.database_records_not_found,
                                city
                            )
                        )
                    }
                }
            }
        } catch (ex: Exception) {
            _onShowErrorLiveData.postValue(ex.message)
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