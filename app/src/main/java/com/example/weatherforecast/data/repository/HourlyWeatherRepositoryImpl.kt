package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.converter.HourlyWeatherModelConverter
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherLocalDataSource
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.forecast.HourlyWeatherRepository
import com.example.weatherforecast.models.data.HourlyWeatherResponse
import com.example.weatherforecast.models.domain.HourlyWeatherDomainModel
import com.example.weatherforecast.models.domain.LoadResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi

/**
 * WeatherForecastRepository implementation. Provides data for domain layer.
 *
 * @property hourlyWeatherRemoteDataSource source of remote data for domain layer
 * @property hourlyWeatherLocalDataSource source of local data for domain layer
 * @property modelsConverter converts server response to domain entity
 * @property coroutineDispatchers dispatchers for coroutines
 */
class HourlyWeatherRepositoryImpl(
    private val hourlyWeatherRemoteDataSource: HourlyWeatherRemoteDataSource,
    private val hourlyWeatherLocalDataSource: HourlyWeatherLocalDataSource,
    private val modelsConverter: HourlyWeatherModelConverter,
    private val coroutineDispatchers: CoroutineDispatchers,
) : HourlyWeatherRepository {

    @InternalSerializationApi
    override suspend fun refreshWeatherForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<HourlyWeatherDomainModel> =
        withContext(coroutineDispatchers.io) {
            try {
                return@withContext loadHourlyWeatherAndSave(city, temperatureType)
            } catch (e: Exception) {
                return@withContext loadLocalHourlyWeatherOrError(city, temperatureType, e)
            }
        }

    @InternalSerializationApi
    private suspend fun loadHourlyWeatherAndSave(
        city: String,
        temperatureType: TemperatureType
    ): LoadResult<HourlyWeatherDomainModel> {
        val response = hourlyWeatherRemoteDataSource.loadHourlyWeatherForCity(city)
        val body = response.body() ?: return LoadResult.Error(
            Exception("hourly weather response for city has empty body")
        )
        val result = modelsConverter.convert(
            temperatureType,
            city,
            response
        )
        saveWeather(body)
        return LoadResult.Remote(result)
    }

    @InternalSerializationApi
    private suspend fun loadLocalHourlyWeatherOrError(
        city: String,
        temperatureType: TemperatureType,
        e: Exception
    ): LoadResult<HourlyWeatherDomainModel> {
        try {
            val response = hourlyWeatherLocalDataSource.getHourlyWeather(city)
            val result = modelsConverter.convert(
                temperatureType,
                city,
                response
            )
            return LoadResult.Local(result, e.message.toString())
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    @InternalSerializationApi
    override suspend fun refreshWeatherForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyWeatherDomainModel> =
        withContext(coroutineDispatchers.io) {
            val result: HourlyWeatherDomainModel
            val response =
                hourlyWeatherRemoteDataSource.loadHourlyWeatherForLocation(latitude, longitude)
            val body = response.body() ?: return@withContext LoadResult.Error(
                Exception("hourly weather response for location has empty body")
            )
            result = modelsConverter.convert(
                temperatureType,
                body.city.name,
                response
            )
            saveWeather(body)
            return@withContext LoadResult.Remote(result)
        }

    @InternalSerializationApi
    override suspend fun loadCachedWeather(
        city: String,
        temperatureType: TemperatureType,
        remoteError: String
    ) =
        withContext(coroutineDispatchers.io) {
            val response: HourlyWeatherDomainModel
            val datasourceResponse = hourlyWeatherLocalDataSource.getHourlyWeather(city)
            response = modelsConverter.convert(
                temperatureType,
                city,
                datasourceResponse
            )
            return@withContext LoadResult.Local(response, remoteError)
        }

    @InternalSerializationApi
    private suspend fun saveWeather(response: HourlyWeatherResponse) =
        withContext(coroutineDispatchers.io) {
            launch {
                hourlyWeatherLocalDataSource.saveHourlyWeather(response)
            }
        }
}