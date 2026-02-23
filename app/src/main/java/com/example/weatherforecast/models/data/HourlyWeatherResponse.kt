package com.example.weatherforecast.models.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.weatherforecast.data.database.HourlyWeatherTypeConverters
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.InternalSerializationApi

/**
 * Room entity representing the hourly weather forecast data for a specific city.
 *
 * This class is used for local persistence via Room database. It holds a list of [HourlyWeatherItem]
 * and is uniquely identified by the [city] field, which serves as the primary key.
 *
 * The data is fetched from the OpenWeatherMap API and converted using Retrofit and Gson annotations.
 *
 * @property city Unique identifier and metadata for the city (acts as primary key)
 * @property hourlyForecasts List of hourly forecast entries
 */
@InternalSerializationApi
@Entity(tableName = "hourlyForecasts")
@TypeConverters(HourlyWeatherTypeConverters::class)
data class HourlyWeatherResponse(
    @PrimaryKey
    @ColumnInfo(name = "city_id")
    val cityId: Long,

    @Embedded(prefix = "city_")
    val city: City,

    @SerializedName("list")
    val hourlyForecasts: List<HourlyWeatherItem>
)

/**
 * Represents a single hourly weather forecast entry.
 *
 * Contains timestamped weather data including temperature, weather conditions,
 * wind speed, and formatted date/time string.
 *
 * @property timestamp Unix timestamp (in seconds) for this forecast point
 * @property main Temperature, pressure, humidity, and other atmospheric data
 * @property weather List of weather conditions (e.g., clear sky, rain)
 * @property wind Wind speed and direction
 * @property dateText Formatted date and time string (e.g., "2023-10-05 12:00:00")
 */
@InternalSerializationApi
data class HourlyWeatherItem(
    @SerializedName("dt")
    val timestamp: Long,
    @SerializedName("main")
    val main: Main,
    @SerializedName("weather")
    val weather: List<Weather>,
    @SerializedName("wind")
    val wind: Wind,
    @SerializedName("dt_txt")
    val dateText: String
)

/**
 * Basic information about the city associated with the forecast.
 *
 * Used as part of [HourlyWeatherResponse] and serves as the primary key in the database.
 *
 * @property name Name of the city
 * @property country Country code (e.g., "RU", "US")
 */
data class City(
    @SerializedName("name")
    val name: String,
    @SerializedName("country")
    val country: String
) 