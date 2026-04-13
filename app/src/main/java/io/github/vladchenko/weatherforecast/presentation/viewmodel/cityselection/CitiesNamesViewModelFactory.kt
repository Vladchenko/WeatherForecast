package io.github.vladchenko.weatherforecast.presentation.viewmodel.cityselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.vladchenko.weatherforecast.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.data.util.LoggingService
import io.github.vladchenko.weatherforecast.domain.citiesnames.CitiesNamesInteractor
import io.github.vladchenko.weatherforecast.domain.recentcities.RecentCitiesInteractor
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.utils.ResourceManager
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