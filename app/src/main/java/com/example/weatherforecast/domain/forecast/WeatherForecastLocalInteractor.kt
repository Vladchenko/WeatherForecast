package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.models.domain.WeatherForecastDomainModel

/**
 * Weather forecast interactor.
 *
 * @property weatherForecastRepository provides domain-layer data.
 */
class WeatherForecastLocalInteractor(private val weatherForecastRepository: WeatherForecastRepository) {

    suspend fun loadForecast(city: String): WeatherForecastDomainModel {
        return weatherForecastRepository.loadLocalForecast(city)
    }

    suspend fun saveForecast(domain: WeatherForecastDomainModel) {
        weatherForecastRepository.saveForecast(domain)
    }
}