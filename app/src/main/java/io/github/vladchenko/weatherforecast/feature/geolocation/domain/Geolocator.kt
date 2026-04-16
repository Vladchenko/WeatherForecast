package io.github.vladchenko.weatherforecast.feature.geolocation.domain

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