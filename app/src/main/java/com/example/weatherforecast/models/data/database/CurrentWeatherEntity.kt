package com.example.weatherforecast.models.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * Database entity representing the current weather forecast for a city.
 *
 * This class is used by Room to persist weather data locally. Each instance corresponds
 * to a single city's current weather and is uniquely identified by [city] name.
 *
 * @property city Name of the city (used as primary key)
 * @property latitude Latitude of the city in decimal degrees
 * @property longitude Longitude of the city in decimal degrees
 * @property temperature Current temperature in Kelvin
 * @property feelsLike Temperature adjusted for human perception (wind, humidity)
 * @property tempMin Minimum temperature for the day
 * @property tempMax Maximum temperature for the day
 * @property pressure Atmospheric pressure at sea level (hPa)
 * @property humidity Humidity percentage
 * @property visibility Visibility in meters
 * @property windSpeed Wind speed in m/s
 * @property windDegrees Wind direction in degrees (meteorological)
 * @property cloudCoverage Cloudiness percentage (0–100%)
 * @property weatherMain General weather condition (e.g., "Rain", "Clear")
 * @property weatherDescription Detailed description of the weather
 * @property weatherIcon Icon code for displaying weather conditions
 * @property dateTime Time of data calculation, Unix timestamp (seconds)
 * @property sunrise Sunrise time, Unix timestamp (seconds)
 * @property sunset Sunset time, Unix timestamp (seconds)
 * @property timezoneOffset Shift in seconds from UTC
 * @property cityId Unique identifier assigned by OpenWeather API
 *
 * @see Entity
 * @see PrimaryKey
 * @see TypeConverters
 */
@Entity(tableName = "citiesForecasts")
data class CurrentWeatherEntity(
    @PrimaryKey
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val pressure: Long,
    val humidity: Long,
    val visibility: Long,
    val windSpeed: Double,
    val windDegrees: Long,
    val cloudCoverage: Long,
    val weatherMain: String,
    val weatherDescription: String,
    val weatherIcon: String,
    val dateTime: Long,
    val sunrise: Long,
    val sunset: Long,
    val timezoneOffset: Long,
    val cityId: Long
)