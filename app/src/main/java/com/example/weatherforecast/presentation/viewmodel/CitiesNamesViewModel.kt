package com.example.weatherforecast.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.data.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
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
    private val citiesNamesInteractor: CitiesNamesInteractor
) : AndroidViewModel(app) {

    private val _getCitiesNamesLiveData: MutableLiveData<CitiesNamesDomainModel> = MutableLiveData()
    private val _showErrorLiveData: MutableLiveData<String> = MutableLiveData()

    val getCitiesNamesLiveData: LiveData<CitiesNamesDomainModel>
        get() = _getCitiesNamesLiveData

    val showErrorLiveData: LiveData<String>
        get() = _showErrorLiveData

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("CitiesNamesViewModel", throwable.message!!)
        _showErrorLiveData.postValue(throwable.message!!)
    }

    /**
     * TODO
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
            }
        } catch (ex: Exception) {
            _showErrorLiveData.postValue(ex.message)
        }
    }
}