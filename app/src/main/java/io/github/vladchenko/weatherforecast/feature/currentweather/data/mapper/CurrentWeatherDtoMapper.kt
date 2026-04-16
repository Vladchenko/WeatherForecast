package io.github.vladchenko.weatherforecast.feature.currentweather.data.mapper

import io.github.vladchenko.weatherforecast.feature.currentweather.data.model.CurrentWeatherDto
import io.github.vladchenko.weatherforecast.feature.currentweather.data.model.CurrentWeatherEntity
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
     * Converts [dto] into a [CurrentWeatherEntity] for database storage.
     *
     * Uses the provided [city] instead of [dto.name] to avoid ambiguity between cities with the same name
     * but in different regions (e.g., "London, UK" vs "London, CA"), as the API returns only the bare city name.
     *
     * Extracts core weather data from [dto], using the first weather condition entry. If missing, returns a fallback.
     *
     * @param dto The network DTO from OpenWeather API.
     * @param city The resolved full city name (with state/country context) from user input or geocoding.
     * @return A fully populated entity, or fallback if weather data is missing.
     */
    fun toEntity(dto: CurrentWeatherDto, city: String): CurrentWeatherEntity {
        val weather = dto.weather.firstOrNull() ?: return fallbackEntity(dto)
        return CurrentWeatherEntity(
            city = city,
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