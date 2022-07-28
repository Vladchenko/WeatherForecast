package com.example.weatherforecast.data.models.data

import kotlinx.serialization.Serializable

/**
 * Data model for cities weather forecasts retrieval.
 */
@Serializable
data class WeatherForecastCitiesResponse(
    val cities: List<WeatherForecastCityResponse>
)

/**
 * Data model for city's weather forecast.
 */
@Serializable
data class WeatherForecastCityResponse (
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
)