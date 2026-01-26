package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.models.domain.WeatherForecast

/**
 * Weather forecast interactor.
 *
 * @property weatherForecastRepository provides domain-layer data.
 */
class WeatherForecastLocalInteractor(private val weatherForecastRepository: WeatherForecastRepository) {

    /**
     * Download forecast from local storage.
     *
     * @param city to load forecast for
     * @param remoteError describing what why remote forecast failed
     * @return forecast result
     */
    suspend fun loadForecast(city: String, remoteError: String): LoadResult<WeatherForecast> {
        return weatherForecastRepository.loadLocalForecast(city, remoteError)
    }
}