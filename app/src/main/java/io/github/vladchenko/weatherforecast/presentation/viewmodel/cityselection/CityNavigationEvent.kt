package io.github.vladchenko.weatherforecast.presentation.viewmodel.cityselection

import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel

/**
 * Sealed interface representing navigation commands emitted by [io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel.CitySearchViewModel].
 *
 * These are one-shot events (not state) that instruct the UI layer to perform navigation actions.
 * Should be consumed immediately and not stored.
 *
 * @see io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel.CitySearchViewModel.navigationEventFlow
 */
sealed interface CityNavigationEvent {

    /**
     * Request to close the application.
     *
     * This event signals that the app should terminate the current task and exit.
     * Typically triggered by user action (e.g. double back press or exit button).
     * The UI layer should call [Activity.finishAffinity] or equivalent to properly close the app.
     */
    data object CloseApp : CityNavigationEvent

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

    /**
     * Request to navigate to the city selection screen.
     *
     * This event instructs the UI to open the city search or city list screen,
     * allowing the user to pick a new location for weather forecast.
     * Typically triggered from a settings menu or a "Change City" button.
     */
    data object NavigateToCitySelection : CityNavigationEvent
}