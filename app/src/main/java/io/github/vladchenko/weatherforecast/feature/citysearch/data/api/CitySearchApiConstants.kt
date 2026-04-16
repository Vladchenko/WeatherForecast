package io.github.vladchenko.weatherforecast.feature.citysearch.data.api

/**
 * Constants for API endpoints used in city search functionality.
 *
 * This object holds endpoint paths for the OpenWeatherMap Geocoding API,
 * which is used to convert city names into geographic coordinates (latitude, longitude).
 */
object ApiConstants {
    /**
     * Endpoint for the OpenWeatherMap Geocoding API (Direct geocoding).
     *
     * Converts a city name into geographic coordinates (latitude and longitude).
     * Used when the user inputs a city name manually.
     *
     * Example: `geo/1.0/direct?q=London&appid={API key}`
     */
    const val GEO_DIRECT = "geo/1.0/direct"
}