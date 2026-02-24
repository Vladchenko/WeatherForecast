package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.CurrentWeather
import com.example.weatherforecast.models.domain.LoadResult

/**
 * Weather forecast interactor.
 *
 * @property currentWeatherRepository provides domain-layer data.
 */
class CurrentWeatherInteractor(private val currentWeatherRepository: CurrentWeatherRepository) {

    suspend fun loadWeatherForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<CurrentWeather> {
        return currentWeatherRepository.refreshWeatherForCity(temperatureType, city)
    }

    suspend fun loadWeatherForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<CurrentWeather> {
        return currentWeatherRepository.refreshWeatherForLocation(
            temperatureType,
            latitude,
            longitude
        )
    }
}