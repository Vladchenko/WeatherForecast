package io.github.vladchenko.weatherforecast.feature.geolocation.data.model.dto

import android.location.Location

/**
 * Data Transfer Object (DTO) representing a geocoding result from the Nominatim API.
 *
 * This class holds the response data when performing forward geocoding (e.g., city name → coordinates).
 * It maps the JSON response from `https://nominatim.openstreetmap.org/search` into a Kotlin data class.
 *
 * Fields:
 * - [lat]: Latitude as a string (e.g., "51.5074")
 * - [lon]: Longitude as a string (e.g., "-0.1278")
 * - [displayName]: Human-readable address or place name (e.g., "London, Greater London, England, United Kingdom")
 *
 * The [toAndroidLocation] function converts this DTO into an Android [Location] object
 * for use in location-based features within the app.
 */
data class NominatimLocationDto(
    val lat: String,
    val lon: String,
    val displayName: String
) {
    /**
     * Converts this [NominatimLocationDto] into an Android [Location] object.
     *
     * @return A new [Location] instance with latitude and longitude set.
     */
    fun toAndroidLocation(): Location {
        return Location("").apply {
            latitude = lat.toDouble()
            longitude = lon.toDouble()
        }
    }
}