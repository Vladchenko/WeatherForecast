package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.HourlyForecastDomainModel
import com.example.weatherforecast.models.domain.LoadResult

/**
 * Weather hourly forecast interactor.
 *
 * @property hourlyForecastRepository provides domain-layer data.
 */
class HourlyForecastLocalInteractor(private val hourlyForecastRepository: HourlyForecastRepository) {

    /**
     * Download forecast from local storage.
     *
     * @param city to load forecast for
     * @param remoteError describing what why remote forecast failed
     * @return forecast result
     */
    suspend fun loadForecast(
        city: String,
        temperatureType: TemperatureType,
        remoteError: String
    ): LoadResult<HourlyForecastDomainModel> {
        return hourlyForecastRepository.loadLocalForecast(city, temperatureType, remoteError)
    }
}