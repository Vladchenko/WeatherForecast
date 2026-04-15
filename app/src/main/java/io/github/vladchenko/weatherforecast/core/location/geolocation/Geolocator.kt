package io.github.vladchenko.weatherforecast.core.location.geolocation

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
     * Define [Location] for [city]
     */
    suspend fun defineLocationByCity(city: String): Location
}