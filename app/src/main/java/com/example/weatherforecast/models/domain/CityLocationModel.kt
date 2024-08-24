package com.example.weatherforecast.models.domain

import android.location.Location

/**
 * Represents data for city to provide a weather forecast on.
 *
 * @property city name of city
 * @property location city location
 */
data class CityLocationModel(
    val city: String,
    val location: Location,
)
