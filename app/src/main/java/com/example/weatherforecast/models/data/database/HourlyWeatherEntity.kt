package com.example.weatherforecast.models.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.weatherforecast.data.database.HourlyWeatherTypeConverters
import kotlinx.collections.immutable.ImmutableList

/**
 * Database entity representing the hourly weather forecast for a city.
 *
 * This class is used by Room to persist time-series weather data locally. Each instance
 * corresponds to one city and contains a list of hourly forecast items.
 *
 * The table name is `"hourlyForecasts"`, with [cityId] as the primary key. Uses [HourlyWeatherTypeConverters]
 * to serialize complex types like [ImmutableList] through custom type conversion.
 *
 * @property cityId Unique identifier assigned by OpenWeather API (used as primary key)
 * @property cityName Name of the city
 * @property cityCountry Country code (ISO 3166-1 alpha-2)
 * @property latitude Optional latitude of the city (may be populated later)
 * @property longitude Optional longitude of the city (may be populated later)
 * @property timezone Optional IANA timezone identifier (e.g., "Europe/Moscow")
 * @property timezoneOffset Shift in seconds from UTC
 * @property lastUpdated Timestamp (ms) when this data was last fetched or stored
 * @property hourlyForecasts Immutable list of [HourlyWeatherItemEntity] objects, each representing one hour
 *
 * @see Entity
 * @see PrimaryKey
 * @see TypeConverters
 * @see HourlyWeatherTypeConverters
 */
@Entity(tableName = "hourlyForecasts")
@TypeConverters(HourlyWeatherTypeConverters::class)
data class HourlyWeatherEntity(
    @PrimaryKey
    val cityId: Long,
    val cityName: String,
    val cityCountry: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String? = null,
    val timezoneOffset: Long = 0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val hourlyForecasts: ImmutableList<HourlyWeatherItemEntity>
)

/**
 * Nested entity representing a single hourly weather data point.
 *
 * Contains atmospheric conditions at a specific timestamp. This class is not a standalone
 * database table but is serialized as part of [HourlyWeatherEntity] using type converters.
 *
 * @property timestamp Unix timestamp in seconds for this forecast
 * @property temperature Temperature in Kelvin
 * @property feelsLike Perceived temperature in Kelvin
 * @property pressure Atmospheric pressure at sea level (hPa)
 * @property humidity Humidity percentage
 * @property weatherMain General weather condition (e.g., "Rain", "Clear")
 * @property weatherDescription Detailed description of current weather
 * @property weatherIcon Icon code used to display appropriate weather icon
 * @property windSpeed Wind speed in meters per second
 * @property windDegrees Wind direction in degrees (meteorological)
 * @property windGust Optional wind gust speed in meters per second
 * @property dateText Formatted date-time string (e.g., "2023-10-05 12:00:00")
 *
 * @see HourlyWeatherTypeConverters for serialization logic
 */
@TypeConverters(HourlyWeatherTypeConverters::class)
data class HourlyWeatherItemEntity(
    val timestamp: Long,
    val temperature: Double,
    val feelsLike: Double,
    val pressure: Long,
    val humidity: Long,
    val weatherMain: String,
    val weatherDescription: String,
    val weatherIcon: String,
    val windSpeed: Double,
    val windDegrees: Long,
    val windGust: Double? = null,
    val dateText: String
)