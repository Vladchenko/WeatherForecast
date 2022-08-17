package com.example.weatherforecast.presentation.viewmodel.cityselection

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.models.domain.CityDomainModel
import com.example.weatherforecast.network.NetworkUtils.isNetworkAvailable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
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

    private val _showErrorLiveData = MutableLiveData<String>()
    private val _updateStatusLiveData = MutableLiveData<String>()
    private val _gotoOutdatedForecastLiveData = MutableLiveData<Unit>()
    private val _getCitiesNamesLiveData = MutableLiveData<CitiesNamesDomainModel>()

    val showErrorLiveData: LiveData<String>
        get() = _showErrorLiveData

    val updateStatusLiveData: LiveData<String>
        get() = _updateStatusLiveData

    val getCitiesNamesLiveData: LiveData<CitiesNamesDomainModel>
        get() = _getCitiesNamesLiveData

    val gotoOutdatedForecastLiveData: LiveData<Unit>
        get() = _gotoOutdatedForecastLiveData

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("CitiesNamesViewModel", throwable.message ?: "")
        if (throwable is NoInternetException) {
            _showErrorLiveData.postValue(throwable.cause.toString())
        }
        _showErrorLiveData.postValue(throwable.message ?: "")
    }

    /**
     * Download a cities names matching a token [city]
     */
    fun getCitiesNames(city: String) {
        Log.d("CitiesNamesViewModel1", city)
        try {
            if (isNetworkAvailable(app)) {
                viewModelScope.launch(exceptionHandler) {
                    val response = citiesNamesInteractor.loadRemoteCitiesNames(city)
                    _getCitiesNamesLiveData.postValue(response)
                }
            } else {
                _showErrorLiveData.postValue(app.applicationContext.getString(R.string.database_forecast_downloading))
                // Trying to download a chosen city from database
                viewModelScope.launch(exceptionHandler) {
                    val result = CitiesNamesDomainModel(
                        citiesNamesInteractor.loadLocalCitiesNames("").flatMapConcat {
                            it.asFlow().map {
                                Log.d("CitiesNamesViewModel2", it.name)
                            }
                            it.asFlow()
                        }.toList()
                    )
                    Log.d("CitiesNamesViewModel3", result.cities.size.toString())
                    if (result.cities.isNotEmpty()) {
                        _getCitiesNamesLiveData.postValue(result)
                        Log.d("CitiesNamesViewModel4", result.cities[0].toString())
                    } else {
                        _showErrorLiveData.postValue(
                            app.applicationContext.getString(
                                R.string.database_records_not_found,
                                city
                            )
                        )
                    }
                }
            }
        } catch (ex: Exception) {
            _showErrorLiveData.postValue(ex.message)
        }
    }

    fun saveChosenCity(city: CityDomainModel) {
        viewModelScope.launch(exceptionHandler) {
            citiesNamesInteractor.saveCitiesNames(city)
        }
    }

    fun checkNetworkConnectionAvailability() {
        if (!isNetworkAvailable(app.applicationContext)) {
            _showErrorLiveData.postValue(app.applicationContext.getString(R.string.network_not_available_error_text))
        } else {
            _updateStatusLiveData.postValue(app.applicationContext.getString(R.string.city_selection_title))
        }
    }
}