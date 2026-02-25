package com.example.weatherforecast.data.repository

import android.util.Log
import com.example.weatherforecast.data.mapper.CurrentWeatherDtoMapper
import com.example.weatherforecast.data.mapper.CurrentWeatherEntityMapper
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherLocalDataSource
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.data.util.WeatherErrorMapper
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.forecast.CurrentWeatherRepository
import com.example.weatherforecast.models.data.database.CurrentWeatherEntity
import com.example.weatherforecast.models.data.network.CurrentWeatherDto
import com.example.weatherforecast.models.domain.CurrentWeather
import com.example.weatherforecast.models.domain.ForecastError
import com.example.weatherforecast.models.domain.LoadResult
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

/**
 * Implementation of [CurrentWeatherRepository] that provides current weather data
 * by combining remote and local data sources with fallback and caching strategies.
 *
 * ## Responsibilities
 * - Fetches real-time weather data from the remote API (by city name or coordinates).
 * - Saves successfully fetched data to the local cache using [CurrentWeatherLocalDataSource].
 * - On failure (network issues, HTTP errors), attempts to load previously cached data.
 * - Maps data between layers:
 *   - Network DTO → Database Entity → Domain Model ([CurrentWeather])
 * - Handles errors uniformly using [WeatherErrorMapper] and provides meaningful [ForecastError] instances.
 *
 * ## Data Flow Strategy
 * 1. Try to fetch fresh data from [CurrentWeatherRemoteDataSource].
 * 2. If successful:
 *    - Map DTO to entity and save via [saveWeather].
 *    - Return [LoadResult.Remote] with domain model.
 * 3. If request fails:
 *    - Use [WeatherErrorMapper.mapToLoadResult] to convert error into [ForecastError].
 *    - Attempt to load cached data via [loadCachedWeatherForCity], returning [LoadResult.Local] if available.
 * 4. If no cache exists or reading fails — return [LoadResult.Error].
 *
 * ## Error Handling
 * Errors are processed consistently:
 * - HTTP errors (404, 401, 5xx) → mapped via [handleApiError] → [WeatherErrorMapper]
 * - Network exceptions (NoInternet, IOException) → caught in `catch` block → mapped via [WeatherErrorMapper]
 * - Local database errors → result in [ForecastError.LocalDataCorrupted]
 *
 * This ensures a unified error experience across the app.
 *
 * @property dtoMapper Mapper for converting [CurrentWeatherDto] to [CurrentWeatherEntity].
 * @property entityMapper Mapper for converting [CurrentWeatherEntity] to [CurrentWeather].
 * @property coroutineDispatchers Dispatcher provider for coroutine context management.
 * @property currentWeatherLocalDataSource Data source for persistent storage of current weather.
 * @property currentWeatherRemoteDataSource Data source for fetching data from the remote API.
 *
 */
@InternalSerializationApi
class CurrentWeatherRepositoryImpl(
    private val dtoMapper: CurrentWeatherDtoMapper,
    private val entityMapper: CurrentWeatherEntityMapper,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
    private val currentWeatherRemoteDataSource: CurrentWeatherRemoteDataSource,
) : CurrentWeatherRepository {

    @InternalSerializationApi
    override suspend fun refreshWeatherForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<CurrentWeather> =
        withContext(coroutineDispatchers.io) {
            try {
                val response = currentWeatherRemoteDataSource.loadWeatherForCity(city)
                fetchAndSave(response, temperatureType)
                    ?: loadCachedWeatherForCityOrError(
                        city,
                        temperatureType,
                        ForecastError.NetworkError(Exception("Empty response body"))
                    )
            } catch (ex: Exception) {
                val error = if (ex is ForecastError) ex else ForecastError.NetworkError(ex)
                loadCachedWeatherForCityOrError(city, temperatureType, error)
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
                val response =
                    currentWeatherRemoteDataSource.loadWeatherForLocation(latitude, longitude)
                fetchAndSave(response, temperatureType)
                    ?: LoadResult.Error(ForecastError.NetworkError(Exception("Empty response body")))
            } catch (ex: Exception) {
                return@withContext WeatherErrorMapper.mapToLoadResult(ex)
            }
        }

    @InternalSerializationApi
    private suspend fun fetchAndSave(
        response: Response<CurrentWeatherDto>,
        temperatureType: TemperatureType
    ): LoadResult<CurrentWeather>? {
        return if (response.isSuccessful) {
            val body = response.body() ?: return null
            val entity = dtoMapper.toEntity(body)
            saveWeather(entity)
            val domainModel = entityMapper.toDomain(entity, temperatureType)
            LoadResult.Remote(domainModel)
        } else {
            handleApiError(response)
        }
    }

    private fun handleApiError(response: Response<*>): LoadResult<CurrentWeather> {
        val errorMessage = try {
            response.errorBody()?.string()
                ?: "Response for city is not successful and error body is empty"
        } catch (ex: Exception) {
            "Failed to read error body: $ex"
        }
        Log.e(TAG, "HTTP ${response.code()}: $errorMessage")

        return WeatherErrorMapper.mapToLoadResult(response)
    }

    @InternalSerializationApi
    private suspend fun loadCachedWeatherForCityOrError(
        city: String,
        temperatureType: TemperatureType,
        remoteError: ForecastError
    ): LoadResult<CurrentWeather> {
        return try {
            loadCachedWeatherForCity(city, temperatureType, remoteError)
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to load cached weather for city $city: $ex")
            LoadResult.Error(ForecastError.NoInternet)
        }
    }

    @InternalSerializationApi
    override suspend fun loadCachedWeatherForCity(
        city: String,
        temperatureType: TemperatureType,
        remoteError: ForecastError
    ): LoadResult<CurrentWeather> =
        withContext(coroutineDispatchers.io) {
            return@withContext try {
                val localModel = currentWeatherLocalDataSource.loadWeather(city)
                val domainModel = entityMapper.toDomain(localModel.body()!!, temperatureType)
                LoadResult.Local(domainModel, remoteError)
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to load cached weather for city $city: $ex")
                LoadResult.Error(
                    ForecastError.LocalDataCorrupted(
                        message = "Local database query failed: $ex"
                    )
                )
            }
        }

    @InternalSerializationApi
    private suspend fun saveWeather(response: CurrentWeatherEntity) =
        withContext(coroutineDispatchers.io) {
            currentWeatherLocalDataSource.saveWeather(response)
        }

    companion object {
        private const val TAG = "CurrentWeatherRepository"
    }
}