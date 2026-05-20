package io.github.vladchenko.weatherforecast.core.model

/**
 * Sealed interface representing the possible screens in the app's navigation flow.
 *
 * Each implementation corresponds to a top-level destination in the UI.
 */
sealed interface CurrentScreen {
    /**
     * Represents the weather display screen.
     * Shows current weather information for the selected city.
     */
    object Weather : CurrentScreen

    /**
     * Represents the city selection screen.
     * Allows the user to choose or change the city for weather forecasting.
     */
    object CitySelection : CurrentScreen
}