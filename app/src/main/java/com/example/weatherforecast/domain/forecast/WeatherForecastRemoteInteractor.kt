package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.HourlyForecastDomainModel
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel

/**
 * Weather forecast interactor.
 *
 * @property weatherForecastRepository provides domain-layer data.
 */
class WeatherForecastRemoteInteractor(private val weatherForecastRepository: WeatherForecastRepository) {

    suspend fun loadForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<WeatherForecastDomainModel> {
        return weatherForecastRepository.loadAndSaveRemoteForecastForCity(temperatureType, city)
    }

    suspend fun loadForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<WeatherForecastDomainModel> {
        return weatherForecastRepository.loadAndSaveRemoteForecastForLocation(
            temperatureType,
            latitude,
            longitude
        )
    }

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