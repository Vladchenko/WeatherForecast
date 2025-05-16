package com.example.weatherforecast.models.domain

import kotlinx.collections.immutable.ImmutableList

data class HourlyForecastDomainModel(
    val city: String,
    val hourlyForecasts: ImmutableList<HourlyForecastItemDomainModel>
)

data class HourlyForecastItemDomainModel(
    val timestamp: Long,
    val temperature: String,
    val feelsLike: String,
    val humidity: Int,
    val windSpeed: Double,
    val weatherDescription: String,
    val weatherIcon: String,
    val dateText: String
) 