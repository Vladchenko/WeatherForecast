package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.converter.CurrentWeatherModelConverter
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherLocalDataSource
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.forecast.CurrentWeatherRepository
import com.example.weatherforecast.models.data.CurrentWeatherResponse
import com.example.weatherforecast.models.domain.CurrentWeather
import com.example.weatherforecast.models.domain.LoadResult
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

/**
 * WeatherForecastRepository implementation. Provides data for domain layer.
 *
 * @property currentWeatherRemoteDataSource source of remote data for domain layer
 * @property currentWeatherLocalDataSource source of local data for domain layer
 * @property modelsConverter converts server response to domain entity
 * @property coroutineDispatchers dispatchers for coroutines
 */
class CurrentWeatherRepositoryImpl(
    private val currentWeatherRemoteDataSource: CurrentWeatherRemoteDataSource,
    private val currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
    private val modelsConverter: CurrentWeatherModelConverter,
    private val coroutineDispatchers: CoroutineDispatchers,
) : CurrentWeatherRepository {

    @InternalSerializationApi
    override suspend fun loadAndSaveRemoteWeatherForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<CurrentWeather> =
        withContext(coroutineDispatchers.io) {
            val response = currentWeatherRemoteDataSource.loadWeatherForCity(city)
            val result = modelsConverter.convert(
                temperatureType,
                city,
                response
            )
            saveWeather(response.body()!!)     //NPE handled in WeatherForecastViewModel's exceptionHandler
            return@withContext LoadResult.Remote(result)
        }

    @InternalSerializationApi
    override suspend fun loadAndSaveRemoteWeatherForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<CurrentWeather> =
        withContext(coroutineDispatchers.io) {
            val result: CurrentWeather
            val response =
                currentWeatherRemoteDataSource.loadWeatherForLocation(latitude, longitude)
            result = modelsConverter.convert(
                temperatureType,
                response.body()!!.city,
                response
            )
            saveWeather(response.body()!!)     //NPE handled in WeatherForecastViewModel's exceptionHandler
            return@withContext LoadResult.Remote(result)
        }

    @InternalSerializationApi
    override suspend fun loadCachedWeather(
        city: String,
        temperatureType: TemperatureType,
        remoteError: String
    ) =
        withContext(coroutineDispatchers.io) {
            val response: CurrentWeather
            val datasourceResponse =
                Response.success(currentWeatherLocalDataSource.loadWeather(city))
            response = modelsConverter.convert(
                temperatureType,
                city,
                datasourceResponse
            )
            return@withContext LoadResult.Local(response, remoteError)
        }

    @InternalSerializationApi
    private suspend fun saveWeather(response: CurrentWeatherResponse) =
        withContext(coroutineDispatchers.io) {
            currentWeatherLocalDataSource.saveWeather(response)
        }
}