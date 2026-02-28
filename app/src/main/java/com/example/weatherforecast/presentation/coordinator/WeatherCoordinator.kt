package com.example.weatherforecast.presentation.coordinator

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.weatherforecast.R
import com.example.weatherforecast.geolocation.PermissionResolver
import com.example.weatherforecast.models.presentation.Message
import com.example.weatherforecast.presentation.alertdialog.dialogcontroller.WeatherDialogController
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.CurrentWeatherViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.HourlyWeatherViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherUiState
import com.example.weatherforecast.presentation.viewmodel.geolocation.GeoLocationViewModel
import com.example.weatherforecast.utils.ResourceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Coordinates UI logic between multiple view models and presentation components.
 *
 * Acts as a central orchestrator that observes state and events from various view models
 * ([CurrentWeatherViewModel], [HourlyWeatherViewModel], [GeoLocationViewModel]) and translates them
 * into appropriate UI actions such as status updates, dialog displays, or navigation triggers.
 *
 * ## Responsibilities
 * - Observes message flows for displaying transient UI feedback (success, error, warning)
 * - Handles special cases like missing or invalid city input
 * - Updates the app bar based on current weather state
 * - Delegates geolocation coordination to [GeoLocationCoordinator]
 * - Ensures lifecycle-safe observation using [repeatOnLifecycle]
 *
 * ## Design Pattern
 * Implements the **Coordinator pattern** to decouple navigation and side-effect handling
 * from UI controllers (Activities/Fragments), improving testability and separation of concerns.
 *
 * ## Thread Safety
 * All coroutine collections occur on the main thread via [repeatOnLifecycle], ensuring safe access
 * to UI components. Designed to be launched within a lifecycle-aware scope (e.g., `lifecycleScope`).
 *
 * @property statusRenderer Responsible for displaying statuses (loading, success, error) in the UI
 * @property appBarViewModel Controls the state of the application's top app bar
 * @property resourceManager Provides access to string resources for dynamic message construction
 * @property hourlyViewModel Source of hourly forecast data and related messages
 * @property dialogController Manages presentation of alert dialogs to the user
 * @property forecastViewModel Source of current weather data, UI state, and user-initiated actions
 * @property geoLocationCoordinator Handles geolocation-specific workflow and user interactions
 */
