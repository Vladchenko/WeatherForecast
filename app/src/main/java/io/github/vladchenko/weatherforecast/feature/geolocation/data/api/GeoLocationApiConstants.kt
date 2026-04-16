package io.github.vladchenko.weatherforecast.feature.geolocation.data.api

object GeoLocationApiConstants {
    /**
     * Named binding for the Retrofit instance used with Nominatim (OpenStreetMap) API.
     *
     * Ensures correct injection of [NominatimApi] with proper headers and base URL.
     */
    const val NOMINATIM = "NominatimRetrofit"

    /**
     * Base URL for the Nominatim OpenStreetMap API.
     *
     * Used for reverse geocoding (coordinates → address) and forward geocoding (address → coordinates).
     * Requires proper User-Agent and From headers to avoid HTTP 403 errors.
     *
     * Documentation: https://nominatim.org/release-docs/latest/api/Search/
     */
    const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"

    /**
     * Developer contact email used for API identification.
     * Required by external services like Nominatim for rate limiting and contact purposes.
     */
    const val DEVELOPER_EMAIL = "vladdasaev@gmail.com"

    /**
     * Name of the application for use in identification headers.
     */
    const val APP_NAME = "WeatherForecastApp"

    /**
     * Current version of the application.
     */
    const val APP_VERSION = "1.0"

    /**
     * User-Agent string used in HTTP requests to identify the application.
     * Format follows RFC 7231 and complies with Nominatim's usage policy.
     *
     * Example: "WeatherForecastApp/1.0 (vladdasaev@gmail.com)"
     */
    const val USER_AGENT = "$APP_NAME/$APP_VERSION ($DEVELOPER_EMAIL)"
}