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
import retrofit2.Response

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
            val response = hourlyWeatherRemoteDataSource.loadHourlyWeatherForCity(city)
            val result = modelsConverter.convert(
                temperatureType,
                city,
                response
            )
            saveWeather(response.body()!!)     //NPE handled in hourlyWeatherViewModel's exceptionHandler
            return@withContext LoadResult.Remote(result)
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
            result = modelsConverter.convert(
                temperatureType,
                response.body()!!.city.name,
                response
            )
            saveWeather(response.body()!!)     //NPE handled in hourlyWeatherViewModel's exceptionHandler
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
            val datasourceResponse =
                Response.success(hourlyWeatherLocalDataSource.getHourlyWeather(city))
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