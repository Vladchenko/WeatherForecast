package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.converter.CurrentForecastModelConverter
import com.example.weatherforecast.data.repository.datasource.WeatherForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.forecast.WeatherForecastRepository
import com.example.weatherforecast.models.data.WeatherForecastResponse
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.models.domain.WeatherForecast
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

/**
 * WeatherForecastRepository implementation. Provides data for domain layer.
 *
 * @property weatherForecastRemoteDataSource source of remote data for domain layer
 * @property weatherForecastLocalDataSource source of local data for domain layer
 * @property modelsConverter converts server response to domain entity
 * @property coroutineDispatchers dispatchers for coroutines
 * @property temperatureType like celsius, fahrenheit
 */
class WeatherForecastRepositoryImpl(
    private val weatherForecastRemoteDataSource: WeatherForecastRemoteDataSource,
    private val weatherForecastLocalDataSource: WeatherForecastLocalDataSource,
    private val modelsConverter: CurrentForecastModelConverter,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val temperatureType: TemperatureType
) : WeatherForecastRepository {

    @InternalSerializationApi
    override suspend fun loadAndSaveRemoteForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<WeatherForecast> =
        withContext(coroutineDispatchers.io) {
            val response = weatherForecastRemoteDataSource.loadForecastForCity(city)
            val result = modelsConverter.convert(
                temperatureType,
                city,
                response
            )
            saveForecast(response.body()!!)     //NPE handled in WeatherForecastViewModel's exceptionHandler
            return@withContext LoadResult.Remote(result)
        }

    @InternalSerializationApi
    override suspend fun loadAndSaveRemoteForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<WeatherForecast> =
        withContext(coroutineDispatchers.io) {
            val result: WeatherForecast
            val response =
                weatherForecastRemoteDataSource.loadForecastForLocation(latitude, longitude)
            result = modelsConverter.convert(
                temperatureType,
                response.body()!!.city,
                response
            )
            saveForecast(response.body()!!)     //NPE handled in WeatherForecastViewModel's exceptionHandler
            return@withContext LoadResult.Remote(result)
        }

    @InternalSerializationApi
    override suspend fun loadLocalForecast(city: String, remoteError: String) =
        withContext(coroutineDispatchers.io) {
            val response: WeatherForecast
            val datasourceResponse =
                Response.success(weatherForecastLocalDataSource.loadForecastData(city))
            response = modelsConverter.convert(
                temperatureType,
                city,
                datasourceResponse
            )
            return@withContext LoadResult.Local(response, remoteError)
        }

    @InternalSerializationApi
    private suspend fun saveForecast(response: WeatherForecastResponse) =
        withContext(coroutineDispatchers.io) {
            weatherForecastLocalDataSource.saveForecastData(response)
        }
}