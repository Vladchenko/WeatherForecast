package com.example.weatherforecast.data.repository

import android.util.Log
import com.example.weatherforecast.data.mapper.CurrentWeatherDtoMapper
import com.example.weatherforecast.data.mapper.CurrentWeatherEntityMapper
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherLocalDataSource
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.forecast.CurrentWeatherRepository
import com.example.weatherforecast.models.data.DataErrorToForecastErrorMapper
import com.example.weatherforecast.models.data.DataResult
import com.example.weatherforecast.models.data.database.CurrentWeatherEntity
import com.example.weatherforecast.models.data.network.CurrentWeatherDto
import com.example.weatherforecast.models.domain.CurrentWeather
import com.example.weatherforecast.models.domain.ForecastError
import com.example.weatherforecast.models.domain.LoadResult
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi

/**
 * Implementation of [CurrentWeatherRepository] that retrieves and caches current weather data
 * using a combination of remote and local data sources with fallback mechanisms.
 *
 * ## Responsibilities
 * - Fetches real-time current weather data from the remote API by city name or coordinates.
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
 *    - Attempt to load cached data via [loadCachedWeatherForCity], returning [LoadResult.Local] if available.
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
 * @property dtoMapper Mapper for converting [CurrentWeatherDto] to [CurrentWeatherEntity].
 * @property entityMapper Mapper for converting [CurrentWeatherEntity] to [CurrentWeather].
 * @property coroutineDispatchers Dispatcher provider for background execution.
 * @property errorMapper Converts data-layer [DataError] into domain-level [ForecastError].
 * @property currentWeatherLocalDataSource Data source for persistent storage and retrieval of weather data.
 * @property currentWeatherRemoteDataSource Data source for fetching data from the remote API.
 */
@InternalSerializationApi
class CurrentWeatherRepositoryImpl(
    private val dtoMapper: CurrentWeatherDtoMapper,
    private val entityMapper: CurrentWeatherEntityMapper,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val errorMapper: DataErrorToForecastErrorMapper,
    private val currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
    private val currentWeatherRemoteDataSource: CurrentWeatherRemoteDataSource
) : CurrentWeatherRepository {

    @InternalSerializationApi
    override suspend fun refreshWeatherForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<CurrentWeather> =
        withContext(coroutineDispatchers.io) {
            when (val result = currentWeatherRemoteDataSource.loadWeatherForCity(city)) {
                is DataResult.Success -> {
                    val dto = result.data
                    fetchAndSave(dto, temperatureType)
                        ?: LoadResult.Error(ForecastError.NoDataAvailable("Mapped DTO is null"))
                }
                is DataResult.Error -> {
                    val forecastError = errorMapper.map(result.error)
                    loadCachedWeatherForCityOrError(city, temperatureType, forecastError)
                }
            }
        }

    @InternalSerializationApi
    override suspend fun refreshWeatherForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<CurrentWeather> =
        withContext(coroutineDispatchers.io) {
            when (val result = currentWeatherRemoteDataSource.loadWeatherForLocation(latitude, longitude)) {
                is DataResult.Success -> {
                    val dto = result.data
                    fetchAndSave(dto, temperatureType)
                        ?: LoadResult.Error(ForecastError.NoDataAvailable("Mapped DTO is null"))
                }
                is DataResult.Error -> {
                    val forecastError = errorMapper.map(result.error)
                    LoadResult.Error(forecastError)
                }
            }
        }

    @InternalSerializationApi
    private suspend fun fetchAndSave(
        dto: CurrentWeatherDto,
        temperatureType: TemperatureType
    ): LoadResult<CurrentWeather>? {
        return try {
            val entity = dtoMapper.toEntity(dto)
            saveWeather(entity)
            val domainModel = entityMapper.toDomain(entity, temperatureType)
            LoadResult.Remote(domainModel)
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to map or save weather data: $ex")
            null
        }
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
            try {
                val localModel = currentWeatherLocalDataSource.loadWeather(city)
                val domainModel = entityMapper.toDomain(localModel, temperatureType)
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