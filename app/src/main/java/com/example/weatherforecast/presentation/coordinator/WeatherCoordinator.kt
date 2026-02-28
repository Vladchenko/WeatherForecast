package com.example.weatherforecast.presentation.coordinator

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
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
 * Coordinates high-level UI logic for the weather forecast screen.
 *
 * Acts as the central orchestrator that connects view models with presentation components,
 * translating state changes into appropriate UI actions. Designed to keep fragments and activities
 * free of coordination logic, improving testability and separation of concerns.
 *
 * ## Responsibilities
 * - Observes message flows from weather view models and updates status via [StatusRenderer]
 * - Synchronizes app bar state with current weather UI state
 * - Delegates specialized workflows to dedicated coordinators:
 *   - [GeoLocationCoordinator] handles location-based forecast retrieval
 *   - [CitySelectionCoordinator] manages fallback strategies for invalid or missing city input
 *
 * ## Lifecycle Management
 * Uses [repeatOnLifecycle] to ensure observation occurs only during active lifecycle states,
 * preventing memory leaks and unnecessary processing. Must be started with [startObserving].
 *
 * ## Thread Safety
 * All coroutine collections occur on the main thread via [lifecycle.repeatOnLifecycle],
 * ensuring safe access to UI components.
 *
 * @property statusRenderer Displays transient statuses (loading, success, error)
 * @property appBarViewModel Controls top app bar appearance based on weather state
 * @property hourlyViewModel Source of hourly forecast data and related messages
 * @property forecastViewModel Main source of current weather data and user actions
 * @property geoLocationCoordinator Handles geolocation-specific workflow and user interactions
 * @property citySelectionCoordinator Manages responses to blank or invalid city input
 */
class WeatherCoordinator private constructor(
    private val statusRenderer: StatusRenderer,
    private val appBarViewModel: AppBarViewModel,
    private val hourlyViewModel: HourlyWeatherViewModel,
    private val forecastViewModel: CurrentWeatherViewModel,
    private val geoLocationCoordinator: GeoLocationCoordinator,
    private val citySelectionCoordinator: CitySelectionCoordinator
) {

    /**
     * Starts observing all relevant state and event flows from associated view models.
     *
     * Launches concurrent coroutines to collect:
     * - Message events from both current and hourly weather view models
     * - Changes in overall weather UI state for app bar synchronization
     *
     * Also initializes observation in:
     * - [geoLocationCoordinator] — for location-based forecast workflow
     * - [citySelectionCoordinator] — for handling missing or invalid city input
     *
     * Observation is scoped to [Lifecycle.State.STARTED] to prevent unnecessary work
     * while the UI is not visible.
     *
     * @param scope The coroutine scope used to launch observation tasks
     * @param lifecycle The lifecycle to which observation is bound
     */
    fun startObserving(scope: CoroutineScope, lifecycle: Lifecycle) {
        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectMessageFlow(forecastViewModel.messageSharedFlow) }
                launch { collectMessageFlow(hourlyViewModel.messageSharedFlow) }
                launch { collectForecastState(forecastViewModel.forecastStateFlow) }
            }
        }

        geoLocationCoordinator.startObserving(scope, lifecycle)
        citySelectionCoordinator.startObserving(scope, lifecycle)
    }

    /**
     * Collects messages emitted from shared flows and updates the UI status accordingly.
     *
     * Used by both current and hourly forecast view models to display transient feedback
     * such as loading indicators, errors, or success confirmations.
     *
     * Delegates actual rendering to [statusRenderer.updateFromMessage].
     *
     * @param flow A shared flow emitting [Message] instances representing UI feedback
     */
    private suspend fun collectMessageFlow(flow: SharedFlow<Message>) {
        flow.collect { statusRenderer.updateFromMessage(it) }
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
     * including creation of:
     * - [GeoLocationCoordinator]
     * - [CitySelectionCoordinator]
     *
     * Promotes loose coupling and testability by allowing full control over dependencies.
     */
    class Factory {
        /**
         * Creates and returns a fully configured [WeatherCoordinator] instance.
         *
         * Initializes both [GeoLocationCoordinator] and [CitySelectionCoordinator]
         * with appropriate callbacks and shared dependencies.
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
                geoLocationViewModel = geoLocationViewModel,
                permissionResolver = permissionResolver,
                statusRenderer = statusRenderer,
                dialogController = dialogController,
                resourceManager = resourceManager,
                onGotoCitySelection = onGotoCitySelection,
                onRequestLocationPermission = onRequestLocationPermission,
                onPermanentlyDenied = onPermanentlyDenied,
                onNegativeNoPermission = onNegativeNoPermission,
                onForecastLoadForLocation = { locationModel ->
                    forecastViewModel.loadRemoteForecastForLocation(locationModel)
                }
            )

            val citySelectionCoordinator = CitySelectionCoordinator(
                forecastViewModel = forecastViewModel,
                geoLocationCoordinator = geoLocationCoordinator,
                statusRenderer = statusRenderer,
                dialogController = dialogController,
                resourceManager = resourceManager,
                onGotoCitySelection = onGotoCitySelection
            )

            return WeatherCoordinator(
                statusRenderer = statusRenderer,
                hourlyViewModel = hourlyViewModel,
                appBarViewModel = appBarViewModel,
                forecastViewModel = forecastViewModel,
                geoLocationCoordinator = geoLocationCoordinator,
                citySelectionCoordinator = citySelectionCoordinator
            )
        }
    }
}