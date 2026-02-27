package com.example.weatherforecast.data.repository.datasourceimpl

import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.data.database.HourlyWeatherDAO
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherRemoteDataSource
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherLocalDataSource
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.models.data.database.HourlyWeatherEntity
import kotlinx.serialization.InternalSerializationApi

/**
 * [CurrentWeatherRemoteDataSource] implementation.
 *
 * @property dao of Retrofit library to download weather forecast data
 * @property loggingService centralized service for structured logging
 */
@InternalSerializationApi
class HourlyWeatherLocalDataSourceImpl(
    private val dao: HourlyWeatherDAO,
    private val loggingService: LoggingService
) : HourlyWeatherLocalDataSource {

    @InternalSerializationApi
    override suspend fun getHourlyWeather(city: String): HourlyWeatherEntity {
        val entry = dao.getHourlyForecast(city) ?: throw NoSuchDatabaseEntryException(city)
        loggingService.logDebugEvent(
            "HourlyWeatherLocalDataSourceImpl",
            "$city city forecast loaded successfully"
        )
        return entry
    }

    @InternalSerializationApi
    override suspend fun saveHourlyWeather(entity: HourlyWeatherEntity) {
        dao.insertHourlyForecast(entity)
        loggingService.logDebugEvent(
            "HourlyForecastLocalDataSourceImpl",
            "${entity.cityName} hourly weather saved successfully"
        )
    }
}