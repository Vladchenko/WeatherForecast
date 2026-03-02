package com.example.weatherforecast.data.api

/**
 * Object containing constant API endpoints and configuration values used throughout the application.
 *
 * These constants are used for:
 * - Retrofit service interface base URLs and paths
 * - HTTP request headers (e.g., User-Agent)
 * - External API integration (OpenWeatherMap, Nominatim)
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

    /**
     * Endpoint for retrieving current weather data for a specific location.
     *
     * Accepts coordinates (lat/lon), city name, or city ID.
     * Returns a JSON object with temperature, humidity, wind speed, and weather condition.
     *
     * Example: `data/2.5/weather?q=London&appid={API key}`
     */
    const val CURRENT_WEATHER = "data/2.5/weather"

    /**
     * Endpoint for retrieving 5-day / 3-hour forecast data.
     *
     * Provides multiple weather snapshots every 3 hours for the next 5 days.
     * Used to populate the hourly forecast section in the UI.
     *
     * Example: `data/2.5/forecast?q=London&appid={API key}`
     */
    const val HOURLY_WEATHER = "data/2.5/forecast"

    /**
     * Name of the application for use in identification headers.
     */
    const val APP_NAME = "WeatherForecastApp"

    /**
     * Current version of the application.
     */
    const val APP_VERSION = "1.0"

    /**
     * Developer contact email used for API identification.
     * Required by external services like Nominatim for rate limiting and contact purposes.
     */
    const val DEVELOPER_EMAIL = "vladdasaev@gmail.com"

    /**
     * User-Agent string used in HTTP requests to identify the application.
     * Format follows RFC 7231 and complies with Nominatim's usage policy.
     *
     * Example: "WeatherForecastApp/1.0 (vladdasaev@gmail.com)"
     */
    const val USER_AGENT = "$APP_NAME/$APP_VERSION ($DEVELOPER_EMAIL)"

    /**
     * Base URL for the Nominatim OpenStreetMap API.
     *
     * Used for reverse geocoding (coordinates → address) and forward geocoding (address → coordinates).
     * Requires proper User-Agent and From headers to avoid HTTP 403 errors.
     *
     * Documentation: https://nominatim.org/release-docs/latest/api/Search/
     */
    const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
}