package io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain.HourlyWeatherInteractor

/**
 * Factory for creating [HourlyWeatherViewModel] instances.
 *
 * Implements [ViewModelProvider.Factory] to provide the necessary dependencies for the hourly
 * weather feature.
 *
 * @property loggingService Service for application logging.
 * @property resourceManager Provides access to application string resources.
 * @property statusStateHolder Manages and broadcasts UI status messages.
 * @property preferencesManager Manages user preferences (e.g., temperature units).
 * @property forecastRemoteInteractor Domain interactor responsible for loading hourly weather data.
 */
@Suppress("UNCHECKED_CAST")
class HourlyWeatherViewModelFactory(
    private val loggingService: LoggingService,
    private val resourceManager: ResourceManager,
    private val statusStateHolder: StatusStateHolder,
    private val preferencesManager: PreferencesManager,
    private val forecastRemoteInteractor: HourlyWeatherInteractor
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of [HourlyWeatherViewModel].
     *
     * @param modelClass The class of the ViewModel to create.
     * @return The newly created [HourlyWeatherViewModel] instance.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HourlyWeatherViewModel(
            loggingService,
            resourceManager,
            statusStateHolder,
            preferencesManager,
            forecastRemoteInteractor
        ) as T
    }
}