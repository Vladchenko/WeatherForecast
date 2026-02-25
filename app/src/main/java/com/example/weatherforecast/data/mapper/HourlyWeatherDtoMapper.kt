package com.example.weatherforecast.data.mapper

import com.example.weatherforecast.models.data.database.HourlyWeatherEntity
import com.example.weatherforecast.models.data.database.HourlyWeatherItemEntity
import com.example.weatherforecast.models.data.network.HourlyWeatherDto
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.InternalSerializationApi

/**
 * Mapper class responsible for converting [HourlyWeatherDto] (network model) into [HourlyWeatherEntity] (database model).
 *
 * Transforms the API response into a structured format suitable for local storage.
 * Extracts city metadata and maps each hourly forecast item into [HourlyWeatherItemEntity].
 * Uses persistent collections for efficient immutability and performance in Jetpack Compose.
 *
 * @see toEntity for the main transformation function
 */
@InternalSerializationApi
class HourlyWeatherDtoMapper {

    /**
     * Converts a [HourlyWeatherDto] from the network into a [HourlyWeatherEntity] for database storage.
     *
     * Populates city information such as [cityId], [cityName], and [cityCountry] from the [dto.city] field.
     * Maps each item in [dto.hourlyForecasts] into a [HourlyWeatherItemEntity], extracting:
     * - Temperature, pressure, humidity
     * - Weather condition (main, description, icon)
     * - Wind speed, direction, and gust
     * - Timestamp and formatted date
     *
     * The [lastUpdated] field is set to the current time to support caching logic.
     *
     * @param dto The network DTO containing hourly weather data.
     * @return A fully populated [HourlyWeatherEntity] with immutable list of forecast items.
     */
    fun toEntity(dto: HourlyWeatherDto): HourlyWeatherEntity {
        val city = dto.city
        return HourlyWeatherEntity(
            cityId = city.id,
            cityName = city.name,
            cityCountry = city.country,
            lastUpdated = System.currentTimeMillis(),
            hourlyForecasts = dto.hourlyForecasts.map { item ->
                HourlyWeatherItemEntity(
                    timestamp = item.timestamp,
                    temperature = item.main.temp,
                    feelsLike = item.main.feelsLike,
                    pressure = item.main.pressure,
                    humidity = item.main.humidity,
                    weatherMain = item.weather.firstOrNull()?.main.orEmpty(),
                    weatherDescription = item.weather.firstOrNull()?.description.orEmpty(),
                    weatherIcon = item.weather.firstOrNull()?.icon.orEmpty(),
                    windSpeed = item.wind.speed,
                    windDegrees = item.wind.degrees,
                    windGust = item.wind.gust,
                    dateText = item.dateText
                )
            }.toPersistentList()
        )
    }
}