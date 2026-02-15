package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.converter.HourlyForecastModelsConverter
import com.example.weatherforecast.data.repository.datasource.HourlyForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.HourlyForecastRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.forecast.HourlyForecastRepository
import com.example.weatherforecast.models.data.HourlyForecastResponse
import com.example.weatherforecast.models.domain.HourlyForecastDomainModel
import com.example.weatherforecast.models.domain.LoadResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

/**
 * WeatherForecastRepository implementation. Provides data for domain layer.
 *
 * @property hourlyForecastRemoteDataSource source of remote data for domain layer
 * @property hourlyForecastLocalDataSource source of local data for domain layer
 * @property modelsConverter converts server response to domain entity
 * @property coroutineDispatchers dispatchers for coroutines
 */
class HourlyForecastRepositoryImpl(
    private val hourlyForecastRemoteDataSource: HourlyForecastRemoteDataSource,
    private val hourlyForecastLocalDataSource: HourlyForecastLocalDataSource,
    private val modelsConverter: HourlyForecastModelsConverter,
    private val coroutineDispatchers: CoroutineDispatchers,
) : HourlyForecastRepository {

    @InternalSerializationApi
    override suspend fun loadHourlyForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<HourlyForecastDomainModel> =
        withContext(coroutineDispatchers.io) {
            val response = hourlyForecastRemoteDataSource.loadHourlyForecastForCity(city)
            val result = modelsConverter.convert(
                temperatureType,
                city,
                response
            )
            saveForecast(response.body()!!)     //NPE handled in hourlyForecastViewModel's exceptionHandler
            return@withContext LoadResult.Remote(result)
        }

    @InternalSerializationApi
    override suspend fun loadHourlyForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyForecastDomainModel> =
        withContext(coroutineDispatchers.io) {
            val result: HourlyForecastDomainModel
            val response =
                hourlyForecastRemoteDataSource.loadHourlyForecastForLocation(latitude, longitude)
            result = modelsConverter.convert(
                temperatureType,
                response.body()!!.city.name,
                response
            )
            saveForecast(response.body()!!)     //NPE handled in hourlyForecastViewModel's exceptionHandler
            return@withContext LoadResult.Remote(result)
        }

    @InternalSerializationApi
    override suspend fun loadLocalForecast(city: String, remoteError: String) =
        withContext(coroutineDispatchers.io) {
            val response: HourlyForecastDomainModel
            val datasourceResponse =
                Response.success(hourlyForecastLocalDataSource.getHourlyForecastData(city))
            response = modelsConverter.convert(
                temperatureType,
                city,
                datasourceResponse
            )
            return@withContext LoadResult.Local(response, remoteError)
        }

    @InternalSerializationApi
    private suspend fun saveForecast(response: HourlyForecastResponse) =
        withContext(coroutineDispatchers.io) {
            launch {
                hourlyForecastLocalDataSource.saveHourlyForecastData(response)
            }
        }
}