class WeatherCoordinator private constructor(
    private val statusRenderer: StatusRenderer,
    private val appBarViewModel: AppBarViewModel,
    private val resourceManager: ResourceManager,
    private val hourlyViewModel: HourlyWeatherViewModel,
    private val dialogController: WeatherDialogController,
    private val forecastViewModel: CurrentWeatherViewModel,
    private val geoLocationCoordinator: GeoLocationCoordinator
) {

    /**
     * Starts observing all relevant state and event flows from associated view models.
     *
     * Launches concurrent coroutines to collect:
     * - Message events from both current and hourly weather view models
     * - City not found and blank city input states
     * - Changes in overall weather UI state for app bar synchronization
     *
     * Also initializes observation in the [geoLocationCoordinator] to ensure full geolocation
 * workflow is active.
     *
     * Observation is scoped to [Lifecycle.State.STARTED] to prevent unnecessary processing
     * when the UI is not visible.
     *
     * @param scope The coroutine scope used to launch observation tasks
     * @param lifecycle The lifecycle to which observation is bound
     */
    fun startObserving(scope: CoroutineScope, lifecycle: Lifecycle) {
        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectMessageFlow(forecastViewModel.messageSharedFlow) }
                launch { collectMessageFlow(hourlyViewModel.messageSharedFlow) }
                launch { collectChosenCityNotFoundFlow(forecastViewModel.chosenCityNotFoundStateFlow) }
                launch { collectChosenCityBlankFlow(forecastViewModel.chosenCityBlankStateFlow) }
                launch { collectForecastState(forecastViewModel.forecastStateFlow) }
            }
        }

        geoLocationCoordinator.startObserving(scope, lifecycle)
    }

    /**
     * Collects messages emitted from shared flows and updates the UI status accordingly.
     *
     * Used by both current and hourly forecast view models to display transient feedback
     * such as loading indicators, errors, or success confirmations.
     *
     * @param flow A shared flow emitting [Message] instances representing UI feedback
     */
    private suspend fun collectMessageFlow(flow: SharedFlow<Message>) {
        flow.collect { statusRenderer.updateFromMessage(it) }
    }

    /**
     * Observes events where the requested city could not be found in the forecast data.
     *
     * When triggered, shows a warning status and presents a dialog offering the user
     * options to retry with geolocation or select a city manually.
     *
     * @param flow A shared flow emitting the name of the city that was not found
     */
    private suspend fun collectChosenCityNotFoundFlow(flow: SharedFlow<String>) {
        flow.collect { city ->
            statusRenderer.showWarning(
                resourceManager.getString(R.string.no_selected_city_forecast, city)
            )
            dialogController.showChosenCityNotFound(city) {
                geoLocationCoordinator.startGeoLocation()
            }
        }
    }

    /**
     * Observes events indicating that the user attempted to search with an empty query.
     *
     * Triggers automatic fallback to geolocation-based forecast retrieval.
     *
     * @param flow A shared flow emitting a unit event when blank input is detected
     */
    private suspend fun collectChosenCityBlankFlow(flow: SharedFlow<Unit>) {
        flow.collect {
            geoLocationCoordinator.startGeoLocation()
        }
    }

    /**
     * Observes changes in the overall weather UI state and synchronizes the app bar.
     *
     * Keeps the top app bar (title, icons, etc.) in sync with the current weather data
     * and loading state.
     *
     * @param flow A state flow representing the current [WeatherUiState]
     */
    private suspend fun collectForecastState(flow: StateFlow<WeatherUiState>) {
        flow.collect { state ->
            appBarViewModel.updateAppBarState(state)
        }
    }

    /**
     * Factory class for creating configured instances of [WeatherCoordinator].
     *
     * Encapsulates dependency injection logic and ensures proper wiring of internal components,
     * especially the creation and configuration of [GeoLocationCoordinator].
     *
     * Promotes loose coupling and testability by allowing full control over dependencies.
     */
    class Factory {
        /**
         * Creates and returns a fully configured [WeatherCoordinator] instance.
         *
         * Initializes the [GeoLocationCoordinator] with appropriate callbacks that trigger
         * forecast loading upon successful location resolution.
         *
         * @param statusRenderer Renderer for displaying status messages
         * @param appBarViewModel ViewModel controlling app bar appearance
         * @param resourceManager Accessor for localized string resources
         * @param permissionResolver Handles runtime location permission requests
         * @param hourlyViewModel Provides hourly forecast data and messages
         * @param dialogController Manages presentation of alert dialogs
         * @param forecastViewModel Main source of current weather data and user actions
         * @param geoLocationViewModel Provides geolocation state and operations
         * @param onGotoCitySelection Callback invoked to navigate to city selection screen
         * @param onPermanentlyDenied Callback for handling permanent denial of location permission
         * @param onNegativeNoPermission Callback when user declines permission without retrying
         * @param onRequestLocationPermission Callback to request location permission from system
         * @return A fully initialized and wired [WeatherCoordinator] instance
         */
        fun create(
            statusRenderer: StatusRenderer,
            appBarViewModel: AppBarViewModel,
            resourceManager: ResourceManager,
            permissionResolver: PermissionResolver,
            hourlyViewModel: HourlyWeatherViewModel,
            dialogController: WeatherDialogController,
            forecastViewModel: CurrentWeatherViewModel,
            geoLocationViewModel: GeoLocationViewModel,
            onGotoCitySelection: () -> Unit,
            onPermanentlyDenied: () -> Unit,
            onNegativeNoPermission: () -> Unit,
            onRequestLocationPermission: () -> Unit
        ): WeatherCoordinator {
            val geoLocationCoordinator = GeoLocationCoordinator(
                statusRenderer = statusRenderer,
                resourceManager = resourceManager,
                dialogController = dialogController,
                permissionResolver = permissionResolver,
                onGotoCitySelection = onGotoCitySelection,
                onPermanentlyDenied = onPermanentlyDenied,
                geoLocationViewModel = geoLocationViewModel,
                onNegativeNoPermission = onNegativeNoPermission,
                onRequestLocationPermission = onRequestLocationPermission,
                onForecastLoadForLocation = { locationModel ->
                    forecastViewModel.loadRemoteForecastForLocation(locationModel)
                }
            )

            return WeatherCoordinator(
                statusRenderer = statusRenderer,
                hourlyViewModel = hourlyViewModel,
                appBarViewModel = appBarViewModel,
                resourceManager = resourceManager,
                dialogController = dialogController,
                forecastViewModel = forecastViewModel,
                geoLocationCoordinator = geoLocationCoordinator
            )
        }
    }
}