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
 * @property loggingService centralized service for application logging
 * @property stateHolder manages and broadcasts UI status messages (loading, errors, info)
 * @property resourceManager provides access to application string resources
 * @property preferencesManager manages user preferences (e.g., temperature units)
 * @property networkStateHolder manages network connectivity (connect or disconnect)
 * @property chosenCityInteractor handles persistence of the last selected city
 * @property weatherInteractor provides domain logic for loading weather forecast data
 * @property uiConverter converts domain models to UI presentation models
 */
class CurrentWeatherViewModelFactory(
    private val loggingService: LoggingService,
    private val stateHolder: StatusStateHolder,
    private val resourceManager: ResourceManager,
    private val uiConverter: WeatherDomainToUiMapper,
    private val networkStateHolder: NetworkStateHolder,
    private val preferencesManager: PreferencesManager,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val weatherInteractor: CurrentWeatherInteractor,
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
                stateHolder,
                networkStateHolder,
                preferencesManager,
                chosenCityInteractor,
                uiConverter,
                weatherInteractor
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}