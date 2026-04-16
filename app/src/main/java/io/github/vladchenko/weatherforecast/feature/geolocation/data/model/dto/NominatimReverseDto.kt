package io.github.vladchenko.weatherforecast.feature.geolocation.data.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object (DTO) representing a reverse geocoding result from the Nominatim API.
 *
 * This class holds the response data when performing reverse geocoding (coordinates → address).
 * It maps the JSON response from `https://nominatim.openstreetmap.org/reverse` into a Kotlin data class.
 *
 * Fields:
 * - [displayName]: Full human-readable address (e.g., "London, Greater London, England, United Kingdom")
 * - [address]: Nested object containing structured address components such as city, town, village, etc.
 *
 * The [Address] inner class provides access to individual parts of the address, with fallback logic
 * in [getCityOrLocality] to return the most specific populated place name available.
 */
data class NominatimReverseDto(
    val displayName: String,
    val address: Address
) {
    /**
     * Inner class representing the structured address part of the reverse geocoding response.
     *
     * Each field corresponds to a geographic administrative level. The actual value present depends
     * on the location and data availability in OpenStreetMap.
     */
    data class Address(
        @SerializedName("city") val city: String? = null,
        @SerializedName("town") val town: String? = null,
        @SerializedName("village") val village: String? = null,
        @SerializedName("hamlet") val hamlet: String? = null,
        @SerializedName("state") val state: String? = null,
        @SerializedName("country") val country: String? = null,
        @SerializedName("county") val county: String? = null
    ) {
        /**
         * Returns the most specific populated place name available from the address components.
         *
         * Priority order: city → town → village → hamlet → county → state → country.
         * If none are present, returns "Unknown".
         *
         * @return The name of the locality or "Unknown" if no known place name is found.
         */
        fun getCityOrLocality(): String {
            return city ?: town ?: village ?: hamlet ?: county ?: state ?: country ?: "Unknown"
        }
    }
}