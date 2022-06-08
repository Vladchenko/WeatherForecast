package com.example.weatherforecast.domain

import com.example.weatherforecast.data.models.WeatherForecastResponse
import com.example.weatherforecast.data.util.Resource

/**
 * Weather forecast interactor.
 *
 * @property weatherForecastRepository provides data-layer data.
 */
class WeatherForecastInteractor(private val weatherForecastRepository: WeatherForecastRepository) {
    suspend fun execute(city: String): Resource<WeatherForecastResponse> {
        return weatherForecastRepository.getWeatherForecastData(city)
    }
}