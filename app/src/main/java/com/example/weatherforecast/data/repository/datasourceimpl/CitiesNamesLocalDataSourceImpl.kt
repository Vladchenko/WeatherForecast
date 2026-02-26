package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.database.CitiesNamesDAO
import com.example.weatherforecast.data.repository.datasource.CitiesNamesLocalDataSource
import com.example.weatherforecast.models.data.database.CitySearchEntity
import kotlinx.serialization.InternalSerializationApi

/**
 * [CitiesNamesLocalDataSource] implementation
 *
 * @property dao Room library data model to access data locally.
 */
class CitiesNamesLocalDataSourceImpl(private val dao: CitiesNamesDAO) : CitiesNamesLocalDataSource {

    @InternalSerializationApi
    override suspend fun loadCitiesNames(token: String): List<CitySearchEntity> {
        val model = dao.getCitiesNames(token)
        Log.d("CitiesNamesLocalDataSourceImpl", model.toString())
        return model
    }

    @InternalSerializationApi
    override suspend fun deleteAllCitiesNames() {
        dao.deleteAll()
    }
}