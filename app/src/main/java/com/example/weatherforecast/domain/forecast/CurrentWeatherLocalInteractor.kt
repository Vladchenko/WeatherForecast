package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.CurrentWeather
import com.example.weatherforecast.models.domain.LoadResult

/**
 * Weather forecast interactor.
 *
 * @property currentWeatherRepository provides domain-layer data.
 */
class CurrentWeatherLocalInteractor(private val currentWeatherRepository: CurrentWeatherRepository) {

    /**
     * Download forecast from local storage.
     *
     * @param city to load forecast for
     * @param remoteError describing what why remote forecast failed
     * @return forecast result
     */
    suspend fun loadForecast(city: String, temperatureType: TemperatureType, remoteError: String): LoadResult<CurrentWeather> {
        return currentWeatherRepository.loadCachedWeather(city, temperatureType, remoteError)
    }
}