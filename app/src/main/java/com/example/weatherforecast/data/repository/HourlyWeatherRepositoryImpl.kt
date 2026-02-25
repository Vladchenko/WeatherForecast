package com.example.weatherforecast.data.repository

import android.util.Log
import com.example.weatherforecast.data.mapper.HourlyWeatherDtoMapper
import com.example.weatherforecast.data.mapper.HourlyWeatherEntityMapper
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherLocalDataSource
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.forecast.HourlyWeatherRepository
import com.example.weatherforecast.models.data.network.HourlyWeatherDto
import com.example.weatherforecast.models.domain.ForecastError
import com.example.weatherforecast.models.domain.HourlyWeatherDomainModel
import com.example.weatherforecast.models.domain.LoadResult
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

/**
 * Repository implementation for hourly weather data.
 *
 * Uses remote and local data sources with proper mapping between layers.
 */
@InternalSerializationApi
class HourlyWeatherRepositoryImpl(
    private val dispatchers: CoroutineDispatchers,
    private val dtoMapper: HourlyWeatherDtoMapper,
    private val entityMapper: HourlyWeatherEntityMapper,
    private val localDataSource: HourlyWeatherLocalDataSource,
    private val remoteDataSource: HourlyWeatherRemoteDataSource
) : HourlyWeatherRepository {

    override suspend fun refreshWeatherForCity(
        city: String,
        temperatureType: TemperatureType
    ): LoadResult<HourlyWeatherDomainModel> =
        withContext(dispatchers.io) {
            safeApiCall(
                apiCall = { remoteDataSource.loadHourlyWeatherForCity(city) },
                city = city,
                temperatureType = temperatureType
            )
        }

    override suspend fun refreshWeatherForLocation(
        city: String,
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyWeatherDomainModel> =
        withContext(dispatchers.io) {
            safeApiCall(
                apiCall = { remoteDataSource.loadHourlyWeatherForLocation(latitude, longitude) },
                city = city,
                temperatureType = temperatureType
            )
        }

    override suspend fun loadCachedWeather(
        city: String,
        temperatureType: TemperatureType,
        remoteError: ForecastError
    ): LoadResult<HourlyWeatherDomainModel> =
        withContext(dispatchers.io) {
            try {
                val entity = localDataSource.getHourlyWeather(city) ?: return@withContext LoadResult.Error(
                    ForecastError.NoDataAvailable("No cached data found for city: $city")
                )

                val domainModel = entityMapper.toDomain(entity, temperatureType)

                Log.d(TAG, "Loaded hourly weather from cache for city: $city")
                LoadResult.Local(domainModel, remoteError)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load cached hourly weather for $city", e)
                LoadResult.Error(ForecastError.LocalDataCorrupted("Cache read failed: ${e.message}"))
            }
        }

    private suspend fun safeApiCall(
        apiCall: suspend () -> Response<HourlyWeatherDto>,
        city: String,
        temperatureType: TemperatureType
    ): LoadResult<HourlyWeatherDomainModel> {
        return try {
            val response = apiCall()
            if (response.isSuccessful && response.body() != null) {
                handleSuccessResponse(response.body()!!, city, temperatureType)
            } else {
                handleApiError(response, city, temperatureType)
            }
        } catch (e: Exception) {
            val error = when (e) {
                is ForecastError -> e
                else -> ForecastError.NetworkError(e)
            }
            Log.e(TAG, "Network error for city: $city", e)
            loadCachedWeather(city, temperatureType, error)
        }
    }

    private suspend fun handleSuccessResponse(
        dto: HourlyWeatherDto,
        city: String,
        temperatureType: TemperatureType
    ): LoadResult<HourlyWeatherDomainModel> {
        // Convert DTO → DB entity
        val entity = dtoMapper.toEntity(dto)

        // Save to DB
        localDataSource.saveHourlyWeather(entity)

        // Map to domain
        val domainModel = entityMapper.toDomain(entity, temperatureType)

        Log.d(TAG, "Saved and mapped hourly weather for city: $city")
        return LoadResult.Remote(domainModel)
    }

    private suspend fun handleApiError(
        response: Response<*>,
        city: String,
        temperatureType: TemperatureType
    ): LoadResult<HourlyWeatherDomainModel> {
        val message = try {
            response.errorBody()?.string() ?: "HTTP ${response.code()} error"
        } catch (e: Exception) {
            "Failed to read error body: $e"
        }

        val error = when (response.code()) {
            404 -> ForecastError.CityNotFound(city = city, message = message)
            401 -> ForecastError.ApiKeyInvalid(message)
            in 500..599 -> ForecastError.ServerError(response.code(), message)
            else -> ForecastError.NetworkError(Exception("HTTP ${response.code()} $message"))
        }

        Log.d(TAG, "API error for $city: $error")
        return loadCachedWeather(city, temperatureType, error)
    }

    companion object {
        private const val TAG = "HourlyWeatherRepository"
    }
}