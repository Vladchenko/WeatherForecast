package com.example.weatherforecast.presentation.viewmodel.cityselection

/**
 * Sealed interface representing navigation commands emitted by [CitiesNamesViewModel].
 *
 * These are one-shot events (not state) that instruct the UI layer to perform navigation actions.
 * Should be consumed immediately and not stored.
 *
 * @see CitiesNamesViewModel.navigationEvent
 */
sealed interface CityNavigationEvent {
    /**
     * Request to navigate back in the navigation stack.
     */
    data object NavigateUp : CityNavigationEvent

    /**
     * Request to open weather forecast screen for a specific city.
     *
     * @property city Full display name of the city (e.g., "Kazan, Tatarstan, RU")
     */
    data class OpenWeatherFor(val city: String) : CityNavigationEvent
}