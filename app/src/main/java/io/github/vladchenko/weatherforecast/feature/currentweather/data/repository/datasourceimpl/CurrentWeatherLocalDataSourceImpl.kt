package io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasourceimpl

import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasource.CurrentWeatherDAO
import io.github.vladchenko.weatherforecast.feature.currentweather.data.model.CurrentWeatherEntity
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasource.CurrentWeatherLocalDataSource
import kotlinx.serialization.InternalSerializationApi
import javax.inject.Inject

/**
 * [io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasource.CurrentWeatherLocalDataSource] implementation.
 *
 * @property dao to access weather forecast data from local database
 * @property loggingService centralized service for structured logging
 */
class CurrentWeatherLocalDataSourceImpl @Inject constructor(
    private val dao: CurrentWeatherDAO,
    private val loggingService: LoggingService
) : CurrentWeatherLocalDataSource {

    @InternalSerializationApi
    override suspend fun loadWeather(city: String): CurrentWeatherEntity {
        val entry = dao.findCityForecast(city) ?: throw NoSuchDatabaseEntryException(city)
        loggingService.logDebugEvent(TAG, "${entry.city} city forecast loaded successfully")
        return entry
    }

    @InternalSerializationApi
    override suspend fun saveWeather(response: CurrentWeatherEntity) {
        dao.insertCityForecast(response)
        loggingService.logDebugEvent(TAG, "${response.city} weather saved successfully")
    }

    companion object {
        private const val TAG = "CurrentWeatherLocalDataSourceImpl"
    }
}