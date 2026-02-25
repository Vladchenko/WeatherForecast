package com.example.weatherforecast.data.repository

import android.util.Log
import com.example.weatherforecast.data.mapper.HourlyWeatherDtoMapper
import com.example.weatherforecast.data.mapper.HourlyWeatherEntityMapper
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherLocalDataSource
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.data.util.WeatherErrorMapper
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
 * Implementation of [HourlyWeatherRepository] that provides hourly weather data
 * by combining remote and local data sources with fallback and caching strategies.
 *
 * ## Responsibilities
 * - Fetches fresh hourly weather data from the remote API (by city name or coordinates).
 * - Saves successfully fetched data to the local cache for offline use.
 * - On failure (network issues, HTTP errors), attempts to load previously cached data.
 * - Maps data between layers:
 *   - Network DTO → Database Entity → Domain Model
 * - Handles errors uniformly using [WeatherErrorMapper] and provides meaningful [ForecastError] instances.
 *
 * ## Data Flow Strategy
 * 1. Try to fetch fresh data from [HourlyWeatherRemoteDataSource].
 * 2. If successful:
 *    - Map DTO to entity and save in [HourlyWeatherLocalDataSource].
 *    - Return [LoadResult.Remote] with domain model.
 * 3. If request fails (HTTP error, network issue):
 *    - Use [WeatherErrorMapper] to convert the error into a domain-level [ForecastError].
 *    - Attempt to load cached data via [loadCachedWeather], returning [LoadResult.Local] if available.
 * 4. If no cache exists or reading fails — return [LoadResult.Error].
 *
 * ## Error Handling
 * Errors are processed using [WeatherErrorMapper.mapToLoadResult], which supports:
 * - HTTP errors (404, 401, 5xx)
 * - Network exceptions (NoInternet, IOException)
 * - Retrofit-specific exceptions (HttpException)
 *
 * This ensures consistent error handling across all repositories.
 *
 * @property dispatchers Dispatcher provider for coroutine context management.
 * @property dtoMapper Mapper for converting [HourlyWeatherDto] to database entity.
 * @property entityMapper Mapper for converting entity to [HourlyWeatherDomainModel].
 * @property localDataSource Data source for persistent storage of hourly weather.
 * @property remoteDataSource Data source for fetching data from the remote API.
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
                val errorResult = WeatherErrorMapper.mapToLoadResult(response)
                loadCachedWeather(city, temperatureType, errorResult.error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error for city: $city", e)
            val errorResult = WeatherErrorMapper.mapToLoadResult(e)
            loadCachedWeather(city, temperatureType, errorResult.error)
        }
    }

    private suspend fun handleSuccessResponse(
        dto: HourlyWeatherDto,
        city: String,
        temperatureType: TemperatureType
    ): LoadResult<HourlyWeatherDomainModel> {
        val entity = dtoMapper.toEntity(dto)
        localDataSource.saveHourlyWeather(entity)
        val domainModel = entityMapper.toDomain(entity, temperatureType)
        Log.d(TAG, "Saved and mapped hourly weather for city: $city")
        return LoadResult.Remote(domainModel)
    }

    companion object {
        private const val TAG = "HourlyWeatherRepository"
    }
}