package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel

/**
 * Weather forecast interactor.
 *
 * @property weatherForecastRepository provides domain-layer data.
 */
class WeatherForecastRemoteInteractor(private val weatherForecastRepository: WeatherForecastRepository) {

    suspend fun loadForecastForCity(temperatureType: TemperatureType, city: String): WeatherForecastDomainModel {
        return weatherForecastRepository.loadRemoteForecastForCity(temperatureType, city)
    }

    suspend fun loadRemoteForecastForLocation(temperatureType: TemperatureType, latitude: Double, longitude: Double)
        : WeatherForecastDomainModel {
        return weatherForecastRepository.loadRemoteForecastForLocation(temperatureType, latitude, longitude)
    }
}