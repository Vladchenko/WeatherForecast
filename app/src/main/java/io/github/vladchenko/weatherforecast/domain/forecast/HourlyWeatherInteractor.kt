package io.github.vladchenko.weatherforecast.domain.forecast

import io.github.vladchenko.weatherforecast.data.util.TemperatureType
import io.github.vladchenko.weatherforecast.models.domain.HourlyWeatherDomainModel
import io.github.vladchenko.weatherforecast.models.domain.LoadResult

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
    ): LoadResult<HourlyWeatherDomainModel> {
        return weatherForecastRepository.refreshWeatherForLocation(city, temperatureType, latitude, longitude)
    }
}