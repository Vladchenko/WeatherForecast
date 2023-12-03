package com.example.weatherforecast.geolocation

import android.location.Location

/**
 * Defines location of a current device
 */
interface Geolocator {
    /**
     * Get city(area) name (i.e. city) by [location]
     */
    suspend fun defineCityNameByLocation(location: Location): String

    /**
     * Define [android.location.Location] for [city]
     */
    suspend fun defineLocationByCity(city: String): Location
}