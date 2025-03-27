package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.converter.ForecastDataToDomainModelsConverter
import com.example.weatherforecast.data.repository.datasource.WeatherForecastLocalDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.models.data.WeatherForecastResponse
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class WeatherForecastLocalRepository @Inject constructor(
    private val localDataSource: WeatherForecastLocalDataSource,
    private val modelsConverter: ForecastDataToDomainModelsConverter,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val temperatureType: TemperatureType
) {
    suspend fun loadForecast(city: String, remoteError: String): LoadResult<WeatherForecastDomainModel> =
        withContext(coroutineDispatchers.io) {
            val response = Response.success(localDataSource.loadForecastData(city))
            val result = modelsConverter.convert(temperatureType, city, response)
            LoadResult.Local(result, remoteError)
        }

    suspend fun saveForecast(response: WeatherForecastResponse) {
        withContext(coroutineDispatchers.io) {
            localDataSource.saveForecastData(response)
        }
    }
} 