package com.example.weatherforecast.data.repository.datasourceimpl

import com.example.weatherforecast.data.database.CitiesNamesDAO
import com.example.weatherforecast.data.repository.datasource.CitiesNamesLocalDataSource
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.models.data.database.CitySearchEntity
import kotlinx.serialization.InternalSerializationApi
import javax.inject.Inject

/**
 * [CitiesNamesLocalDataSource] implementation.
 *
 * @property dao Room library data model to access data locally.
 * @property loggingService centralized service for structured logging
 */
class CitiesNamesLocalDataSourceImpl @Inject constructor(
    private val dao: CitiesNamesDAO,
    private val loggingService: LoggingService
) : CitiesNamesLocalDataSource {

    @InternalSerializationApi
    override suspend fun loadCitiesNames(token: String): List<CitySearchEntity> {
        val model = dao.getCitiesNames(token)
        loggingService.logDebugEvent(TAG, "Loaded ${model.size} cities for token '$token': $model")
        return model
    }

    @InternalSerializationApi
    override suspend fun deleteAllCitiesNames() {
        dao.deleteAll()
        loggingService.logDebugEvent(TAG, "All city names deleted from local database")
    }

    companion object {
        private const val TAG = "CitiesNamesLocalDataSourceImpl"
    }
}