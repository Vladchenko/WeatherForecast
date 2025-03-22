package com.example.weatherforecast.models.domain

import androidx.compose.runtime.Immutable

/**
 * Weather forecast response model
 *
 * @property city a name of city to provide a weather forecast on
 * @property coordinate of longitude and latitude
 * @property dateTime on which a forecast is provided
 * @property temperature for the city
 * @property weatherType like rain, snow, etc
 * @property temperatureType like Celsius, Fahrenheit
 * @property serverError if there is an error from remote server
 */
data class WeatherForecastDomainModel(
    val city: String,
    val coordinate: Coordinate,
    val dateTime: String,
    val temperature: String,
    val weatherType: String,
    val temperatureType: String,
    val serverError: String
) {
    override fun toString(): String {
        return "WeatherForecastDomainModel(city='$city', coordinate=$coordinate, date='$dateTime', temperature='$temperature', weatherType='$weatherType', temperatureType='$temperatureType', serverError='$serverError')"
    }
}

/**
 * Coordinate of a location to provide a weather forecast for
 *
 * @property latitude 1st ordinate to locate city
 * @property longitude 2nd ordinate to locate city
 */
@Immutable
data class Coordinate(
    val latitude: Double,
    val longitude: Double,
) {
    override fun toString(): String {
        return "Coordinate(latitude=$latitude, longitude=$longitude)"
    }
}