package com.example.weatherforecast.models.data.network

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
/**
 * Data Transfer Object (DTO) representing a single city search result from the network API.
 *
 * This class is used to deserialize JSON responses containing city information
 * returned by the location search endpoint.
 *
 * @property name The name of the city (e.g., "London").
 * @property lat The latitude of the city's coordinates.
 * @property lon The longitude of the city's coordinates.
 * @property country The country code (e.g., "GB") where the city is located.
 * @property state Optional state or province name (e.g., "California"), if applicable.
 */
@Serializable
@InternalSerializationApi
data class CitiesSearchResultDto(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
)