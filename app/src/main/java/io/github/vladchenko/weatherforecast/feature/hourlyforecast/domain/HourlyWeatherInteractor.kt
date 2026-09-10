package io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain

import io.github.vladchenko.weatherforecast.core.domain.model.HourlyWeather
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.domain.model.TemperatureUnit

/**
 * Weather forecast interactor.
 *
 * @property weatherForecastRepository provides domain-layer data.
 */
class HourlyWeatherInteractor(private val weatherForecastRepository: HourlyWeatherRepository) {

    suspend fun loadHourlyWeatherForLocation(
        city: String,
        temperatureUnit: TemperatureUnit,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyWeather> {
        return weatherForecastRepository.refreshWeatherForLocation(city, temperatureUnit, latitude, longitude)
    }
}