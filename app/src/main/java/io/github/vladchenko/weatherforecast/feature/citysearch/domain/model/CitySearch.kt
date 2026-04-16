package io.github.vladchenko.weatherforecast.feature.citysearch.domain.model

import kotlinx.collections.immutable.ImmutableList
import javax.annotation.concurrent.Immutable

/**
 * Data model for cities names retrieval.
 *
 * @property cities data models list
 * @property error message if cities list failed to be fetched
 */
@Immutable
data class CitySearch(
    val cities: ImmutableList<CityDomainModel>,
    val error: String
)

/**
 * Data model for city geo location.
 *
 * @property name city name
 * @property lat latitude for city
 * @property lon longitude for city
 * @property country that city located in
 * @property state that city located in
 */
data class CityDomainModel(
    val name: String,
    val lat: Double,
    val lon: Double,
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
        get() = "$name|$state|$country|$lat|$lon"
}
