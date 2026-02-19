package com.example.weatherforecast.data.repository

import android.util.Log
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
    override suspend fun refreshWeatherForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<CurrentWeather> =
        withContext(coroutineDispatchers.io) {
            try {
                return@withContext loadWeatherForCityAndSave(city, temperatureType)
            } catch (ex: Exception) {
                return@withContext loadCachedWeatherForCityOrError(city, temperatureType, ex)
            }
        }

    @InternalSerializationApi
    private suspend fun loadWeatherForCityAndSave(
        city: String,
        temperatureType: TemperatureType
    ): LoadResult<CurrentWeather> {
        val response = currentWeatherRemoteDataSource.loadWeatherForCity(city)
        return if (response.isSuccessful) {
            handleSuccessResponse(response, temperatureType, city)
        } else {
            handleApiError(response)
        }
    }

    @InternalSerializationApi
    private suspend fun handleSuccessResponse(
        response: Response<CurrentWeatherResponse>,
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<CurrentWeather> {
        val body = response.body()
            ?: return LoadResult.Error(Exception("Response for city is successful, but its body is empty"))
        val result = modelsConverter.convert(temperatureType, city, response)
        saveWeather(body)
        return LoadResult.Remote(result)
    }

    private fun handleApiError(response: Response<*>): LoadResult<CurrentWeather> {
        val errorMessage = try {
            response.errorBody()?.string() ?: "Unknown error"
        } catch (e: Exception) {
            "Failed to read error body"
        }
        Log.e(TAG, "HTTP ${response.code()}: $errorMessage")
        return LoadResult.Error(Exception(errorMessage))
    }

    @InternalSerializationApi
    private suspend fun loadCachedWeatherForCityOrError(
        city: String,
        temperatureType: TemperatureType,
        ex: Exception
    ): LoadResult<CurrentWeather> {
        return try {
            loadCachedWeatherForCity(
                city, temperatureType, ex.message.toString()
            )
        } catch (ex: Exception) {
            LoadResult.Error(ex)
        }
    }

    @InternalSerializationApi
    override suspend fun refreshWeatherForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<CurrentWeather> =
        withContext(coroutineDispatchers.io) {
            try {
                return@withContext loadWeatherForLocationAndSave(
                    latitude,
                    longitude,
                    temperatureType
                )
            } catch (ex: Exception) {
                // TODO Implement loading weather for location from local db
                return@withContext LoadResult.Error(ex)
            }
        }

    @InternalSerializationApi
    private suspend fun loadWeatherForLocationAndSave(
        latitude: Double,
        longitude: Double,
        temperatureType: TemperatureType
    ): LoadResult<CurrentWeather> {
        val response =
            currentWeatherRemoteDataSource.loadWeatherForLocation(latitude, longitude)
        val body = response.body()
            ?: return LoadResult.Error(
                Exception("Response for location is successful, but its body is empty")
            )
        val result = modelsConverter.convert(
            temperatureType,
            body.city,
            response
        )
        saveWeather(body)
        return LoadResult.Remote(result)
    }

    @InternalSerializationApi
    override suspend fun loadCachedWeatherForCity(
        city: String,
        temperatureType: TemperatureType,
        remoteError: String
    ) =
        withContext(coroutineDispatchers.io) {
            val response: CurrentWeather
            val datasourceResponse = currentWeatherLocalDataSource.loadWeather(city)
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

    companion object {
        private const val TAG = "CurrentWeatherRepository"
    }
}