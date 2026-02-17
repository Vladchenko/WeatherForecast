package com.example.weatherforecast.models.presentation


import androidx.compose.runtime.Immutable

/**
 * Data class representing the weather forecast UI state.
 *
 * Holds all necessary information to display current weather conditions in the UI,
 * including city name, coordinates, date/time, temperature, weather condition,
 * associated icon resource ID, and any server error message.
 *
 * @property city Name of the city for which the forecast is displayed
 * @property coordinate Geographic coordinates (latitude and longitude) of the city
 * @property dateTime Formatted date and time of the forecast
 * @property weatherIconId Id of the weather condition icon (e.g., sunny, rainy)
 * @property temperature Current temperature as a formatted string
 * @property weatherType Description of the weather condition (e.g., "clear sky", "light rain")
 * @property temperatureType Unit of temperature measurement (e.g., "°C", "°F")
 * @property serverError Error message from the API; empty if no error occurred
 */
data class CurrentWeatherUi(
    val city: String,
    val coordinate: Coordinate,
    val dateTime: String,
    val weatherIconId: Int,
    val temperature: String,
    val weatherType: String,
    val temperatureType: String,
    val serverError: String
) {
    override fun toString(): String {
        return "WeatherForecastUi(city='$city', coordinate=$coordinate, date='$dateTime', temperature='$temperature', weatherType='$weatherType', temperatureType='$temperatureType', serverError='$serverError')"
    }
}

/**
 * Immutable data class representing geographic coordinates.
 *
 * Used to store latitude and longitude values for a location.
 * Marked with [@Immutable] for use in Jetpack Compose state.
 *
 * @property latitude Latitude in degrees (range: -90 to +90)
 * @property longitude Longitude in degrees (range: -180 to +180)
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