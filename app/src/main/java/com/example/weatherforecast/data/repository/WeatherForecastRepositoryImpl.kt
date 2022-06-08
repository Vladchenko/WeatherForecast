package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.converter.ResponseToResourceConverter
import com.example.weatherforecast.data.models.WeatherForecastResponse
import com.example.weatherforecast.data.repository.datasourceimpl.WeatherForecastDataSourceImpl
import com.example.weatherforecast.data.util.Resource
import com.example.weatherforecast.domain.WeatherForecastRepository

/**
 * WeatherForecastRepository implementation.
 * Provides data for domain layer.
 *
 * @property weatherForecastDataSourceImpl source of data for domain layer
 * @property ResponseToResourceConverter converts server response to domain entity
 */
class WeatherForecastRepositoryImpl(
    private val weatherForecastDataSourceImpl: WeatherForecastDataSourceImpl,
    private val converter: ResponseToResourceConverter
) : WeatherForecastRepository {
    override suspend fun getWeatherForecastData(city: String): Resource<WeatherForecastResponse> {
        return converter.convert(weatherForecastDataSourceImpl.getWeatherForecastData(city))
    }
}