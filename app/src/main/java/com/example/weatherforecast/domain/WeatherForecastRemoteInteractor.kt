package com.example.weatherforecast.domain

import com.example.weatherforecast.data.models.WeatherForecastDomainModel
import com.example.weatherforecast.data.util.TemperatureType

/**
 * Weather forecast interactor.
 *
 * @property weatherForecastRepository provides data-layer data.
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