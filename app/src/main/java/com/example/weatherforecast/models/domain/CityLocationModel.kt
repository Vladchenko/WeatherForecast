package com.example.weatherforecast.models.domain

import android.location.Location

/**
 *
 */
data class CityLocationModel(
    val city: String,
    val location: Location,
)
