package com.example.weatherforecast.presentation.viewmodel.cityselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor

/**
 * Cities names view model factory
 *
 * @property coroutineDispatchers dispatchers for coroutines
 * @property citiesNamesInteractor provides domain layer data
 */
class CitiesNamesViewModelFactory(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val citiesNamesInteractor: CitiesNamesInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CitiesNamesViewModel(
            coroutineDispatchers,
            citiesNamesInteractor
        ) as T
    }
}