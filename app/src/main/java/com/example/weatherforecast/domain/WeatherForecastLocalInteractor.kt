package com.example.weatherforecast.domain

import com.example.weatherforecast.data.models.WeatherForecastDomainModel

/**
 * Weather forecast interactor.
 *
 * @property weatherForecastRepository provides data-layer data.
 */
class WeatherForecastLocalInteractor(private val weatherForecastRepository: WeatherForecastRepository) {

    suspend fun loadForecast(city: String): WeatherForecastDomainModel {
        return weatherForecastRepository.loadLocalForecast(city)
    }

    suspend fun saveForecast(domain: WeatherForecastDomainModel) {
        weatherForecastRepository.saveForecast(domain)
    }
}