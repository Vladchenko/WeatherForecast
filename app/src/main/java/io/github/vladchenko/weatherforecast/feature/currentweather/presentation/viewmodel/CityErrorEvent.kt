package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel

/**
 * Sealed class representing error events related to city selection failures.
 *
 * Used to notify the UI layer about problems with city lookup. Each variant
 * indicates a different failure scenario, enabling exhaustive handling in `when` expressions.
 */
sealed class CityErrorEvent {

    /**
     * Indicates that no city was provided and no saved city exists.
     */
    object CityBlank : CityErrorEvent()

    /**
     * Indicates that the requested city could not be resolved from API or cache.
     *
     * @param name the name of the city that was not found
     */
    data class CityNotFound(val name: String) : CityErrorEvent()
}