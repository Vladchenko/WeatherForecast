package io.github.vladchenko.weatherforecast.core.navigation.api

import androidx.navigation.NavOptions
import io.github.vladchenko.weatherforecast.core.navigation.impl.NavigationEventDispatcher
import io.github.vladchenko.weatherforecast.core.navigation.impl.NavigationEventDispatcherImpl

/**
 * Sealed interface representing navigation events in the application.
 *
 * This interface defines all possible navigation actions that can be
 * dispatched through [NavigationEventDispatcher]. Each event represents a
 * specific navigation action, such as navigating to a screen, going back, or closing the app.
 *
 * Navigation events are typically triggered by user interactions (button presses,
 * selections) or system events and are handled by implementations
 * of [NavigationEventDispatcher] to perform actual navigation operations.
 *
 * See also:
 * - [NavigationEventDispatcher] — dispatcher interface
 * - [NavigationEventDispatcherImpl] — default implementation
 */
sealed interface NavigationEvent {

    /**
     * Navigate up to the previous screen.
     *
     * This event is triggered when the user requests to go back, for example,
     * by pressing the "Back" button on the toolbar. It should close the current
     * screen or remove it from the navigation stack.
     */
    data object NavigateUp : NavigationEvent

    /**
     * Navigate to the city selection screen.
     *
     * This event instructs the UI to open the city search or list screen,
     * allowing the user to select a new location for the weather forecast.
     * Typically triggered by an "Change City" button or toolbar action.
     *
     * @param navOptions Optional navigation parameters for custom animations and behavior.
     *                   If null, default navigation behavior is used.
     */
    data class NavigateToCitySelection(val navOptions: NavOptions? = null) : NavigationEvent

    /**
     * Navigate to the weather forecast screen for the specified city.
     *
     * This event is triggered when the user selects a city from search results
     * or recent cities list. The [city] parameter is the city name in URL-encoded format
     * (format "name, state, country"), along with [latitude] and [longitude] coordinates.
     *
     * @param city City name in URL-encoded format (e.g., "Moscow,RU")
     * @param latitude City latitude
     * @param longitude City longitude
     */
    data class ShowWeatherFor(
        val city: String,
        val latitude: Float,
        val longitude: Float
    ) : NavigationEvent

    /**
     * Close the application.
     *
     * This event is triggered when the user requests to exit the application,
     * typically through a system action or "Exit" button.
     */
    data object CloseApp : NavigationEvent

    /**
     * Navigate to the settings screen.
     *
     * This event instructs the UI to open the app settings screen,
     * allowing the user to change preferences such as temperature unit.
     * Typically triggered by a "Settings" button or toolbar action.
     */
    data object NavigateToSettings : NavigationEvent

    /**
     * Navigate to the AI weather summary screen.
     *
     * @param navOptions Optional navigation parameters for custom animations and behavior.
     *                   If null, default navigation behavior is used.
     */
    data class NavigateToAiSummary(
        val navOptions: NavOptions? = null
    ) : NavigationEvent
}