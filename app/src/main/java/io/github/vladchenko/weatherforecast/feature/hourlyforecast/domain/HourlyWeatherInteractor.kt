package io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain

import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.model.TemperatureType
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain.model.HourlyWeather

/**
 * Weather forecast interactor.
 *
 * @property weatherForecastRepository provides domain-layer data.
 */
class HourlyWeatherInteractor(private val weatherForecastRepository: HourlyWeatherRepository) {

    suspend fun loadHourlyWeatherForLocation(
        city: String,
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyWeather> {
        return weatherForecastRepository.refreshWeatherForLocation(city, temperatureType, latitude, longitude)
    }
}