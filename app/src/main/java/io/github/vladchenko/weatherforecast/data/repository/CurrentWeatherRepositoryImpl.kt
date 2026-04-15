package io.github.vladchenko.weatherforecast.data.repository

import io.github.vladchenko.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import io.github.vladchenko.weatherforecast.data.mapper.CurrentWeatherDtoMapper
import io.github.vladchenko.weatherforecast.data.mapper.CurrentWeatherEntityMapper
import io.github.vladchenko.weatherforecast.data.repository.datasource.CurrentWeatherLocalDataSource
import io.github.vladchenko.weatherforecast.data.repository.datasource.CurrentWeatherRemoteDataSource
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.data.util.TemperatureType
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.domain.forecast.CurrentWeatherRepository
import io.github.vladchenko.weatherforecast.models.data.DataError
import io.github.vladchenko.weatherforecast.models.data.DataErrorToForecastErrorMapper
import io.github.vladchenko.weatherforecast.models.data.DataResult
import io.github.vladchenko.weatherforecast.models.data.database.CurrentWeatherEntity
import io.github.vladchenko.weatherforecast.models.data.network.CurrentWeatherDto
import io.github.vladchenko.weatherforecast.models.domain.CurrentWeather
import io.github.vladchenko.weatherforecast.models.domain.ForecastError
import io.github.vladchenko.weatherforecast.models.domain.LoadResult
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi

/**
 * Implementation of [CurrentWeatherRepository] that retrieves and caches current weather data
 * using a combination of remote and local data sources with fallback mechanisms.
 *
 * ## Responsibilities
 * - Fetches real-time current weather data from the remote API by coordinates.
 * - Saves successfully fetched data to the local database via [CurrentWeatherLocalDataSource].
 * - On remote fetch failure, attempts to load previously cached data as fallback.
 * - Maps data between layers:
 *   - Network DTO ([CurrentWeatherDto]) → Database Entity ([CurrentWeatherEntity]) → Domain Model ([CurrentWeather])
 * - Handles errors uniformly by converting [DataError] (data layer) to [ForecastError] (domain layer) using [DataErrorToForecastErrorMapper].
 *
 * ## Data Flow Strategy
 * 1. Try to fetch fresh data from [CurrentWeatherRemoteDataSource].
 * 2. If successful:
 *    - Map DTO to entity and save via [saveWeather].
 *    - Return [LoadResult.Remote] with domain model.
 * 3. If request fails:
 *    - Convert [DataResult.Error] to domain-level [ForecastError] using mapper.
 *    - Attempt to load cached data via [loadCachedWeatherForLocation], returning [LoadResult.Local] if available.
 * 4. If no cache exists or reading fails — return [LoadResult.Error].
 *
 * ## Error Handling
 * All errors originate in the data layer as [DataError] and are mapped to meaningful [ForecastError] instances:
 * - [DataError.NetworkError] → [ForecastError.NoInternet]
 * - [DataError.RequestFailError] → [ForecastError.CityNotFound]
 * - [DataError.ServerError], [DataError.ApiKeyInvalid], [ResponseNoBodyError] → [ForecastError.NoDataAvailable]
 * - [DataError.DatabaseError] → [ForecastError.LocalDataCorrupted]
 *
 * This ensures clean separation between layers and prevents data-layer types from leaking into the domain.
 *
 * ## Caching Behavior
 * - Fresh data is always preferred and triggers cache update.
 * - Cache is used only when remote data cannot be retrieved.
 * - Cache miss after remote failure results in [LoadResult.Error].
 *
 * ## Thread Safety
 * All operations are dispatched on [CoroutineDispatchers.io], making this repository safe to use on any thread
 * when called within structured concurrency.
 *
 * @property loggingService to log events
 * @property dtoMapper Mapper for converting [CurrentWeatherDto] to [CurrentWeatherEntity].
 * @property entityMapper Mapper for converting [CurrentWeatherEntity] to [CurrentWeather].
 * @property coroutineDispatchers Dispatcher provider for background execution.
 * @property errorMapper Converts data-layer [DataError] into domain-level [ForecastError].
 * @property currentWeatherLocalDataSource Data source for persistent storage and retrieval of weather data.
 * @property currentWeatherRemoteDataSource Data source for fetching data from the remote API.
 */
@InternalSerializationApi
class CurrentWeatherRepositoryImpl(
    private val loggingService: LoggingService,
    private val dtoMapper: CurrentWeatherDtoMapper,
    private val entityMapper: CurrentWeatherEntityMapper,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val errorMapper: DataErrorToForecastErrorMapper,
    private val currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
    private val currentWeatherRemoteDataSource: CurrentWeatherRemoteDataSource
) : CurrentWeatherRepository {

    @InternalSerializationApi
    override suspend fun refreshWeatherForLocation(
        city: String,
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<CurrentWeather> =
        withContext(coroutineDispatchers.io) {
            val result = currentWeatherRemoteDataSource.loadWeatherForLocation(city, latitude, longitude)

            when (result) {
                is DataResult.Success -> {
                    fetchAndSave(result.data, city, temperatureType)
                        ?: LoadResult.Error(city, ForecastError.NoDataAvailable("Mapped DTO is null"))
                }
                is DataResult.Error -> {
                    loadCachedWeatherForLocationOrError(city, temperatureType, errorMapper.map(result.error))
                }
            }
        }

    @InternalSerializationApi
    private suspend fun fetchAndSave(
        dto: CurrentWeatherDto,
        city: String,
        temperatureType: TemperatureType
    ): LoadResult<CurrentWeather>? {
        return try {
            val entity = dtoMapper.toEntity(dto, city)
            saveWeather(entity)
            val domainModel = entityMapper.toDomain(entity, temperatureType)
            LoadResult.Remote(domainModel)
        } catch (ex: Exception) {
            loggingService.logError(TAG, "Failed to map or save weather data: $ex", ex)
            LoadResult.Error(city, ForecastError.UncategorizedError(ex.message.toString(), ex))
        }
    }

    @InternalSerializationApi
    private suspend fun loadCachedWeatherForLocationOrError(
        city: String,
        temperatureType: TemperatureType,
        remoteError: ForecastError
    ): LoadResult<CurrentWeather> {
        return try {
            loadCachedWeatherForLocation(city, temperatureType, remoteError)
        } catch (ex: Exception) {
            loggingService.logError(TAG, "Failed to load cached weather for city $city: $ex", ex)
            LoadResult.Error(city, ForecastError.LocalDataCorrupted("Failed to read from cache: ${ex.message}"))
        }
    }

    @InternalSerializationApi
    private suspend fun loadCachedWeatherForLocation(
        city: String,
        temperatureType: TemperatureType,
        remoteError: ForecastError
    ): LoadResult<CurrentWeather> =
        withContext(coroutineDispatchers.io) {
            try {
                val localModel = currentWeatherLocalDataSource.loadWeather(city)
                val domainModel = entityMapper.toDomain(localModel, temperatureType)
                LoadResult.Local(domainModel, remoteError)
            } catch (_: NoSuchDatabaseEntryException) {
                loggingService.logDebugEvent(TAG, "No cached weather data found for city: $city")
                LoadResult.Error(city, ForecastError.NoDataAvailable("No cached data for $city"))
            } catch (ex: Exception) {
                loggingService.logError(TAG, "Failed to load cached weather for city $city: $ex", ex)
                LoadResult.Error(
                    city,
                    ForecastError.LocalDataCorrupted("Local database query failed: ${ex.message}")
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