package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.mapper.HourlyWeatherDtoMapper
import com.example.weatherforecast.data.mapper.HourlyWeatherEntityMapper
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherLocalDataSource
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherRemoteDataSource
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.forecast.HourlyWeatherRepository
import com.example.weatherforecast.models.data.DataError
import com.example.weatherforecast.models.data.DataErrorToForecastErrorMapper
import com.example.weatherforecast.models.data.DataResult
import com.example.weatherforecast.models.data.network.HourlyWeatherDto
import com.example.weatherforecast.models.domain.ForecastError
import com.example.weatherforecast.models.domain.HourlyWeatherDomainModel
import com.example.weatherforecast.models.domain.LoadResult
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi

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
 * - Handles errors uniformly using [DataErrorToForecastErrorMapper] and provides meaningful [ForecastError] instances.
 *
 * ## Data Flow Strategy
 * 1. Try to fetch fresh data from [HourlyWeatherRemoteDataSource].
 * 2. If successful:
 *    - Map DTO to entity and save in [HourlyWeatherLocalDataSource].
 *    - Return [LoadResult.Remote] with domain model.
 * 3. If request fails (HTTP error, network issue):
 *    - Convert [DataError] to domain-level [ForecastError] using mapper.
 *    - Attempt to load cached data via [loadCachedWeather], returning [LoadResult.Local] if available.
 * 4. If no cache exists or reading fails — return [LoadResult.Error].
 *
 * ## Error Handling
 * Errors from data layer ([DataError]) are mapped to domain-level [ForecastError] using [DataErrorToForecastErrorMapper].
 * This ensures consistent error handling across all repositories without leaking data-layer types.
 *
 * @property loggingService to log events
 * @property dispatchers Dispatcher provider for coroutine context management.
 * @property dtoMapper Mapper for converting [HourlyWeatherDto] to database entity.
 * @property entityMapper Mapper for converting entity to [HourlyWeatherDomainModel].
 * @property localDataSource Data source for persistent storage of hourly weather.
 * @property remoteDataSource Data source for fetching data from the remote API.
 * @property errorMapper Converts [DataError] to [ForecastError] for domain consistency.
 */
@InternalSerializationApi
class HourlyWeatherRepositoryImpl(
    private val loggingService: LoggingService,
    private val dispatchers: CoroutineDispatchers,
    private val dtoMapper: HourlyWeatherDtoMapper,
    private val entityMapper: HourlyWeatherEntityMapper,
    private val errorMapper: DataErrorToForecastErrorMapper,
    private val localDataSource: HourlyWeatherLocalDataSource,
    private val remoteDataSource: HourlyWeatherRemoteDataSource,
) : HourlyWeatherRepository {

    override suspend fun refreshWeatherForCity(
        city: String,
        temperatureType: TemperatureType
    ): LoadResult<HourlyWeatherDomainModel> =
        withContext(dispatchers.io) {
            when (val result = remoteDataSource.loadHourlyWeatherForCity(city)) {
                is DataResult.Success -> {
                    handleSuccessResponse(result.data, city, temperatureType)
                }
                is DataResult.Error -> {
                    val forecastError = errorMapper.map(result.error)
                    loadCachedWeather(city, temperatureType, forecastError)
                }
            }
        }

    override suspend fun refreshWeatherForLocation(
        city: String,
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyWeatherDomainModel> =
        withContext(dispatchers.io) {
            when (val result = remoteDataSource.loadHourlyWeatherForLocation(latitude, longitude)) {
                is DataResult.Success -> {
                    handleSuccessResponse(result.data, city, temperatureType)
                }
                is DataResult.Error -> {
                    val forecastError = errorMapper.map(result.error)
                    loadCachedWeather(city, temperatureType, forecastError)
                }
            }
        }

    override suspend fun loadCachedWeather(
        city: String,
        temperatureType: TemperatureType,
        remoteError: ForecastError
    ): LoadResult<HourlyWeatherDomainModel> =
        withContext(dispatchers.io) {
            try {
                val entity = localDataSource.getHourlyWeather(city)
                    ?: return@withContext LoadResult.Error(
                        ForecastError.NoDataAvailable("No cached data found for city: $city")
                    )
                val domainModel = entityMapper.toDomain(entity, temperatureType)
                loggingService.logDebugEvent(TAG, "Loaded hourly weather from cache for city: $city")
                LoadResult.Local(domainModel, remoteError)
            } catch (e: Exception) {
                loggingService.logError(TAG, "Failed to load cached hourly weather for $city", e)
                LoadResult.Error(ForecastError.LocalDataCorrupted("Cache read failed: ${e.message}"))
            }
        }

    private suspend fun handleSuccessResponse(
        dto: HourlyWeatherDto,
        city: String,
        temperatureType: TemperatureType
    ): LoadResult<HourlyWeatherDomainModel> {
        return try {
            val entity = dtoMapper.toEntity(dto)
            localDataSource.saveHourlyWeather(entity)
            val domainModel = entityMapper.toDomain(entity, temperatureType)
            loggingService.logDebugEvent(TAG, "Saved and mapped hourly weather for city: $city")
            LoadResult.Remote(domainModel)
        } catch (e: Exception) {
            loggingService.logError(TAG, "Failed to map or save hourly weather for city: $city", e)
            LoadResult.Error(ForecastError.LocalDataCorrupted("Mapping or saving failed: ${e.message}"))
        }
    }

    companion object {
        private const val TAG = "HourlyWeatherRepository"
    }
}