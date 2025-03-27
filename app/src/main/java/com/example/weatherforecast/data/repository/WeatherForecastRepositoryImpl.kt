package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.domain.forecast.WeatherForecastRepository
import com.example.weatherforecast.models.data.WeatherForecastResponse
import com.example.weatherforecast.models.domain.HourlyForecastDomainModel
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel

class WeatherForecastRepositoryImpl(
    private val remoteRepository: WeatherForecastRemoteRepository,
    private val localRepository: WeatherForecastLocalRepository
) : WeatherForecastRepository {

    override suspend fun loadAndSaveRemoteForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<WeatherForecastDomainModel> {
        return try {
            val result = remoteRepository.loadForecastForCity(city)
            if (result is LoadResult.Remote && result.rawResponse is WeatherForecastResponse) {
                localRepository.saveForecast(result.rawResponse)
            }
            result
        } catch (e: Exception) {
            localRepository.loadForecast(city, e.message ?: "Unknown error")
        }
    }

    override suspend fun loadAndSaveRemoteForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<WeatherForecastDomainModel> {
        return try {
            val result = remoteRepository.loadForecastForLocation(latitude, longitude)
            if (result is LoadResult.Remote && result.rawResponse is WeatherForecastResponse) {
                localRepository.saveForecast(result.rawResponse)
            }
            result
        } catch (e: Exception) {
            localRepository.loadForecast("", e.message ?: "Unknown error")
        }
    }

    override suspend fun loadLocalForecast(city: String, remoteError: String): LoadResult<WeatherForecastDomainModel> {
        return localRepository.loadForecast(city, remoteError)
    }

    override suspend fun loadHourlyForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<HourlyForecastDomainModel> {
        return remoteRepository.loadHourlyForecastForCity(city)
    }

    override suspend fun loadHourlyForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyForecastDomainModel> {
        return remoteRepository.loadHourlyForecastForLocation(latitude, longitude)
    }
}