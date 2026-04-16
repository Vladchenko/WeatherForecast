package io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasourceimpl

import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource.local.CitySearchDAO
import io.github.vladchenko.weatherforecast.feature.citysearch.data.model.CitySearchEntity
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource.local.CitySearchLocalDataSource
import kotlinx.serialization.InternalSerializationApi
import javax.inject.Inject

/**
 * [CitySearchLocalDataSource] implementation.
 *
 * @property dao Room library data model to access data locally.
 * @property loggingService centralized service for structured logging
 */
@InternalSerializationApi
class CitySearchLocalDataSourceImpl @Inject constructor(
    private val dao: CitySearchDAO,
    private val loggingService: LoggingService
) : CitySearchLocalDataSource {


    override suspend fun loadCitiesNames(token: String): List<CitySearchEntity> {
        val model = dao.findCitiesNames(token)
        loggingService.logDebugEvent(TAG, "Loaded ${model.size} cities for token '$token': $model")
        return model
    }

    override suspend fun saveCitiesNames(citiesNames: List<CitySearchEntity>) {
        dao.insertCitiesNames(citiesNames)
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