package io.github.vladchenko.weatherforecast.data.repository.datasourceimpl

import io.github.vladchenko.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import io.github.vladchenko.weatherforecast.data.database.HourlyWeatherDAO
import io.github.vladchenko.weatherforecast.data.repository.datasource.CurrentWeatherRemoteDataSource
import io.github.vladchenko.weatherforecast.data.repository.datasource.HourlyWeatherLocalDataSource
import io.github.vladchenko.weatherforecast.data.util.LoggingService
import io.github.vladchenko.weatherforecast.models.data.database.HourlyWeatherEntity
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
    override suspend fun loadHourlyWeather(city: String): HourlyWeatherEntity {
        val entry = dao.findHourlyForecast(city) ?: throw NoSuchDatabaseEntryException(city)
        loggingService.logDebugEvent(
            TAG,
            "$city city forecast loaded successfully"
        )
        return entry
    }

    @InternalSerializationApi
    override suspend fun saveHourlyWeather(entity: HourlyWeatherEntity) {
        dao.insertHourlyForecast(entity)
        loggingService.logDebugEvent(
            TAG,
            "${entity.cityName} hourly weather saved successfully"
        )
    }

    companion object {
        const val TAG = "HourlyWeatherLocalDataSourceImpl"
    }
}