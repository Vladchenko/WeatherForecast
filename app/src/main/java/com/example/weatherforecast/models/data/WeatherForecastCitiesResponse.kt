package com.example.weatherforecast.models.data

import kotlinx.serialization.Serializable

/**
 * Data model for cities weather forecasts retrieval.
 *
 * @property cities to provide weather forecasts for.
 */
@Serializable
data class WeatherForecastCitiesResponse(
    val cities: List<WeatherForecastCityResponse>
)

/**
 * Data model for city's information.
 */
@Serializable
data class WeatherForecastCityResponse (
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
)