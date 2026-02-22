package com.example.weatherforecast.data.repository

import android.util.Log
import com.example.weatherforecast.data.converter.HourlyWeatherModelConverter
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherLocalDataSource
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.forecast.HourlyWeatherRepository
import com.example.weatherforecast.models.data.HourlyWeatherResponse
import com.example.weatherforecast.models.domain.ForecastError
import com.example.weatherforecast.models.domain.HourlyWeatherDomainModel
import com.example.weatherforecast.models.domain.LoadResult
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

/**
 * WeatherForecastRepository implementation. Provides data for domain layer.
 *
 * @property hourlyWeatherRemoteDataSource source of remote data for domain layer
 * @property hourlyWeatherLocalDataSource source of local data for domain layer
 * @property modelsConverter converts server response to domain entity
 * @property coroutineDispatchers dispatchers for coroutines
 */
class HourlyWeatherRepositoryImpl(
    private val hourlyWeatherRemoteDataSource: HourlyWeatherRemoteDataSource,
    private val hourlyWeatherLocalDataSource: HourlyWeatherLocalDataSource,
    private val modelsConverter: HourlyWeatherModelConverter,
    private val coroutineDispatchers: CoroutineDispatchers,
) : HourlyWeatherRepository {

    @InternalSerializationApi
    override suspend fun refreshWeatherForCity(
        city: String,
        temperatureType: TemperatureType
    ): LoadResult<HourlyWeatherDomainModel> =
        withContext(coroutineDispatchers.io) {
            safeApiCall(
                apiCall = { hourlyWeatherRemoteDataSource.loadHourlyWeatherForCity(city) },
                city = city,
                temperatureType = temperatureType,
                fallbackError = { code, message ->
                    when (code) {
                        404 -> ForecastError.CityNotFound(city = city, message = message)
                        401 -> ForecastError.ApiKeyInvalid(message)
                        in 500..599 -> ForecastError.ServerError(code, message)
                        else -> ForecastError.NetworkError(Exception("HTTP $code $message"))
                    }
                }
            )
        }

    @InternalSerializationApi
    override suspend fun refreshWeatherForLocation(
        city: String,
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyWeatherDomainModel> =
        withContext(coroutineDispatchers.io) {
            safeApiCall(
                apiCall = {
                    hourlyWeatherRemoteDataSource.loadHourlyWeatherForLocation(
                        latitude,
                        longitude
                    )
                },
                city = city,
                temperatureType = temperatureType,
                fallbackError = { code, message ->
                    when (code) {
                        404 -> ForecastError.CityNotFound(city = city, message = message)
                        401 -> ForecastError.ApiKeyInvalid(message)
                        in 500..599 -> ForecastError.ServerError(code, message)
                        else -> ForecastError.NetworkError(Exception("HTTP $code $message"))
                    }
                }
            )
        }

    @InternalSerializationApi
    override suspend fun loadCachedWeather(
        city: String,
        temperatureType: TemperatureType,
        remoteError: ForecastError
    ): LoadResult<HourlyWeatherDomainModel> =
        withContext(coroutineDispatchers.io) {
            try {
                val response = hourlyWeatherLocalDataSource.getHourlyWeather(city)
                val result = modelsConverter.convert(temperatureType, city, response)
                Log.d(TAG, "Hourly weather loaded from local storage on city: $city")
                LoadResult.Local(result, remoteError)
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to load cached weather: $ex")
                LoadResult.Error(remoteError)
            }
        }

    /**
     * Executes API call safely and tries to load from cache on any error.
     */
    @Suppress("UNCHECKED_CAST")
    @InternalSerializationApi
    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>,
        city: String,
        temperatureType: TemperatureType,
        fallbackError: (Int, String) -> ForecastError
    ): LoadResult<HourlyWeatherDomainModel> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body() ?: return LoadResult.Error(
                    ForecastError.NetworkError(Exception("Response is successful but body is null"))
                )
                val cityName = if (body is HourlyWeatherResponse) body.city.name else city
                val result = modelsConverter.convert(
                    temperatureType,
                    cityName,
                    response as Response<HourlyWeatherResponse>
                )
                saveWeather(body as HourlyWeatherResponse)
                Log.d(TAG, "\"$city\"")
                LoadResult.Remote(result)
            } else {
                val message = try {
                    response.errorBody()?.string() ?: "HTTP ${response.code()} error"
                } catch (ex: Exception) {
                    "Failed to read error body: $ex"
                }
                val error = fallbackError(response.code(), message)
                Log.d(TAG, "\"$city\", error: \"$error")
                loadLocalHourlyWeatherOrError(city, temperatureType, error)
            }
        } catch (ex: Exception) {
            val error = when (ex) {
                is ForecastError -> ex
                else -> ForecastError.NetworkError(ex)
            }
            Log.e(TAG, "\"$city\", error: \"$error")
            loadLocalHourlyWeatherOrError(city, temperatureType, error)
        }
    }

    @InternalSerializationApi
    private suspend fun loadLocalHourlyWeatherOrError(
        city: String,
        temperatureType: TemperatureType,
        remoteError: ForecastError
    ): LoadResult<HourlyWeatherDomainModel> {
        return try {
            val response = hourlyWeatherLocalDataSource.getHourlyWeather(city)
            val result = modelsConverter.convert(temperatureType, city, response)
            Log.d(TAG, "Hourly weather loaded from local storage on city: $city")
            LoadResult.Local(result, remoteError)
        } catch (ex: Throwable) {
            Log.e(TAG, "Hourly weather failed loading from cache: \"$ex\"")
            LoadResult.Error(remoteError)
        }
    }

    @InternalSerializationApi
    private suspend fun saveWeather(response: HourlyWeatherResponse) =
        withContext(coroutineDispatchers.io) {
            hourlyWeatherLocalDataSource.saveHourlyWeather(response)
        }

    companion object {
        private const val TAG = "HourlyWeatherRepository"
    }
}