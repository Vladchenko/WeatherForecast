package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.converter.ForecastDataToDomainModelsConverter
import com.example.weatherforecast.data.converter.HourlyForecastDataToDomainModelsConverter
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.models.domain.HourlyForecastDomainModel
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WeatherForecastRemoteRepository @Inject constructor(
    private val remoteDataSource: WeatherForecastRemoteDataSource,
    private val modelsConverter: ForecastDataToDomainModelsConverter,
    private val hourlyForecastConverter: HourlyForecastDataToDomainModelsConverter,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val temperatureType: TemperatureType
) {
    suspend fun loadForecastForCity(city: String): LoadResult<WeatherForecastDomainModel> =
        withContext(coroutineDispatchers.io) {
            val response = remoteDataSource.loadForecastDataForCity(city)
            val result = modelsConverter.convert(temperatureType, city, response)
            LoadResult.Remote(result, response.body()!!)
        }

    suspend fun loadForecastForLocation(latitude: Double, longitude: Double): LoadResult<WeatherForecastDomainModel> =
        withContext(coroutineDispatchers.io) {
            val response = remoteDataSource.loadForecastForLocation(latitude, longitude)
            val result = modelsConverter.convert(temperatureType, response.body()!!.city, response)
            LoadResult.Remote(result, response.body()!!)
        }

    suspend fun loadHourlyForecastForCity(city: String): LoadResult<HourlyForecastDomainModel> =
        withContext(coroutineDispatchers.io) {
            val response = remoteDataSource.loadHourlyForecastForCity(city)
            val result = hourlyForecastConverter.convert(temperatureType, city, response)
            LoadResult.Remote(result, response.body()!!)
        }

    suspend fun loadHourlyForecastForLocation(latitude: Double, longitude: Double): LoadResult<HourlyForecastDomainModel> =
        withContext(coroutineDispatchers.io) {
            val response = remoteDataSource.loadHourlyForecastForLocation(latitude, longitude)
            val result = hourlyForecastConverter.convert(temperatureType, response.body()!!.city.name, response)
            LoadResult.Remote(result, response.body()!!)
        }
} 