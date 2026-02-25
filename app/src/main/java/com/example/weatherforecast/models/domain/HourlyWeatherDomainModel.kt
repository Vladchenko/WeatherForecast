package com.example.weatherforecast.models.domain

import kotlinx.collections.immutable.ImmutableList

/**
 * Domain model representing hourly weather forecast data for a specific city.
 *
 * @property city Name of the city for which the forecast is provided
 * @property hourlyForecasts List of hourly forecast entries
 */
data class HourlyWeatherDomainModel(
    val city: String,
    val hourlyForecasts: ImmutableList<HourlyItemDomainModel>
)

/**
 * Domain model representing a single hourly weather forecast entry.
 *
 * @property timestamp Unix timestamp (in seconds) for the forecast point
 * @property temperature Current temperature as formatted string (e.g. "20°C")
 * @property feelsLike Feels-like temperature as formatted string (e.g. "18°C")
 * @property humidity Relative humidity percentage
 * @property windSpeed Wind speed in meters per second
 * @property weatherDescription Human-readable weather condition (e.g. "Light rain")
 * @property weatherIcon Weather condition icon code (used to fetch corresponding image)
 * @property dateText Formatted time label (e.g. "14:00" or "Now")
 */
data class HourlyItemDomainModel(
    val timestamp: Long,
    val temperature: String,
    val feelsLike: String,
    val humidity: Int,
    val windSpeed: Double,
    val weatherDescription: String,
    val weatherIcon: String,
    val dateText: String
)