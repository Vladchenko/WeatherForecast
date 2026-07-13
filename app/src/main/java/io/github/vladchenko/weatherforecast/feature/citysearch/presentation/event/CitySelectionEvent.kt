package io.github.vladchenko.weatherforecast.feature.citysearch.presentation.event

/**
 * Sealed interface representing user actions in the city selection screen.
 *
 * This class is used to communicate UI events from the Compose UI layer to the [CitySearchViewModel]
 * in a type-safe and centralized manner. Each event corresponds to a specific user interaction,
 * such as typing a query, selecting a city, or navigating back.
 *
 * Using a sealed interface ensures:
 * - Exhaustive handling of all possible events via `when` expressions
 * - Clear separation between UI actions and business logic
 * - Easier testing and maintenance
 *
 * Events are consumed by calling [CitySearchViewModel.onCitySelectionEvent] method.
 */
sealed interface CitySelectionEvent {
    /**
     * Clears the city search input (mask) and hides city suggestions.
     * Should be used when user clears input or selects a city.
     */
    data object ClearQuery : CitySelectionEvent

    /**
     * Loads set of cities searched recently
     */
    data object LoadRecentCities : CitySelectionEvent

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
     * Requests clearing of the recently used cities cache.
     *
     * Triggered by user action (e.g., tapping "Clear history" in UI).
     * Should reset recent cities list and persist the change.
     */
    object ClearRecentCities : CitySelectionEvent
}