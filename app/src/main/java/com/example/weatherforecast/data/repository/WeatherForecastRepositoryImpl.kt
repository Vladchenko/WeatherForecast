package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.converter.ForecastDataToDomainModelsConverter
import com.example.weatherforecast.data.repository.datasource.WeatherForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.forecast.WeatherForecastRepository
import com.example.weatherforecast.models.data.WeatherForecastResponse
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val modelsConverter: ForecastDataToDomainModelsConverter,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val temperatureType: TemperatureType
) : WeatherForecastRepository {

    override suspend fun loadRemoteForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): Result<WeatherForecastDomainModel> =
        withContext(coroutineDispatchers.io) {
            val result: WeatherForecastDomainModel
            val response = weatherForecastRemoteDataSource.loadForecastDataForCity(city)
            result = modelsConverter.convert(
                temperatureType,
                city,
                response
            )
            saveForecast(response.body()!!)     //TODO Handle null body
            return@withContext Result.success(result)
        }

    override suspend fun loadAndSaveRemoteForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<WeatherForecastDomainModel> =
        withContext(coroutineDispatchers.io) {
            val result: WeatherForecastDomainModel
            try {
                val response = weatherForecastRemoteDataSource.loadForecastDataForCity(city)
                result = modelsConverter.convert(
                    temperatureType,
                    city,
                    response
                )
                saveForecast(response.body()!!)     //TODO Handle null body
            } catch (ex: Exception) {
                return@withContext LoadResult.Local(
                    try {
                        loadLocalForecast(city)
                    } catch (ex: Exception) {
                        return@withContext LoadResult.Fail(ex.message.orEmpty())
                    },
                    ex.message.orEmpty()
                )
            }
            return@withContext LoadResult.Remote(result)
        }

    override suspend fun loadAndSaveRemoteForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<WeatherForecastDomainModel> =
        withContext(coroutineDispatchers.io) {
            val result: WeatherForecastDomainModel
            try {
                val response = weatherForecastRemoteDataSource.loadForecastForLocation(latitude, longitude)
                result = modelsConverter.convert(
                    temperatureType,
                    response.body()!!.city,
                    response
                )
                saveForecast(response.body()!!)     //TODO Handle null body
            } catch (ex: Exception) {
//                return@withContext LoadResult.Local(
//                    try {
//                        loadLocalForecast(city) //TODO Should there be save for lat lon, or define city by lat lon and save it ?
//                    } catch (ex: Exception) {
                        return@withContext LoadResult.Fail(ex.message.orEmpty())
//                    },
//                    ex.message.orEmpty()
//                )
            }
            return@withContext LoadResult.Remote(result)
        }


    override suspend fun loadLocalForecast(city: String) =
        withContext(coroutineDispatchers.io) {
            val response: WeatherForecastDomainModel
            val datasourceResponse = Response.success(weatherForecastLocalDataSource.loadForecastData(city))
            response = modelsConverter.convert(
                temperatureType,
                city,
                datasourceResponse
            )
            return@withContext response
        }

    private suspend fun saveForecast(response: WeatherForecastResponse) =
        withContext(coroutineDispatchers.io) {
            launch {
                weatherForecastLocalDataSource.saveForecastData(response)
            }
        }
}