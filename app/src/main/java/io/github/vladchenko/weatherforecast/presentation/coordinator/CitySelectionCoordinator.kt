package io.github.vladchenko.weatherforecast.presentation.coordinator

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.geolocation.GeoLocationCallback
import io.github.vladchenko.weatherforecast.core.geolocation.GeoLocationEvent
import io.github.vladchenko.weatherforecast.core.geolocation.GeoLocationEventBus
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CityErrorEvent
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModel
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
 * @property statusStateHolder Manages and broadcasts UI status updates (info, warnings, errors).
 * @property dialogController Manages the presentation of selection and error dialogs.
 * @property forecastViewModel Source of city selection events (not found, blank input).
 * @property geoLocationEventBus Unified event bus for broadcasting geolocation-related events.
 */
class CitySelectionCoordinator(
    private val onGotoCitySelection: () -> Unit,
    private val statusStateHolder: StatusStateHolder,
    private val dialogController: WeatherDialogController,
    private val forecastViewModel: CurrentWeatherViewModel,
    private val geoLocationEventBus: GeoLocationEventBus,
) {

    /**
     * Starts observing city-related flows from [CurrentWeatherViewModel] and reacting to events.
     *
     * Launches collection of [CurrentWeatherViewModel.cityErrorEventFlow] flow for handling missing
     * city input or unknown city names.
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
                launch { collectCityErrorEventFlow(forecastViewModel.cityErrorEventFlow) }
            }
        }
    }

    /**
     * Observes the city error event flow and handles specific error scenarios.
     *
     * Responds to missing or unresolvable city input by triggering fallback strategies:
     * - For missing city input: initiates geolocation to determine the current location.
     * - For unresolvable city names: displays a warning status message and shows a dialog
     *   prompting the user to manually select a city.
     *
     * @param flow The [SharedFlow] emitting [CityErrorEvent] instances.
     */
    private suspend fun collectCityErrorEventFlow(flow: SharedFlow<CityErrorEvent>) {
        flow.collect { value ->
            when (value) {
                is CityErrorEvent.CityBlank -> {
                    statusStateHolder.updateInfoStatus(R.string.geo_detecting)
                    geoLocationEventBus.send(GeoLocationEvent.DefineDeviceLocation)
                }

                is CityErrorEvent.CityNotFound -> {
                    statusStateHolder.updateWarningStatus(
                        R.string.forecast_no_data_for_city, value.name
                    )
                    dialogController.showChosenCityNotFound(value.name) {
                        onGotoCitySelection()
                    }
                }
            }
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
         * @param callback Interface for sending geolocation events to the UI.
         * @param statusStateHolder Manages and broadcasts UI status updates.
         * @param geoLocationEventBus Unified event bus for broadcasting geolocation-related events.
         * @param dialogController Manages presentation of alert dialogs.
         * @param forecastViewModel Source of city selection events (not found, blank input).
         * @return A fully initialized [CitySelectionCoordinator] instance.
         */
        fun create(
            callback: GeoLocationCallback,
            statusStateHolder: StatusStateHolder,
            geoLocationEventBus: GeoLocationEventBus,
            dialogController: WeatherDialogController,
            forecastViewModel: CurrentWeatherViewModel,
        ): CitySelectionCoordinator {
            return CitySelectionCoordinator(
                dialogController = dialogController,
                forecastViewModel = forecastViewModel,
                statusStateHolder = statusStateHolder,
                geoLocationEventBus = geoLocationEventBus,
                onGotoCitySelection = { callback.onEvent(GeoLocationEvent.GotoCitySelection) }
            )
        }
    }
}