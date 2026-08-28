package io.github.vladchenko.weatherforecast.core.domain.model

import javax.annotation.concurrent.Immutable

/**
 * Data model for city geo location.
 *
 * @property name city name
 * @property latitude latitude for city
 * @property longitude longitude for city
 * @property country that city located in
 * @property state that city located in
 */
@Immutable
data class CityModel(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val state: String? = null,
) {
    /**
     * Unique identifier for the city based on its full location data.
     *
     * Composed of: name, state, country, latitude, and longitude, joined by '|'.
     * This ensures uniqueness even for cities with the same name in different regions.
     *
     * Used as a stable key in composables (e.g., LazyColumn) to prevent unnecessary recompositions
     * and maintain state across list updates.
     *
     * Example: "Paris||FR|48.8566|2.3522" for Paris, France
     *          "Paris|Texas|US|33.6609|-95.5555" for Paris, Texas, USA
     */
    val id: String
        get() = "$name|$state|$country|$latitude|$longitude"
}
