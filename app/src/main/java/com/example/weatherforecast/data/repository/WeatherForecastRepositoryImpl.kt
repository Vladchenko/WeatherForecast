package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.converter.DataToDomainModelsConverter
import com.example.weatherforecast.data.converter.ResponseToResourceConverter
import com.example.weatherforecast.data.models.WeatherForecastDomainModel
import com.example.weatherforecast.data.repository.datasource.WeatherForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.domain.WeatherForecastRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WeatherForecastRepository implementation.
 * Provides data for domain layer.
 *
 * @property weatherForecastRemoteDataSource source of remote data for domain layer
 * @property ResponseToResourceConverter converts server response to domain entity
 */
class WeatherForecastRepositoryImpl(
    private val weatherForecastRemoteDataSource: WeatherForecastRemoteDataSource,
    private val weatherForecastLocalDataSource: WeatherForecastLocalDataSource,
    private val modelsConverter: DataToDomainModelsConverter
) : WeatherForecastRepository {

    override suspend fun loadRemoteForecastForCity(temperatureType: TemperatureType, city: String) =
        withContext(Dispatchers.IO) {
            modelsConverter.convert(temperatureType, city, weatherForecastRemoteDataSource.getWeatherForecastDataForCity(city))
        }

    override suspend fun loadRemoteForecastForLocation(temperatureType: TemperatureType, latitude: Double, longitude: Double) =
        withContext(Dispatchers.IO) {
            val model = weatherForecastRemoteDataSource.getWeatherForecastForLocation(latitude, longitude)
            modelsConverter.convert(temperatureType, model.body()!!.name, model)
        }

    override suspend fun loadLocalForecast(city: String) =
        withContext(Dispatchers.IO) {
            weatherForecastLocalDataSource.loadWeatherForecastData(city)
        }

    override suspend fun saveForecast(model: WeatherForecastDomainModel) =
        withContext(Dispatchers.IO) {
            weatherForecastLocalDataSource.saveWeatherForecastData(model)
        }
}