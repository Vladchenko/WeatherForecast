package com.example.weatherforecast.presentation.viewmodel.cityselection

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.network.NetworkUtils.isNetworkAvailable
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
) : AndroidViewModel(app) {

    private val _OnShowErrorLiveData = MutableLiveData<String>()
    private val _OnUpdateStatusLiveData = MutableLiveData<String>()
    private val _OnGetCitiesNamesLiveData = MutableLiveData<CitiesNamesDomainModel>()

    val onShowErrorLiveData: LiveData<String>
        get() = _OnShowErrorLiveData

    val onUpdateStatusLiveData: LiveData<String>
        get() = _OnUpdateStatusLiveData

    val onGetCitiesNamesLiveData: LiveData<CitiesNamesDomainModel>
        get() = _OnGetCitiesNamesLiveData

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("CitiesNamesViewModel", throwable.message ?: "")
        if (throwable is NoInternetException) {
            _OnShowErrorLiveData.postValue(throwable.cause.toString())
        }
        if (throwable is NoSuchDatabaseEntryException) {
            _OnShowErrorLiveData.postValue("Forecast for city ${throwable.message} is not present in database")
        }
        _OnShowErrorLiveData.postValue(throwable.message ?: "")
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
                    _OnGetCitiesNamesLiveData.postValue(response)
                }
            } else {
                _OnShowErrorLiveData.postValue(app.applicationContext.getString(R.string.database_forecast_downloading))
                // Trying to download a chosen city from database
                viewModelScope.launch(exceptionHandler) {
                    val result = CitiesNamesDomainModel(
                        citiesNamesInteractor.loadLocalCitiesNames(city).toList()
                    )
                    Log.d("CitiesNamesViewModel3", result.cities.size.toString())
                    if (result.cities.isNotEmpty()) {
                        _OnGetCitiesNamesLiveData.postValue(result)
                        Log.d("CitiesNamesViewModel4", result.cities[0].toString())
                    } else {
                        _OnShowErrorLiveData.postValue(
                            app.applicationContext.getString(
                                R.string.database_records_not_found,
                                city
                            )
                        )
                    }
                }
            }
        } catch (ex: Exception) {
            _OnShowErrorLiveData.postValue(ex.message)
        }
    }

    fun checkNetworkConnectionAvailability() {
        if (!isNetworkAvailable(app.applicationContext)) {
            _OnShowErrorLiveData.postValue(app.applicationContext.getString(R.string.network_not_available_error_text))
        } else {
            _OnUpdateStatusLiveData.postValue(app.applicationContext.getString(R.string.city_selection_title))
        }
    }
}