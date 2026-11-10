package io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain

import io.github.vladchenko.weatherforecast.core.domain.model.HourlyWeather
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.domain.model.TemperatureUnit

/**
 * Weather forecast interactor.
 *
 * Provides methods for fetching hourly weather forecast data
 * through the domain layer.
 *
 * @property weatherForecastRepository repository for fetching domain layer data
 */
class HourlyWeatherInteractor(private val weatherForecastRepository: HourlyWeatherRepository) {

    /**
     * Loads hourly weather data for a specific location.
     *
     * @param city name of the city
     * @param temperatureUnit temperature unit for the response
     * @param latitude geographic latitude of the location
     * @param longitude geographic longitude of the location
     * @return [LoadResult] containing either remote or cached local weather data, or an error
     */
    suspend fun loadHourlyWeatherForLocation(
        city: String,
        temperatureUnit: TemperatureUnit,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyWeather> {
        return weatherForecastRepository.refreshWeatherForLocation(
            city,
            latitude,
            longitude,
            temperatureUnit,
        )
    }
}