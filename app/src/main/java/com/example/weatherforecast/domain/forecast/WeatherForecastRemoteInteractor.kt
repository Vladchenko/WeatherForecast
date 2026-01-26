package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.models.domain.WeatherForecast

/**
 * Weather forecast interactor.
 *
 * @property weatherForecastRepository provides domain-layer data.
 */
class WeatherForecastRemoteInteractor(private val weatherForecastRepository: WeatherForecastRepository) {

    suspend fun loadForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<WeatherForecast> {
        return weatherForecastRepository.loadAndSaveRemoteForecastForCity(temperatureType, city)
    }

    suspend fun loadForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<WeatherForecast> {
        return weatherForecastRepository.loadAndSaveRemoteForecastForLocation(
            temperatureType,
            latitude,
            longitude
        )
    }
}