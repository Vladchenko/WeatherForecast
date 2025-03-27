package com.example.weatherforecast.models.domain

data class HourlyForecastDomainModel(
    val city: String,
    val hourlyForecasts: List<HourlyForecastItemDomainModel>
)

data class HourlyForecastItemDomainModel(
    val timestamp: Long,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherDescription: String,
    val weatherIcon: String,
    val dateText: String
) 