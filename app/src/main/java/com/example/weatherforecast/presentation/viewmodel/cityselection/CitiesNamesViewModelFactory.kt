package com.example.weatherforecast.presentation.viewmodel.cityselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import com.example.weatherforecast.domain.recentcities.RecentCitiesInteractor
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.utils.ResourceManager
import kotlinx.coroutines.FlowPreview

/**
 * CitiesNamesViewModel factory
 *
 * @property loggingService provides logging
 * @property statusRenderer displays loading, success, warning, or error statuses
 * @property resourceManager provides string resources
 * @property connectivityObserver internet connectivity observer
 * @property citiesNamesInteractor provides domain layer data for searched cities
 * @property recentCitiesInteractor provides domain layer data for cities searched recently
 */
class CitiesNamesViewModelFactory(
    private val loggingService: LoggingService,
    private val statusRenderer: StatusRenderer,
    private val resourceManager: ResourceManager,
    private val connectivityObserver: ConnectivityObserver,
    private val citiesNamesInteractor: CitiesNamesInteractor,
    private val recentCitiesInteractor: RecentCitiesInteractor,
) : ViewModelProvider.Factory {

    @FlowPreview
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CitiesNamesViewModel(
            connectivityObserver,
            loggingService,
            statusRenderer,
            resourceManager,
            citiesNamesInteractor,
            recentCitiesInteractor
        ) as T
    }
}