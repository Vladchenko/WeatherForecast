package io.github.vladchenko.weatherforecast.presentation.coordinator

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.ui.status.StatusType
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModel
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationCallback
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationCallbackEvent
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * Coordinates user actions and fallback strategies related to city selection.
 *
 * This class acts as an intermediary between [CurrentWeatherViewModel] and the UI layer,
 * handling edge cases where city selection fails or input is missing. It provides
 * a clean separation between navigation logic and city selection resolution.
 *
 * ## Key Scenarios
 * 1. **City Not Found**: When the user searches for a city that doesn't exist in the weather API database.
 *    - Displays a warning status message with the city name.
 *    - Shows a dialog offering to navigate to manual city selection.
 *
 * 2. **Blank City Input**: When the user attempts to load weather without providing a city name,
 *    and no previously saved city exists.
 *    - Automatically triggers geolocation via [GeoLocationCoordinator] as a fallback strategy.
 *
 * ## Lifecycle Awareness
 * All flow observation is scoped to [Lifecycle.State.STARTED] to prevent memory leaks
 * and unnecessary work while the UI is not visible.
 *
 * @property onGotoCitySelection Callback triggered when the user needs to manually select a city.
 * @property resourceManager Provides localized string resources for UI messages.
 * @property statusStateHolder Manages and broadcasts UI status updates (info, warnings, errors).
 * @property dialogController Manages the presentation of selection and error dialogs.
 * @property forecastViewModel Source of city selection events (not found, blank input).
 * @property geoLocationCoordinator Handles geolocation-based city resolution as a fallback.
 *
 * @see CurrentWeatherViewModel
 * @see GeoLocationCoordinator
 * @see WeatherDialogController
 */
class CitySelectionCoordinator(
    private val onGotoCitySelection: () -> Unit,
    private val resourceManager: ResourceManager,
    private val statusStateHolder: StatusStateHolder,
    private val dialogController: WeatherDialogController,
    private val forecastViewModel: CurrentWeatherViewModel,
    private val geoLocationCoordinator: GeoLocationCoordinator
) {

    /**
     * Starts observing city-related flows from [CurrentWeatherViewModel] and reacting to events.
     *
     * Launches collection of two flows:
     * - [CurrentWeatherViewModel.chosenCityNotFoundStateFlow] — for handling unknown city names.
     * - [CurrentWeatherViewModel.chosenCityBlankStateFlow] — for handling missing city input.
     *
     * All observation occurs within [Lifecycle.State.STARTED] to ensure lifecycle safety
     * and prevent memory leaks.
     *
     * @param scope The coroutine scope (typically provided by the Activity/Fragment) for launching collectors.
     * @param lifecycle The lifecycle of the UI component to bind observation to.
     */
    fun startObserving(scope: CoroutineScope, lifecycle: Lifecycle) {
        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectChosenCityNotFoundFlow(forecastViewModel.chosenCityNotFoundStateFlow) }
                launch { collectChosenCityBlankFlow(forecastViewModel.chosenCityBlankStateFlow) }
            }
        }
    }

    /**
     * Handles events when a searched city is not found by the weather API.
     *
     * Displays a warning status message via [StatusStateHolder] containing the unknown city name,
     * then shows a dialog ([WeatherDialogController.showChosenCityNotFound]) offering the user
     * the option to navigate to manual city selection.
     *
     * @param flow The [SharedFlow] emitting unknown city names.
     */
    private suspend fun collectChosenCityNotFoundFlow(flow: SharedFlow<String>) {
        flow.collect { city ->
            statusStateHolder.updateStatus(
                StatusType.Warning(
                    resourceManager.getString(R.string.forecast_no_data_for_city, city)
                )
            )
            dialogController.showChosenCityNotFound(city) {
                onGotoCitySelection()
            }
        }
    }

    /**
     * Handles events when the user attempts to load weather without providing a city name
     * and no saved city exists.
     *
     * As a fallback strategy, triggers geolocation via [GeoLocationCoordinator.startGeoLocation]
     * to automatically detect the user's current location and resolve the city from coordinates.
     *
     * @param flow The [SharedFlow] emitting blank city events.
     */
    private suspend fun collectChosenCityBlankFlow(flow: SharedFlow<Unit>) {
        flow.collect {
            geoLocationCoordinator.startGeoLocation()
        }
    }

    /**
     * Factory class for creating configured instances of [CitySelectionCoordinator].
     *
     * Encapsulates dependency injection logic and ensures proper wiring of internal components,
     * promoting loose coupling and testability.
     */
    class Factory {
        /**
         * Creates and returns a fully configured [CitySelectionCoordinator] instance.
         *
         * Initializes the coordinator with all required dependencies:
         * - [GeoLocationCallback] for event propagation (wires navigation through [GeoLocationCallbackEvent.GotoCitySelection]).
         * - [ResourceManager] for localized string resources.
         * - [StatusStateHolder] for broadcasting UI status updates.
         * - [WeatherDialogController] for managing dialog presentation.
         * - [CurrentWeatherViewModel] as the source of city selection events.
         * - [GeoLocationCoordinator] for handling geolocation-based city resolution as a fallback.
         *
         * @param callback Interface for sending geolocation events to the UI.
         * @param resourceManager Provides localized string resources.
         * @param statusStateHolder Manages and broadcasts UI status updates.
         * @param dialogController Manages presentation of alert dialogs.
         * @param forecastViewModel Source of city selection events (not found, blank input).
         * @param geoLocationCoordinator Handles geolocation-based city resolution as a fallback.
         * @return A fully initialized [CitySelectionCoordinator] instance.
         */
        fun create(
            callback: GeoLocationCallback,
            resourceManager: ResourceManager,
            statusStateHolder: StatusStateHolder,
            dialogController: WeatherDialogController,
            forecastViewModel: CurrentWeatherViewModel,
            geoLocationCoordinator: GeoLocationCoordinator,
        ): CitySelectionCoordinator {
            return CitySelectionCoordinator(
                resourceManager = resourceManager,
                dialogController = dialogController,
                forecastViewModel = forecastViewModel,
                statusStateHolder = statusStateHolder,
                geoLocationCoordinator = geoLocationCoordinator,
                onGotoCitySelection = { callback.onEvent(GeoLocationCallbackEvent.GotoCitySelection) }
            )
        }
    }
}