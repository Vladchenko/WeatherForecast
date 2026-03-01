package com.example.weatherforecast.presentation.coordinator

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.alertdialog.dialogcontroller.WeatherDialogController
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.presentation.viewmodel.forecast.CurrentWeatherViewModel
import com.example.weatherforecast.utils.ResourceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * Coordinates user actions related to city selection and fallback strategies.
 *
 * This coordinator handles cases where:
 * - No city was selected (blank input)
 * - Selected city was not found
 *
 * It presents appropriate UI feedback and offers resolution paths such as:
 * - Retrying with geolocation
 * - Navigating to manual city selection
 *
 * Decouples city selection logic from weather coordination concerns.
 *
 * @property forecastViewModel provides state and events related to city input
 * @property geoLocationCoordinator handles location-based city resolution
 * @property statusRenderer displays status messages to the user
 * @property dialogController manages presentation of selection dialogs
 * @property resourceManager accesses localized string resources
 * @property onGotoCitySelection triggers navigation to city picker
 */
class CitySelectionCoordinator(
    private val forecastViewModel: CurrentWeatherViewModel,
    private val geoLocationCoordinator: GeoLocationCoordinator,
    private val statusRenderer: StatusRenderer,
    private val dialogController: WeatherDialogController,
    private val resourceManager: ResourceManager,
    private val onGotoCitySelection: () -> Unit
) {

    /**
     * Starts observing city-related flows and reacting to invalid or missing input.
     *
     * Launches collection of:
     * - [CurrentWeatherViewModel.chosenCityNotFoundStateFlow]
     * - [CurrentWeatherViewModel.chosenCityBlankStateFlow]
     *
     * All observations are lifecycle-safe and occur during [Lifecycle.State.STARTED].
     *
     * @param scope Coroutine scope for launching collectors
     * @param lifecycle Lifecycle to bind observation duration to
     */
    fun startObserving(scope: CoroutineScope, lifecycle: Lifecycle) {
        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectChosenCityNotFoundFlow(forecastViewModel.chosenCityNotFoundStateFlow) }
                launch { collectChosenCityBlankFlow(forecastViewModel.chosenCityBlankStateFlow) }
            }
        }
    }

    private suspend fun collectChosenCityNotFoundFlow(flow: SharedFlow<String>) {
        flow.collect { city ->
            statusRenderer.showWarning(
                resourceManager.getString(R.string.forecast_no_data_for_city, city)
            )
            dialogController.showChosenCityNotFound(city) {
                onGotoCitySelection()
            }
        }
    }

    private suspend fun collectChosenCityBlankFlow(flow: SharedFlow<Unit>) {
        flow.collect {
            geoLocationCoordinator.startGeoLocation()
        }
    }
}