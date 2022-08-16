package com.example.weatherforecast.presentation.viewmodel.cityselection

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor

/**
 * Cities names view model factory
 *
 * @property app custom [Application] implementation for Hilt
 * @property citiesNamesInteractor provides domain layer data
 */
class CitiesNamesViewModelFactory(
    private val app: Application,
    private val citiesNamesInteractor: CitiesNamesInteractor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CitiesNamesViewModel(
            app,
            citiesNamesInteractor
        ) as T
    }
}