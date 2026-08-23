package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.vladchenko.weatherforecast.core.network.NetworkStateHolder
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.CurrentWeatherInteractor
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.converter.WeatherDomainToUiMapper
import kotlinx.serialization.InternalSerializationApi

/**
 * Factory for creating [CurrentWeatherViewModel] instances with dependency injection.
 *
 * Implements [ViewModelProvider.Factory] to provide the necessary dependencies
 * for weather data loading, UI state management, and city selection.
 *
 * @property loggingService Centralized service for application logging
 * @property resourceManager Provides access to application string resources
 * @property uiConverter Converts domain models to UI presentation models
 * @property statusStateHolder Manages and broadcasts UI status messages (loading, errors, info)
 * @property networkStateHolder Manages network connectivity (connect or disconnect)
 * @property preferencesManager Manages user preferences (e.g., temperature units)
 * @property chosenCityInteractor Handles persistence of the last selected city
 * @property forecastInteractor Provides domain logic for loading weather forecast data
 * @property weatherResponseHandler Handles processing of weather response results
 */
class CurrentWeatherViewModelFactory(
    private val loggingService: LoggingService,
    private val resourceManager: ResourceManager,
    private val uiConverter: WeatherDomainToUiMapper,
    private val statusStateHolder: StatusStateHolder,
    private val networkStateHolder: NetworkStateHolder,
    private val preferencesManager: PreferencesManager,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val forecastInteractor: CurrentWeatherInteractor,
    private val weatherResponseHandler: WeatherResponseHandler,
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the [CurrentWeatherViewModel].
     *
     * @param modelClass the class type of the ViewModel to create
     * @return the newly created ViewModel instance
     * @throws IllegalArgumentException if the model class is not [CurrentWeatherViewModel]
     */
    @InternalSerializationApi
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CurrentWeatherViewModel::class.java)) {
            return CurrentWeatherViewModel(
                loggingService,
                resourceManager,
                statusStateHolder,
                networkStateHolder,
                preferencesManager,
                chosenCityInteractor,
                forecastInteractor,
                weatherResponseHandler,
                uiConverter,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}