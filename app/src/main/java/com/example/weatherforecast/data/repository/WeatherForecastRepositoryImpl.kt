package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.converter.ForecastDataToDomainModelsConverter
import com.example.weatherforecast.data.repository.datasource.WeatherForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.forecast.WeatherForecastRepository
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import kotlinx.coroutines.withContext

/**
 * WeatherForecastRepository implementation. Provides data for domain layer.
 *
 * @property weatherForecastRemoteDataSource source of remote data for domain layer
 * @property weatherForecastLocalDataSource source of local data for domain layer
 * @property modelsConverter converts server response to domain entity
 * @property dispatchers for coroutines
 */
class WeatherForecastRepositoryImpl(
    private val weatherForecastRemoteDataSource: WeatherForecastRemoteDataSource,
    private val weatherForecastLocalDataSource: WeatherForecastLocalDataSource,
    private val modelsConverter: ForecastDataToDomainModelsConverter,
    private val dispatchers: CoroutineDispatchers
) : WeatherForecastRepository {

    override suspend fun loadForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): Result<WeatherForecastDomainModel> =
        withContext(dispatchers.io) {
            var response: WeatherForecastDomainModel
            var result: Result<WeatherForecastDomainModel>
            try {
                response = modelsConverter.convert(
                    temperatureType,
                    city,
                    weatherForecastRemoteDataSource.loadWeatherForecastDataForCity(city)
                )
                result = Result.success(response)
            } catch (ex: NoInternetException) {
                response = loadLocalForecast(city)
                result = Result.success(response.copy(serverError = ex.message.toString()))
            }
            return@withContext result
        }

    override suspend fun loadRemoteForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ) =
        withContext(dispatchers.io) {
            val model =
                weatherForecastRemoteDataSource.loadWeatherForecastForLocation(latitude, longitude)
            modelsConverter.convert(temperatureType, model.body()!!.name, model)
        }

    override suspend fun loadLocalForecast(city: String) =
        withContext(dispatchers.io) {
            weatherForecastLocalDataSource.loadWeatherForecastData(city)
        }

    override suspend fun saveForecast(model: WeatherForecastDomainModel) =
        withContext(dispatchers.io) {
            weatherForecastLocalDataSource.saveWeatherForecastData(model)
        }
}