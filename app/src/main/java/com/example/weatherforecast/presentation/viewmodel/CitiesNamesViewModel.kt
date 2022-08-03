package com.example.weatherforecast.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.data.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.network.NetworkUtils
import kotlinx.coroutines.CoroutineExceptionHandler
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

    private val _showErrorLiveData = MutableLiveData<String>()
    private val _gotoOutdatedForecastLiveData = MutableLiveData<Unit>()
    private val _getCitiesNamesLiveData = MutableLiveData<CitiesNamesDomainModel>()

    val showErrorLiveData: LiveData<String>
        get() = _showErrorLiveData

    val getCitiesNamesLiveData: LiveData<CitiesNamesDomainModel>
        get() = _getCitiesNamesLiveData

    val gotoOutdatedForecastLiveData: LiveData<Unit>
        get() = _gotoOutdatedForecastLiveData

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("CitiesNamesViewModel", throwable.message!!)
        _showErrorLiveData.postValue(throwable.message!!)
    }

    /**
     * Download a cities names matching a token [city]
     */
    fun getCitiesNames(city: String?) {
        try {
            if (NetworkUtils.isNetworkAvailable(app)) {
                viewModelScope.launch(exceptionHandler) {
                    if (!city.isNullOrBlank()) {
                        val response = citiesNamesInteractor.loadCitiesNames(city)
                        _getCitiesNamesLiveData.postValue(response)
                    }
                }
            } else {
                // Start a previous fragment
                _gotoOutdatedForecastLiveData.postValue(Unit)
                _showErrorLiveData.postValue("No internet connection, choose Google Maps' default location area.")
                //TODO Remove progressbar and put message to error text
            }
        } catch (ex: Exception) {
            _showErrorLiveData.postValue(ex.message)
        }
    }
}