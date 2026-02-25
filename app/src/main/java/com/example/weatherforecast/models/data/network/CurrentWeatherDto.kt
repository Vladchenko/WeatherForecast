package com.example.weatherforecast.models.data.network

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) representing the current weather response from the OpenWeather API.
 *
 * This is the root entity deserialized from the API JSON response. It contains comprehensive
 * weather data for a specific location, including atmospheric conditions, wind, clouds,
 * temperature, and system information.
 *
 * @property coord Geographic coordinates of the city ([CoordinateDto])
 * @property weather List of weather condition details (e.g. clear, rain)
 * @property base Internal parameter used by OpenWeather
 * @property main Contains essential weather metrics like temperature and pressure ([MainDto])
 * @property visibility Visibility in meters
 * @property wind Wind speed and direction ([WindDto])
 * @property clouds Cloudiness percentage ([CloudsDto])
 * @property dt Time of data calculation, Unix timestamp (seconds)
 * @property sys Additional system information like sunrise/sunset times ([SystemDto])
 * @property timezone Shift in seconds from UTC
 * @property id City ID assigned by OpenWeather
 * @property name Name of the city
 * @property cod Internal status code returned by OpenWeather
 */
@Serializable
@InternalSerializationApi
data class CurrentWeatherDto(
    val coord: CoordinateDto,
    val weather: List<WeatherDto>,
    val base: String,
    val main: MainDto,
    val visibility: Long,
    val wind: WindDto,
    val clouds: CloudsDto,
    val dt: Long,
    val sys: SystemDto,
    val timezone: Long,
    val id: Long,
    val name: String,
    val cod: Long,
)

/**
 * DTO representing geographic coordinates.
 *
 * @property lat Latitude in decimal degrees
 * @property lon Longitude in decimal degrees
 */
@Serializable
@InternalSerializationApi
data class CoordinateDto(
    val lat: Double,
    val lon: Double
)

/**
 * DTO describing a single weather condition.
 *
 * Multiple conditions can be present (e.g. "rain" and "thunderstorm").
 *
 * @property id OpenWeather condition ID
 * @property main Group of weather parameters (e.g. "Rain", "Snow")
 * @property description Human-readable description of the weather
 * @property icon Icon code to display appropriate weather icon
 */
@Serializable
@InternalSerializationApi
data class WeatherDto(
    val id: Long,
    val main: String,
    val description: String,
    val icon: String
)

/**
 * DTO containing main weather measurements.
 *
 * @property temp Current temperature in Kelvin
 * @property feelsLike Temperature adjusted for human perception (wind, humidity)
 * @property tempMin Minimum temperature for the day
 * @property tempMax Maximum temperature for the day
 * @property pressure Atmospheric pressure at sea level, hPa
 * @property humidity Humidity percentage
 */
@Serializable
@InternalSerializationApi
data class MainDto(
    val temp: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val pressure: Long,
    val humidity: Long
)

/**
 * DTO representing wind conditions.
 *
 * @property speed Wind speed in m/s
 * @property deg Wind direction in degrees (meteorological)
 * @property gust Optional wind gust speed in m/s
 */
@Serializable
@InternalSerializationApi
data class WindDto(
    val speed: Double,
    val deg: Long,
    val gust: Double? = null
)

/**
 * DTO for cloud coverage data.
 *
 * @property all Cloudiness percentage (0-100%)
 */
@Serializable
@InternalSerializationApi
data class CloudsDto(
    val all: Long
)

/**
 * DTO containing system-level data such as country and sunrise/sunset times.
 *
 * @property type Internal OpenWeather parameter
 * @property id Internal OpenWeather parameter
 * @property country Country code (ISO 3166-1 alpha-2)
 * @property sunrise Sunrise time, Unix timestamp (seconds)
 * @property sunset Sunset time, Unix timestamp (seconds)
 */
@Serializable
@InternalSerializationApi
data class SystemDto(
    val type: Long,
    val id: Long,
    val country: String,
    val sunrise: Long,
    val sunset: Long
)