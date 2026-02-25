package com.example.weatherforecast.data.repository

import android.util.Log
import com.example.weatherforecast.data.mapper.CurrentWeatherDtoMapper
import com.example.weatherforecast.data.mapper.CurrentWeatherEntityMapper
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherLocalDataSource
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
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
 * WeatherForecastRepository implementation. Provides data for domain layer.
 *
 * @property dtoMapper mapper for DTOs
 * @property entityMapper mapper for entities
 * @property coroutineDispatchers dispatchers for coroutines
 * @property currentWeatherRemoteDataSource source of remote data for domain layer
 * @property currentWeatherLocalDataSource source of local data for domain layer
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
                loadWeatherForCityAndSave(city, temperatureType)
            } catch (ex: Exception) {
                val error = when (ex) {
                    is ForecastError -> ex
                    else -> ForecastError.NetworkError(ex)
                }
                loadCachedWeatherForCityOrError(city, temperatureType, error)
            }
        }

    @InternalSerializationApi
    private suspend fun loadWeatherForCityAndSave(
        city: String,
        temperatureType: TemperatureType
    ): LoadResult<CurrentWeather> {
        val response = currentWeatherRemoteDataSource.loadWeatherForCity(city)
        return if (response.isSuccessful) {
            handleSuccessResponse(response, temperatureType)
        } else {
            handleApiError(response)
        }
    }

    @InternalSerializationApi
    private suspend fun handleSuccessResponse(
        response: Response<CurrentWeatherDto>,
        temperatureType: TemperatureType
    ): LoadResult<CurrentWeather> {
        val body = response.body()
            ?: return LoadResult.Error(
                ForecastError.NetworkError(
                    Exception("Response for city is successful, but its body is empty")
                )
            )
        val dbEntity = dtoMapper.toEntity(body)
        saveWeather(dbEntity)
        val domainModel = entityMapper.toDomain(dbEntity, temperatureType)
        return LoadResult.Remote(domainModel)
    }

    private fun handleApiError(response: Response<*>): LoadResult<CurrentWeather> {
        val errorMessage = try {
            response.errorBody()?.string()
                ?: "Response for city is not successful and error body is empty"
        } catch (ex: Exception) {
            "Failed to read error body: $ex"
        }
        Log.e(TAG, "HTTP ${response.code()}: $errorMessage")

        val error = when (response.code()) {
            404 -> ForecastError.CityNotFound(
                city = extractCityFromUrl(response),
                message = errorMessage
            )

            401 -> ForecastError.ApiKeyInvalid(errorMessage)
            in 500..599 -> ForecastError.ServerError(response.code(), errorMessage)
            else -> ForecastError.NetworkError(Exception("HTTP ${response.code()} $errorMessage"))
        }

        return LoadResult.Error(error)
    }

    private fun extractCityFromUrl(response: Response<*>): String {
        return response.raw().request.url.pathSegments.lastOrNull() ?: "Unknown city"
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
    override suspend fun refreshWeatherForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<CurrentWeather> =
        withContext(coroutineDispatchers.io) {
            try {
                loadWeatherForLocationAndSave(latitude, longitude, temperatureType)
            } catch (ex: Exception) {
                val error = when (ex) {
                    is ForecastError -> ex
                    else -> ForecastError.NetworkError(ex)
                }
                LoadResult.Error(error)
            }
        }

    @InternalSerializationApi
    private suspend fun loadWeatherForLocationAndSave(
        latitude: Double,
        longitude: Double,
        temperatureType: TemperatureType
    ): LoadResult<CurrentWeather> {
        val response = currentWeatherRemoteDataSource.loadWeatherForLocation(latitude, longitude)
        return if (response.isSuccessful) {
            val body = response.body()
                ?: return LoadResult.Error(ForecastError.NetworkError(Exception("Response for location is successful, but its body is empty")))
//            val result = modelsConverter.convert(temperatureType, body.name, response)
//            saveWeather(body)
//            LoadResult.Remote(result)
            val dbEntity = dtoMapper.toEntity(body)
            saveWeather(dbEntity)
            val domainModel = entityMapper.toDomain(dbEntity, temperatureType)
            return LoadResult.Remote(domainModel)
        } else {
            handleApiError(response)
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