package com.example.weatherforecast.presentation.viewmodel.cityselection

import com.example.weatherforecast.models.domain.CityDomainModel

/**
 * Sealed interface representing navigation commands emitted by [CitiesNamesViewModel].
 *
 * These are one-shot events (not state) that instruct the UI layer to perform navigation actions.
 * Should be consumed immediately and not stored.
 *
 * @see CitiesNamesViewModel.navigationEventFlow
 */
sealed interface CityNavigationEvent {
    /**
     * Request to navigate back in the navigation stack.
     */
    data object NavigateUp : CityNavigationEvent

    /**
     * Request to open the weather forecast screen for a specific city.
     *
     * Carries the selected [CityDomainModel] containing name, coordinates, and location details.
     * The UI layer should use this data to update the current city and trigger a weather forecast load.
     *
     * @property city the selected city domain model to display weather for
     */
    data class OpenWeatherFor(val city: CityDomainModel) : CityNavigationEvent
}