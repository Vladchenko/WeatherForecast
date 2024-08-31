package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.converter.ForecastDataToDomainModelsConverter
import com.example.weatherforecast.data.repository.datasource.WeatherForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.forecast.WeatherForecastRepository
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import kotlinx.coroutines.withContext

/**
 * WeatherForecastRepository implementation. Provides data for domain layer.
 *
 * @param weatherForecastRemoteDataSource source of remote data for domain layer
 * @param weatherForecastLocalDataSource source of local data for domain layer
 * @param modelsConverter converts server response to domain entity
 * @param coroutineDispatchers dispatchers for coroutines
 */
class WeatherForecastRepositoryImpl(
    private val weatherForecastRemoteDataSource: WeatherForecastRemoteDataSource,
    private val weatherForecastLocalDataSource: WeatherForecastLocalDataSource,
    private val modelsConverter: ForecastDataToDomainModelsConverter,
    private val coroutineDispatchers: CoroutineDispatchers
) : WeatherForecastRepository {

    override suspend fun loadRemoteForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): Result<WeatherForecastDomainModel> =
        withContext(coroutineDispatchers.io) {
            val response: WeatherForecastDomainModel
            val datasourceResponse = weatherForecastRemoteDataSource.loadForecastDataForCity(city)
                response = modelsConverter.convert(
                    temperatureType,
                    city,
                    datasourceResponse
                )
            return@withContext Result.success(response)
        }

    override suspend fun loadRemoteForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ) = withContext(coroutineDispatchers.io) {
        val model = weatherForecastRemoteDataSource.loadForecastForLocation(latitude, longitude)
        Result.success(modelsConverter.convert(temperatureType, model.body()!!.name, model))
    }

    override suspend fun loadLocalForecast(city: String) =
        withContext(coroutineDispatchers.io) {
            weatherForecastLocalDataSource.loadForecastData(city)
        }

    override suspend fun saveForecast(model: WeatherForecastDomainModel) =
        withContext(coroutineDispatchers.io) {
            weatherForecastLocalDataSource.saveForecastData(model)
        }
}