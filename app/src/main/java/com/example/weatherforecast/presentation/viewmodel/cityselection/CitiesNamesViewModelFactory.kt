package com.example.weatherforecast.presentation.viewmodel.cityselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.utils.ResourceManager

/**
 * CitiesNamesViewModel factory
 *
 * @property loggingService provides logging
 * @property statusRenderer displays loading, success, warning, or error statuses
 * @property resourceManager provides string resources
 * @property connectivityObserver internet connectivity observer
 * @property citiesNamesInteractor provides domain layer data
 */
class CitiesNamesViewModelFactory(
    private val loggingService: LoggingService,
    private val statusRenderer: StatusRenderer,
    private val resourceManager: ResourceManager,
    private val connectivityObserver: ConnectivityObserver,
    private val citiesNamesInteractor: CitiesNamesInteractor
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CitiesNamesViewModel(
            connectivityObserver,
            loggingService,
            statusRenderer,
            resourceManager,
            citiesNamesInteractor
        ) as T
    }
}