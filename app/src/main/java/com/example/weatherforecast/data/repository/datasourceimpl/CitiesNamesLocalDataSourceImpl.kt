package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.database.CitiesNamesDAO
import com.example.weatherforecast.data.repository.datasource.CitiesNamesLocalDataSource
import com.example.weatherforecast.models.domain.CityDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * [CitiesNamesLocalDataSource] implementation
 *
 * @property dao Room library data model to access data locally.
 */
class CitiesNamesLocalDataSourceImpl(private val dao: CitiesNamesDAO) : CitiesNamesLocalDataSource {

    override fun loadCitiesNames(token: String): Flow<CityDomainModel> {
        val model = dao.getCitiesNames(token)
        Log.d("CitiesNamesLocalDataSourceImpl", model.toString())
        return model
    }

    override suspend fun deleteAllCitiesNames() {
        dao.deleteAll()
    }
}