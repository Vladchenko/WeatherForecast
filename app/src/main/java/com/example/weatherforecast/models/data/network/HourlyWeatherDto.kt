package com.example.weatherforecast.models.data.network

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) representing the hourly weather forecast response from the OpenWeather API.
 *
 * This is the root entity deserialized from the hourly forecast API endpoint. It contains:
 * - Basic city information ([HourlyCityDto])
 * - A list of hourly weather data points ([HourlyWeatherItemDto])
 *
 * @property city Metadata about the city (name, ID, country)
 * @property hourlyForecasts List of weather conditions for each hour
 */
@Serializable
@InternalSerializationApi
data class HourlyWeatherDto(
    @SerializedName("city")
    val city: HourlyCityDto,
    @SerializedName("list")
    val hourlyForecasts: List<HourlyWeatherItemDto>
)

/**
 * DTO containing basic city information in the hourly forecast response.
 *
 * @property id Unique identifier assigned by OpenWeather
 * @property name Name of the city
 * @property country Country code (ISO 3166-1 alpha-2)
 */
@Serializable
@InternalSerializationApi
data class HourlyCityDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("country")
    val country: String
)

/**
 * DTO representing a single hourly weather data point.
 *
 * Each instance corresponds to weather conditions at a specific timestamp.
 *
 * @property timestamp Unix timestamp (seconds) for this data point
 * @property main Main atmospheric metrics ([HourlyMainDto])
 * @property weather List of weather conditions (e.g. rain, clouds)
 * @property wind Wind speed and direction ([HourlyWindDto])
 * @property dateText Formatted date-time string (e.g. "2023-10-05 12:00:00")
 */
@Serializable
@InternalSerializationApi
data class HourlyWeatherItemDto(
    @SerializedName("dt")
    val timestamp: Long,
    @SerializedName("main")
    val main: HourlyMainDto,
    @SerializedName("weather")
    val weather: List<WeatherItemDto>,
    @SerializedName("wind")
    val wind: HourlyWindDto,
    @SerializedName("dt_txt")
    val dateText: String
)

/**
 * DTO containing core temperature and pressure measurements for an hourly data point.
 *
 * @property temp Current temperature in Kelvin
 * @property feelsLike Temperature adjusted for human perception
 * @property tempMin Minimum temperature for this period
 * @property tempMax Maximum temperature for this period
 * @property pressure Atmospheric pressure at sea level (hPa)
 * @property humidity Humidity percentage
 */
@Serializable
@InternalSerializationApi
data class HourlyMainDto(
    @SerializedName("temp")
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    @SerializedName("temp_min")
    val tempMin: Double,
    @SerializedName("temp_max")
    val tempMax: Double,
    @SerializedName("pressure")
    val pressure: Long,
    @SerializedName("humidity")
    val humidity: Long
)

/**
 * DTO describing a weather condition at a given time.
 *
 * Multiple entries may exist per time slot (e.g. "rain" and "thunderstorm").
 *
 * @property id OpenWeather condition ID
 * @property main General category (e.g. "Rain", "Clear")
 * @property description Human-readable weather description
 * @property icon Icon code for UI rendering
 */
@Serializable
@InternalSerializationApi
data class WeatherItemDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("main")
    val main: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("icon")
    val icon: String
)

/**
 * DTO representing wind conditions for an hourly data point.
 *
 * @property speed Wind speed in m/s
 * @property degrees Wind direction in degrees (meteorological)
 * @property gust Optional wind gust speed in m/s
 */
@Serializable
@InternalSerializationApi
data class HourlyWindDto(
    @SerializedName("speed")
    val speed: Double,
    @SerializedName("deg")
    val degrees: Long,
    @SerializedName("gust")
    val gust: Double? = null
)