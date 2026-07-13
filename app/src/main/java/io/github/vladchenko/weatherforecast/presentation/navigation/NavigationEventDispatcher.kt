package io.github.vladchenko.weatherforecast.presentation.navigation

/**
 * Dispatches navigation events to the navigation controller.
 *
 * This interface provides a mechanism for sending [NavigationEvent] instances
 * to the navigation system.
 *
 * ## Usage
 * - Use [navigate] to send a navigation event from the UI layer or ViewModel.
 *
 * This design decouples the navigation source from the navigation implementation,
 * making it easier to test and maintain navigation logic.
 *
 * @see NavigationEvent
 */
interface NavigationEventDispatcher {
    /**
     * Sends a navigation event to be processed by the navigation controller.
     *
     * @param event The navigation event to be dispatched.
     */
    fun navigate(event: NavigationEvent)
}