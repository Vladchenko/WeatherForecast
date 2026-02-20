package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.HourlyWeatherDomainModel
import com.example.weatherforecast.models.domain.LoadResult

/**
 * Weather forecast interactor.
 *
 * @property weatherForecastRepository provides domain-layer data.
 */
class HourlyWeatherInteractor(private val weatherForecastRepository: HourlyWeatherRepository) {

    suspend fun loadHourlyForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<HourlyWeatherDomainModel> {
        return weatherForecastRepository.refreshWeatherForCity(temperatureType, city)
    }

    suspend fun loadHourlyWeatherForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyWeatherDomainModel> {
        return weatherForecastRepository.refreshWeatherForLocation(temperatureType, latitude, longitude)
    }
}