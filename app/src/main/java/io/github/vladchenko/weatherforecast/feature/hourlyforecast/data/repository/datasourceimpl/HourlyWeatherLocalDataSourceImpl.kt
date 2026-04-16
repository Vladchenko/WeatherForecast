package io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.repository.datasourceimpl

import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import io.github.vladchenko.weatherforecast.data.database.HourlyWeatherDAO
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.model.HourlyWeatherEntity
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.repository.datasource.HourlyWeatherLocalDataSource
import kotlinx.serialization.InternalSerializationApi

/**
 * [HourlyWeatherRemoteDataSource] implementation.
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