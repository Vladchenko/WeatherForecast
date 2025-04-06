package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.HourlyForecastDomainModel
import com.example.weatherforecast.models.domain.LoadResult

/**
 * Weather forecast interactor.
 *
 * @property weatherForecastRepository provides domain-layer data.
 */
class HourlyForecastRemoteInteractor(private val weatherForecastRepository: HourlyForecastRepository) {

    suspend fun loadHourlyForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<HourlyForecastDomainModel> {
        return weatherForecastRepository.loadHourlyForecastForCity(temperatureType, city)
    }

    suspend fun loadHourlyForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyForecastDomainModel> {
        return weatherForecastRepository.loadHourlyForecastForLocation(temperatureType, latitude, longitude)
    }
}