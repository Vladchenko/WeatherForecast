package io.github.vladchenko.weatherforecast.core.location.geolocation.api

import io.github.vladchenko.weatherforecast.models.data.network.NominatimLocationDto
import io.github.vladchenko.weatherforecast.models.data.network.NominatimReverseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API interface for interacting with the Nominatim service (OpenStreetMap).
 *
 * Nominatim provides geocoding and reverse geocoding services:
 * - Forward geocoding: Convert a place name (e.g., city) into geographic coordinates (latitude, longitude)
 * - Reverse geocoding: Convert geographic coordinates into a human-readable address or place name
 *
 * This interface is used with Retrofit to make asynchronous HTTP requests.
 * Requires proper headers (User-Agent, From) to comply with Nominatim's usage policy.
 *
 * Usage:
 * - [search] for forward geocoding (city → location)
 * - [reverse] for reverse geocoding (location → city)
 *
 * Documentation: https://nominatim.org/release-docs/latest/api/Overview/
 */
interface NominatimApi {

    /**
     * Performs forward geocoding: converts a query (e.g., city name) into geographic coordinates.
     *
     * @param query The search term (e.g., "London", "New York").
     * @param format Response format. Default is "json".
     * @param limit Maximum number of results to return. Default is 1.
     * @param email Developer contact email for identification. Required by Nominatim.
     *
     * @return List of [NominatimLocationDto] containing location data.
     *
     * Example request:
     * GET /search?q=London&format=json&limit=1&email=vladdasaev@gmail.com
     */
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 1,
        @Query("email") email: String = "contact@example.com"
    ): List<NominatimLocationDto>

    /**
     * Performs reverse geocoding: converts geographic coordinates into an address.
     *
     * @param lat Latitude of the location.
     * @param lon Longitude of the location.
     * @param format Response format. Default is "json".
     * @param email Developer contact email for identification. Required by Nominatim.
     *
     * @return [NominatimReverseDto] containing address details.
     *
     * Example request:
     * GET /reverse?lat=51.5074&lon=-0.1278&format=json&email=vladdasaev@gmail.com
     */
    @GET("reverse")
    suspend fun reverse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json",
        @Query("email") email: String = "contact@example.com"
    ): NominatimReverseDto
}