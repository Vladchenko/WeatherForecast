package io.github.vladchenko.weatherforecast.core.domain.model

import androidx.compose.runtime.Immutable

/**
 * Domain model representing a city with its geographic coordinates.
 *
 * @property name Human-readable city name (e.g., "London", "New York")
 * @property latitude Latitude coordinate in decimal degrees (-90 to 90)
 * @property longitude Longitude coordinate in decimal degrees (-180 to 180)
 */
@Immutable
data class City(val name: String,
                val latitude: Double,
                val longitude: Double)
{
    override fun toString(): String {
        return "City(name='$name', latitude=$latitude, longitude=$longitude)"
    }
}