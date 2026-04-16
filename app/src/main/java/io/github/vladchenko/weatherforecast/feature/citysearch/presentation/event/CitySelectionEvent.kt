package io.github.vladchenko.weatherforecast.feature.citysearch.presentation.event

import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel

/**
 * Sealed interface representing user actions in the city selection screen.
 *
 * This class is used to communicate UI events from the Compose UI layer to the [io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel.CitySearchViewModel]
 * in a type-safe and centralized manner. Each event corresponds to a specific user interaction,
 * such as typing a query, selecting a city, or navigating back.
 *
 * Using a sealed interface ensures:
 * - Exhaustive handling of all possible events via `when` expressions
 * - Clear separation between UI actions and business logic
 * - Easier testing and maintenance
 *
 * Events are consumed by calling [io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel.CitySearchViewModel.onEvent] method.
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
     * A city has been selected from the suggestions list or recent cities.
     *
     * This event is triggered when the user taps on a predicted city name or selects one from the recent list.
     * It carries the full domain model of the selected city, including coordinates and location details,
     * which should be used to update the current city in the app and load its weather forecast.
     *
     * After processing, the query should typically be cleared and the keyboard dismissed.
     *
     * @property city The selected [io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel] containing name, latitude, longitude, country, and state
     */
    data class SelectCity(val city: CityDomainModel) : CitySelectionEvent
}