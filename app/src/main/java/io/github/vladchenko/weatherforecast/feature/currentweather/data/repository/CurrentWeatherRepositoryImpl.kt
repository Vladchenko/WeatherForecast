package io.github.vladchenko.weatherforecast.feature.currentweather.data.repository

import io.github.vladchenko.weatherforecast.core.data.mapper.DataErrorToForecastErrorMapper
import io.github.vladchenko.weatherforecast.core.data.model.DataResult
import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.domain.model.TemperatureUnit
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import io.github.vladchenko.weatherforecast.data.repository.CurrentWeatherRepository
import io.github.vladchenko.weatherforecast.feature.currentweather.data.datasource.local.CurrentWeatherLocalDataSource
import io.github.vladchenko.weatherforecast.feature.currentweather.data.datasource.remote.CurrentWeatherRemoteDataSource
import io.github.vladchenko.weatherforecast.feature.currentweather.data.mapper.CurrentWeatherDtoMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.data.mapper.CurrentWeatherEntityMapper
import io.github.vladchenko.weatherforecast.feature.currentweather.data.model.CurrentWeatherDto
import io.github.vladchenko.weatherforecast.feature.currentweather.data.model.CurrentWeatherEntity
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather
import kotlinx.coroutines.withContext

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
 * - Handles errors uniformly by converting [io.github.vladchenko.weatherforecast.core.data.model.DataError] (data layer) to [ForecastError] (domain layer) using [DataErrorToForecastErrorMapper].
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
 * @property errorMapper Converts data-layer [io.github.vladchenko.weatherforecast.core.data.model.DataError] into domain-level [ForecastError].
 * @property currentWeatherLocalDataSource Data source for persistent storage and retrieval of weather data.
 * @property currentWeatherRemoteDataSource Data source for fetching data from the remote API.
 */
class CurrentWeatherRepositoryImpl(
    private val loggingService: LoggingService,
    private val dtoMapper: CurrentWeatherDtoMapper,
    private val entityMapper: CurrentWeatherEntityMapper,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val errorMapper: DataErrorToForecastErrorMapper,
    private val currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
    private val currentWeatherRemoteDataSource: CurrentWeatherRemoteDataSource
) : CurrentWeatherRepository {

    /**
     * Refreshes weather data for the specified location.
     *
     * Attempts to fetch fresh data from the remote API. On success, saves it to cache.
     * On failure, attempts to load cached data. If cache is absent, returns an error.
     *
     * @param city city name
     * @param latitude geographic latitude of the location
     * @param longitude geographic longitude of the location
     * @param temperatureUnit temperature unit (Celsius/Fahrenheit)
     * @return [LoadResult] with weather data (remote, local, or error)
     */
    override suspend fun refreshWeatherForLocation(
        city: String,
        latitude: Double,
        longitude: Double,
        temperatureUnit: TemperatureUnit
    ): LoadResult<CurrentWeather> =
        withContext(coroutineDispatchers.io) {
            val result =
                currentWeatherRemoteDataSource.loadWeatherForLocation(city, latitude, longitude)

            when (result) {
                is DataResult.Success -> {
                    fetchAndSave(result.data, city, temperatureUnit)
                }

                is DataResult.Error -> {
                    loadCachedWeatherForLocationOrError(
                        city,
                        temperatureUnit,
                        errorMapper.map(result.error)
                    )
                }
            }
        }

    /**
     * Fetches data from DTO, saves it to the database, and converts to domain model.
     *
     * @param dto weather data from the network response
     * @param city city name
     * @param temperatureUnit temperature unit
     * @return [LoadResult.Remote] with domain model or [LoadResult.Error] on failure
     */
    private suspend fun fetchAndSave(
        dto: CurrentWeatherDto,
        city: String,
        temperatureUnit: TemperatureUnit
    ): LoadResult<CurrentWeather> {
        return try {
            val entity = dtoMapper.toEntity(dto, city)
            val domainModel = entityMapper.toDomain(entity, temperatureUnit)
            try {
                saveWeather(entity)
            } catch (e: Exception) {
                loggingService.logError(TAG, "Failed to save weather to cache for city: $city", e)
            }
            LoadResult.Remote(domainModel)
        } catch (ex: Exception) {
            loggingService.logError(TAG, "Failed to map weather data for city: $city", ex)
            LoadResult.Error(
                city = city,
                error = ForecastError.UncategorizedError(cause = ex)
            )
        }
    }

    /**
     * Loads cached weather data or returns an error.
     *
     * @param city city name
     * @param temperatureUnit temperature unit
     * @param remoteError remote request error that will be passed to [LoadResult.Local]
     * @return [LoadResult.Local] with cached data or [LoadResult.Error]
     */
    private suspend fun loadCachedWeatherForLocationOrError(
        city: String,
        temperatureUnit: TemperatureUnit,
        remoteError: ForecastError
    ): LoadResult<CurrentWeather> {
        return try {
            loadCachedWeatherForLocation(city, temperatureUnit, remoteError)
        } catch (ex: Exception) {
            loggingService.logError(TAG, "Failed to load cached weather for city $city: $ex", ex)
            LoadResult.Error(
                city = city,
                error = ForecastError.LocalStorageError
            )
        }
    }

    /**
     * Loads cached weather data from the local source.
     *
     * @param city city name
     * @param temperatureUnit temperature unit
     * @param remoteError remote request error that will be passed to [LoadResult.Local]
     * @return [LoadResult.Local] with cached data, [LoadResult.Error] on cache miss or error
     */
    private suspend fun loadCachedWeatherForLocation(
        city: String,
        temperatureUnit: TemperatureUnit,
        remoteError: ForecastError
    ): LoadResult<CurrentWeather> =
        withContext(coroutineDispatchers.io) {
            try {
                val localModel = currentWeatherLocalDataSource.loadWeather(city)
                val domainModel = entityMapper.toDomain(localModel, temperatureUnit)
                LoadResult.Local(domainModel, remoteError)
            } catch (_: NoSuchDatabaseEntryException) {
                loggingService.logDebugEvent(TAG, "No cached weather data found for city: $city")
                LoadResult.Error(
                    city = city,
                    error = ForecastError.NoDataAvailable
                )
            } catch (ex: Exception) {
                loggingService.logError(
                    TAG,
                    "Failed to load cached weather for city $city: $ex",
                    ex
                )
                LoadResult.Error(
                    city = city,
                    error = ForecastError.LocalStorageError
                )
            }
        }

    /**
     * Saves weather data to the local database.
     *
     * @param response weather entity to save
     */
    private suspend fun saveWeather(response: CurrentWeatherEntity) =
        withContext(coroutineDispatchers.io) {
            currentWeatherLocalDataSource.saveWeather(response)
        }

    companion object {
        private const val TAG = "CurrentWeatherRepository"
    }
}