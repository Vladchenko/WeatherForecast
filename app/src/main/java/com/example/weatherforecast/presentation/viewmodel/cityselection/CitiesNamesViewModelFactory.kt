package com.example.weatherforecast.presentation.viewmodel.cityselection

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor

/**
 * Cities names view model factory
 *
 * @param app custom [Application] implementation for Hilt
 * @param coroutineDispatchers dispatchers for coroutines
 * @param citiesNamesInteractor provides domain layer data
 */
class CitiesNamesViewModelFactory(
    private val app: Application,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val citiesNamesInteractor: CitiesNamesInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CitiesNamesViewModel(
            app,
            coroutineDispatchers,
            citiesNamesInteractor
        ) as T
    }
}