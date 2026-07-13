package io.github.vladchenko.weatherforecast.presentation.navigation

import androidx.navigation.NavOptions
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel

/**
 * Sealed interface representing navigation events in the application.
 *
 * This interface defines all possible navigation actions that can be dispatched
 * through the [NavigationEventDispatcher]. Each event represents a specific
 * navigation action such as navigating to a screen, going back, or closing the app.
 *
 * Navigation events are typically triggered by user interactions (button clicks,
 * selections) or system events, and are handled by implementations of
 * [NavigationEventDispatcher] to perform the actual navigation operations.
 *
 * See Also:
 * - [NavigationEventDispatcher] for the dispatcher interface
 * - [NavigationEventDispatcherImpl] for the default implementation
 */
sealed interface NavigationEvent {

    /**
     * Navigates back to the previous screen.
     *
     * This event is triggered when the user requests to go back, for example,
     * by clicking the "Up" button in the toolbar. It should dismiss the current
     * screen or pop the back stack.
     */
    data object NavigateUp : NavigationEvent

    /**
     * Navigates to the city selection screen.
     *
     * This event instructs the UI to open the city search or city list screen,
     * allowing the user to pick a new location for the weather forecast.
     * Typically triggered from a "Change City" button or action in the toolbar.
     *
     * @param navOptions Optional navigation options for custom animations and behavior.
     *                   If null, default navigation behavior is used.
     */
    data class NavigateToCitySelection(val navOptions: NavOptions? = null) : NavigationEvent

    /**
     * Navigates to the weather forecast screen for the specified city.
     *
     * This event is triggered when the user selects a city from the search results
     * or recent cities list. The [city] parameter contains all the necessary data
     * (name, state, country, coordinates) to display the weather for that location.
     *
     * @param city The domain model of the selected city containing its details.
     */
    data class ShowWeatherFor(val city: CityDomainModel) : NavigationEvent

    /**
     * Closes the application.
     *
     * This event is triggered when the user requests to exit the app,
     * typically from a system-level action or a dedicated "Exit" button.
     */
    data object CloseApp : NavigationEvent
}
