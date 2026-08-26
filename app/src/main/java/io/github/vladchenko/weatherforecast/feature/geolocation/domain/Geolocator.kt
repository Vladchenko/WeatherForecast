package io.github.vladchenko.weatherforecast.feature.geolocation.domain

import io.github.vladchenko.weatherforecast.core.domain.model.Coordinate

/**
 * Defines geo location of a current device
 */
interface Geolocator {
    /**
     * Define city(area) name by [coordinate]
     */
    suspend fun defineCityNameByLocation(coordinate: Coordinate): String

    /**
     * Define [Coordinate] for [city]
     */
    suspend fun defineLocationByCity(city: String): Coordinate
}