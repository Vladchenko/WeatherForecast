package io.github.vladchenko.weatherforecast.core.domain.model

/**
 * Represents data for city to provide a weather forecast on.
 *
 * @property city name of city
 * @property coordinate city coordinate
 */
data class CityLocationModel(
    val city: String,
    val coordinate: Coordinate,
)

/**
 * Coordinate of a location to provide a weather forecast for
 *
 * @property latitude 1st ordinate to locate city
 * @property longitude 2nd ordinate to locate city
 */
data class Coordinate(
    val latitude: Double,
    val longitude: Double,
) {
    override fun toString(): String {
        return "Coordinate(latitude=$latitude, longitude=$longitude)"
    }
}