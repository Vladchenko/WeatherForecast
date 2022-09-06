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

    override suspend fun loadRemoteForecastForCity(temperatureType: TemperatureType, city: String) =
        withContext(ioDispatcher) {
            modelsConverter.convert(
                temperatureType,
                city,
                weatherForecastRemoteDataSource.getWeatherForecastDataForCity(city)
            )
        }

    override suspend fun loadRemoteForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ) =
        withContext(ioDispatcher) {
            val model =
                weatherForecastRemoteDataSource.getWeatherForecastForLocation(latitude, longitude)
            modelsConverter.convert(temperatureType, model.body()!!.name, model)
        }

    override suspend fun loadLocalForecast(city: String) =
        withContext(ioDispatcher) {
            weatherForecastLocalDataSource.loadWeatherForecastData(city)
        }

    override suspend fun saveForecast(model: WeatherForecastDomainModel) =
        weatherForecastLocalDataSource.saveWeatherForecastData(model)
}