package com.example.weatherforecast.presentation.viewmodel.cityselection

/**
 * Sealed interface representing user actions in the city selection screen.
 *
 * This class is used to communicate UI events from the Compose UI layer to the [CitiesNamesViewModel]
 * in a type-safe and centralized manner. Each event corresponds to a specific user interaction,
 * such as typing a query, selecting a city, or navigating back.
 *
 * Using a sealed interface ensures:
 * - Exhaustive handling of all possible events via `when` expressions
 * - Clear separation between UI actions and business logic
 * - Easier testing and maintenance
 *
 * Events are consumed by calling [CitiesNamesViewModel.onEvent] method.
 */
sealed interface CitySelectionEvent {
    /**
     * Clears the city search input (mask) and hides city suggestions.
     * Should be used when user clears input or selects a city.
     */
    data object ClearQuery : CitySelectionEvent

    /**
     * User requested to navigate back (e.g. clicked "Up" button in toolbar).
     * Should trigger screen dismissal or navigation up in the back stack.
     */
    data object NavigateUp : CitySelectionEvent

    /**
     * The user has updated the city search query.
     *
     * This event is typically emitted on each keystroke (debounced) and triggers
     * an update of the suggestion list based on the current input.
     *
     * @property mask The current text entered by the user for city name filtering
     */
    data class UpdateQuery(val mask: String) : CitySelectionEvent

    /**
     * A city has been selected from the suggestions list.
     *
     * This event is triggered when the user taps on a predicted city name.
     * Should result in navigation to the weather forecast screen for the selected city.
     *
     * @property city Full name of the selected city (e.g., "Kazan, Tatarstan, RU")
     */
    data class SelectCity(val city: String) : CitySelectionEvent
}