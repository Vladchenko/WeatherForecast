package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.event

/**
 * Sealed interface representing navigation and UI events emitted by [CurrentWeatherViewModel].
 *
 * These are one-shot events (not state) that instruct the UI layer to perform navigation or side effects.
 * Should be consumed immediately and not stored.
 */
sealed interface CurrentWeatherEvent {

    /**
     * User requested to navigate back (e.g. clicked "Up" button in toolbar).
     * Should trigger screen dismissal or navigation up in the back stack.
     */
    data object NavigateUp : CurrentWeatherEvent

    /**
     * Request to navigate to the city selection screen.
     *
     * This event instructs the UI to open the city search or city list screen,
     * allowing the user to pick a new location for weather forecast.
     * Typically triggered from a "Change City" button or action in the toolbar.
     */
    data object NavigateToCitySelection : CurrentWeatherEvent

    /**
     * Request to reload current weather data.
     *
     * Typically triggered via pull-to-refresh gesture or manual refresh action.
     * Results in [CurrentWeatherViewModel] re-fetching data from repository and updating state.
     */
    object RefreshWeather: CurrentWeatherEvent
}