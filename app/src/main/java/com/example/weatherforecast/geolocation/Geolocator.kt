package com.example.weatherforecast.geolocation

import android.location.Location

/**
 * Defines geo location of a current device
 */
interface Geolocator {
    /**
     * Define city(area) name by [location]
     */
    suspend fun defineCityNameByLocation(location: Location): String

    /**
     * Define [android.location.Location] for [city]
     */
    suspend fun defineLocationByCity(city: String): Location
}