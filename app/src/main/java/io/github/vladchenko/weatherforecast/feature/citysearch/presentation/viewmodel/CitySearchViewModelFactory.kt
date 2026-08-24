package io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.CitySearchInteractor
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.RecentCitiesInteractor
import kotlinx.coroutines.FlowPreview

/**
 * Factory for creating [CitySearchViewModel] instances.
 *
 * Implements [ViewModelProvider.Factory] to inject dependencies into the ViewModel.
 *
 * @property loggingService service for application logging
 * @property statusStateHolder manages and broadcasts UI status updates
 * @property citySearchInteractor handles domain logic for searching city names
 * @property recentCitiesInteractor handles domain logic for recently searched cities
 */
class CitySearchViewModelFactory(
    private val loggingService: LoggingService,
    private val statusStateHolder: StatusStateHolder,
    private val citySearchInteractor: CitySearchInteractor,
    private val recentCitiesInteractor: RecentCitiesInteractor,
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of [CitySearchViewModel].
     *
     * @param modelClass the class type of the ViewModel to create
     * @return the newly created ViewModel instance
     */
    @FlowPreview
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CitySearchViewModel(
            loggingService,
            statusStateHolder,
            citySearchInteractor,
            recentCitiesInteractor
        ) as T
    }
}