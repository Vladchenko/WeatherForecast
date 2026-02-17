package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.CurrentWeather
import com.example.weatherforecast.models.domain.LoadResult

/**
 * Weather forecast interactor.
 *
 * @property currentWeatherRepository provides domain-layer data.
 */
class CurrentWeatherRemoteInteractor(private val currentWeatherRepository: CurrentWeatherRepository) {

    suspend fun loadForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<CurrentWeather> {
        return currentWeatherRepository.loadAndSaveRemoteWeatherForCity(temperatureType, city)
    }

    suspend fun loadForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<CurrentWeather> {
        return currentWeatherRepository.loadAndSaveRemoteWeatherForLocation(
            temperatureType,
            latitude,
            longitude
        )
    }
}