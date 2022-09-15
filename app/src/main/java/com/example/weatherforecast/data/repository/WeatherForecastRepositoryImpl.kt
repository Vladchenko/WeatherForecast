package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.converter.ForecastDataToDomainModelsConverter
import com.example.weatherforecast.data.repository.datasource.WeatherForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.domain.forecast.WeatherForecastRepository
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WeatherForecastRepository implementation. Provides data for domain layer.
 *
 * @property weatherForecastRemoteDataSource source of remote data for domain layer
 * @property weatherForecastLocalDataSource source of local data for domain layer
 * @property modelsConverter converts server response to domain entity
 */
class WeatherForecastRepositoryImpl(
    private val weatherForecastRemoteDataSource: WeatherForecastRemoteDataSource,
    private val weatherForecastLocalDataSource: WeatherForecastLocalDataSource,
    private val modelsConverter: ForecastDataToDomainModelsConverter,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WeatherForecastRepository {

    override suspend fun loadForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): Result<WeatherForecastDomainModel> =
        withContext(ioDispatcher) {
            var response: WeatherForecastDomainModel
            var result: Result<WeatherForecastDomainModel>
            try {
                response = modelsConverter.convert(
                    temperatureType,
                    city,
                    weatherForecastRemoteDataSource.loadWeatherForecastDataForCity(city)
                )
                result = Result.success(response)
            } catch (ex: Exception) {
                response = weatherForecastLocalDataSource.loadWeatherForecastData(city)
                result = Result.success(response.copy(serverError = ex.message.toString()))
            }
            return@withContext result
        }

    //TODO Might not be needed
    override suspend fun loadForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): Result<WeatherForecastDomainModel> =
        withContext(ioDispatcher) {
//            var response: WeatherForecastDomainModel
            var result: Result<WeatherForecastDomainModel>? = null
//            try {
//                response = modelsConverter.convert(
//                    temperatureType,
//                    city,
//                    weatherForecastRemoteDataSource.loadWeatherForecastForLocation(latitude, longitude)
//                )
//                result = Result.success(response)
//            } catch (ex: Exception) {
//                response = weatherForecastLocalDataSource.loadWeatherForecastData(latitude, longitude)
//                result = Result.success(response)
//            }
            return@withContext result!!
        }

    override suspend fun loadRemoteForecastForCity(temperatureType: TemperatureType, city: String) =
        withContext(ioDispatcher) {
            modelsConverter.convert(
                temperatureType,
                city,
                weatherForecastRemoteDataSource.loadWeatherForecastDataForCity(city)
            )
        }

    override suspend fun loadRemoteForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ) =
        withContext(ioDispatcher) {
            val model =
                weatherForecastRemoteDataSource.loadWeatherForecastForLocation(latitude, longitude)
            modelsConverter.convert(temperatureType, model.body()!!.name, model)
        }

    override suspend fun loadLocalForecast(city: String) =
        withContext(ioDispatcher) {
            weatherForecastLocalDataSource.loadWeatherForecastData(city)
        }

    override suspend fun saveForecast(model: WeatherForecastDomainModel) =
        withContext(ioDispatcher) {
            weatherForecastLocalDataSource.saveWeatherForecastData(model)
        }
}