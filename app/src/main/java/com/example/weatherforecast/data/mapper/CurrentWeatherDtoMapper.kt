package com.example.weatherforecast.data.mapper

import com.example.weatherforecast.models.data.database.CurrentWeatherEntity
import com.example.weatherforecast.models.data.network.CurrentWeatherDto
import kotlinx.serialization.InternalSerializationApi

/**
 * Mapper class that converts [CurrentWeatherDto] (network model) into [CurrentWeatherEntity] (database model).
 *
 * Handles the transformation of API response data into a format suitable for local storage.
 * Safely extracts weather condition details using [dto.weather.firstOrNull], and provides
 * a fallback entity in case no weather data is available.
 *
 * @see toEntity for the main mapping function
 * @see fallbackEntity for default values when weather information is missing
 */
@InternalSerializationApi
class CurrentWeatherDtoMapper {

    /**
     * Converts a [CurrentWeatherDto] from the network into a [CurrentWeatherEntity] for database storage.
     *
     * Extracts core weather metrics such as temperature, wind, pressure, and visibility.
     * Uses the first item in [dto.weather] to populate condition details like [weatherMain],
     * [weatherDescription], and [weatherIcon]. If no weather items are present, returns a fallback entity.
     *
     * @param dto The network data transfer object received from OpenWeather API.
     * @return A fully populated [CurrentWeatherEntity], or a fallback with default values if needed.
     *
     * @see fallbackEntity
     */
    fun toEntity(dto: CurrentWeatherDto): CurrentWeatherEntity {
        val weather = dto.weather.firstOrNull() ?: return fallbackEntity(dto)
        return CurrentWeatherEntity(
            city = dto.name,
            latitude = dto.coord.lat,
            longitude = dto.coord.lon,
            temperature = dto.main.temp,
            feelsLike = dto.main.feelsLike,
            tempMin = dto.main.tempMin,
            tempMax = dto.main.tempMax,
            pressure = dto.main.pressure,
            humidity = dto.main.humidity,
            visibility = dto.visibility,
            windSpeed = dto.wind.speed,
            windDegrees = dto.wind.deg,
            cloudCoverage = dto.clouds.all,
            weatherMain = weather.main,
            weatherDescription = weather.description,
            weatherIcon = weather.icon,
            dateTime = dto.dt,
            sunrise = dto.sys.sunrise,
            sunset = dto.sys.sunset,
            timezoneOffset = dto.timezone,
            cityId = dto.id
        )
    }

    /**
     * Creates a fallback [CurrentWeatherEntity] when no valid weather condition data is present.
     *
     * Used to ensure database consistency even when part of the API response is missing.
     * Sets default values for numeric and string fields, while preserving stable data
     * like [city], coordinates, timestamps, and [cityId].
     *
     * @param dto Original DTO used to populate stable fields.
     * @return A [CurrentWeatherEntity] with safe defaults for missing weather data.
     */
    private fun fallbackEntity(dto: CurrentWeatherDto) = CurrentWeatherEntity(
        city = dto.name,
        latitude = dto.coord.lat,
        longitude = dto.coord.lon,
        temperature = 0.0,
        feelsLike = 0.0,
        tempMin = 0.0,
        tempMax = 0.0,
        pressure = 0,
        humidity = 0,
        visibility = 0,
        windSpeed = 0.0,
        windDegrees = 0,
        cloudCoverage = 0,
        weatherMain = "Unknown",
        weatherDescription = "No data",
        weatherIcon = "",
        dateTime = dto.dt,
        sunrise = dto.sys.sunrise,
        sunset = dto.sys.sunset,
        timezoneOffset = dto.timezone,
        cityId = dto.id
    )
}