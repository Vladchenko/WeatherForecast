package io.github.vladchenko.weatherforecast.presentation.viewmodel.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.feature.currentweather.domain.CurrentWeatherInteractor
import io.github.vladchenko.weatherforecast.presentation.converter.WeatherDomainToUiConverter
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import kotlinx.serialization.InternalSerializationApi

/**
 * WeatherForecastViewModel factory
 *
 * @property loggingService centralized service for application logging
 * @property statusRenderer displays loading, success, warning, or error statuses
 * @property resourceManager resource manager
 * @property preferencesManager to provide preferences for application
 * @property connectivityObserver internet connectivity observer
 * @property coroutineDispatchers coroutines dispatchers
 * @property chosenCityInteractor downloads a previously chosen city
 * @property weatherInteractor downloads weather forecast through network
 */
class CurrentWeatherViewModelFactory(
    private val loggingService: LoggingService,
    private val statusRenderer: StatusRenderer,
    private val resourceManager: ResourceManager,
    private val preferencesManager: PreferencesManager,
    private val connectivityObserver: ConnectivityObserver,
    private val chosenCityInteractor: ChosenCityInteractor,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val weatherInteractor: CurrentWeatherInteractor,
    private val uiConverter: WeatherDomainToUiConverter,
) : ViewModelProvider.Factory {

    @InternalSerializationApi
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CurrentWeatherViewModel::class.java)) {
            return CurrentWeatherViewModel(
                connectivityObserver,
                statusRenderer,
                loggingService,
                resourceManager,
                preferencesManager,
                coroutineDispatchers,
                chosenCityInteractor,
                weatherInteractor,
                uiConverter
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}