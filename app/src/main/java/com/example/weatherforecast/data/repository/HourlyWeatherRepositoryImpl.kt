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
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<HourlyWeatherDomainModel> =
        withContext(coroutineDispatchers.io) {
            try {
                loadHourlyWeatherAndSave(city, temperatureType)
            } catch (ex: Exception) {
                val error = when (ex) {
                    is ForecastError -> ex
                    else -> ForecastError.NetworkError(ex)
                }
                loadLocalHourlyWeatherOrError(city, temperatureType, error)
            }
        }

    @InternalSerializationApi
    private suspend fun loadHourlyWeatherAndSave(
        city: String,
        temperatureType: TemperatureType
    ): LoadResult<HourlyWeatherDomainModel> {
        val response = hourlyWeatherRemoteDataSource.loadHourlyWeatherForCity(city)
        return if (response.isSuccessful) {
            handleSuccessResponse(response, temperatureType, city)
        } else {
            handleApiError(response)
        }
    }

    @InternalSerializationApi
    private suspend fun handleSuccessResponse(
        response: Response<HourlyWeatherResponse>,
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<HourlyWeatherDomainModel> {
        val body = response.body()
            ?: return LoadResult.Error(ForecastError.NetworkError(Exception("Response for city is successful, but its body is empty")))
        val result = modelsConverter.convert(temperatureType, city, response)
        saveWeather(body)
        return LoadResult.Remote(result)
    }

    private fun handleApiError(response: Response<*>): LoadResult<HourlyWeatherDomainModel> {
        val errorMessage = try {
            response.errorBody()?.string()
                ?: "Response for city is not successful and error body is empty"
        } catch (ex: Exception) {
            "Failed to read error body: $ex"
        }

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
    private suspend fun loadLocalHourlyWeatherOrError(
        city: String,
        temperatureType: TemperatureType,
        remoteError: ForecastError
    ): LoadResult<HourlyWeatherDomainModel> {
        return try {
            val datasourceResponse = hourlyWeatherLocalDataSource.getHourlyWeather(city)
            val result = modelsConverter.convert(temperatureType, city, datasourceResponse)
            LoadResult.Local(result, remoteError)
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to load local data: $ex")
            LoadResult.Error(ForecastError.NoInternet)
        }
    }

    @InternalSerializationApi
    override suspend fun refreshWeatherForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyWeatherDomainModel> =
        withContext(coroutineDispatchers.io) {
            try {
                val response =
                    hourlyWeatherRemoteDataSource.loadHourlyWeatherForLocation(latitude, longitude)
                return@withContext if (response.isSuccessful) {
                    val body = response.body()
                        ?: return@withContext LoadResult.Error(
                            ForecastError.NetworkError(
                                Exception(
                                    "Response for location is successful, but its body is empty"
                                )
                            )
                        )
                    val result = modelsConverter.convert(temperatureType, body.city.name, response)
                    saveWeather(body)
                    LoadResult.Remote(result)
                } else {
                    handleApiError(response)
                }
            } catch (ex: Exception) {
                val error = when (ex) {
                    is ForecastError -> ex
                    else -> ForecastError.NetworkError(ex)
                }
                LoadResult.Error(error)
            }
        }

    @InternalSerializationApi
    override suspend fun loadCachedWeather(
        city: String,
        temperatureType: TemperatureType,
        remoteError: ForecastError
    ): LoadResult<HourlyWeatherDomainModel> =
        withContext(coroutineDispatchers.io) {
            val datasourceResponse = hourlyWeatherLocalDataSource.getHourlyWeather(city)
            val result = modelsConverter.convert(temperatureType, city, datasourceResponse)
            LoadResult.Local(result, remoteError)
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