package io.github.vladchenko.weatherforecast.feature.geolocation.data.model.dto

import io.github.vladchenko.weatherforecast.core.domain.model.Coordinate

/**
 * Data Transfer Object (DTO) representing a geocoding result from the Nominatim API.
 *
 * This class holds the response data when performing forward geocoding (e.g., city name → coordinates).
 * It maps the JSON response from `https://nominatim.openstreetmap.org/search` into a Kotlin data class.
 *
 * Fields:
 * - [latitude]: Latitude as a string (e.g., "51.5074")
 * - [longitude]: Longitude as a string (e.g., "-0.1278")
 * - [displayName]: Human-readable address or place name (e.g., "London, Greater London, England, United Kingdom")
 *
 * The [toCoordinate] function converts this DTO into an Android [Coordinate] object
 * for use in location-based features within the app.
 */
data class NominatimLocationDto(
    val latitude: String,
    val longitude: String,
    val displayName: String
) {
    /**
     * Converts this [NominatimLocationDto] into an Android [Coordinate] object.
     *
     * @return A new [Coordinate] instance with latitude and longitude set.
     */
    fun toCoordinate(): Coordinate {
        return Coordinate(latitude.toDouble(), longitude.toDouble())
    }
}