package io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.CitySearchInteractor
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.RecentCitiesInteractor
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import kotlinx.coroutines.FlowPreview

/**
 * CitiesNamesViewModel factory
 *
 * @property loggingService provides logging
 * @property statusRenderer displays loading, success, warning, or error statuses
 * @property resourceManager provides string resources
 * @property connectivityObserver internet connectivity observer
 * @property citySearchInteractor provides domain layer data for searched cities
 * @property recentCitiesInteractor provides domain layer data for cities searched recently
 */
class CitySearchViewModelFactory(
    private val loggingService: LoggingService,
    private val statusRenderer: StatusRenderer,
    private val resourceManager: ResourceManager,
    private val connectivityObserver: ConnectivityObserver,
    private val citySearchInteractor: CitySearchInteractor,
    private val recentCitiesInteractor: RecentCitiesInteractor,
) : ViewModelProvider.Factory {

    @FlowPreview
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CitySearchViewModel(
            connectivityObserver,
            loggingService,
            statusRenderer,
            resourceManager,
            citySearchInteractor,
            recentCitiesInteractor
        ) as T
    }
}