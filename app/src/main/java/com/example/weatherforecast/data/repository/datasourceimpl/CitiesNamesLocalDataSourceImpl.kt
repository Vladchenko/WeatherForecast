package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.database.CitiesNamesDAO
import com.example.weatherforecast.data.models.domain.CityDomainModel
import com.example.weatherforecast.data.repository.datasource.CitiesNamesLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

/**
 * Cities names retrieval from a local data source.
 */
class CitiesNamesLocalDataSourceImpl(private val dao: CitiesNamesDAO) : CitiesNamesLocalDataSource {

    override suspend fun getCitiesNames(token: String): Flow<List<CityDomainModel>> {
        val model = dao.getAllCitiesNames() //TODO Replace it with correct method
        Log.i("CitiesNamesLocalDataSourceImpl", model.toString())
        return model
    }

    override suspend fun saveCity(city: CityDomainModel) {
        Log.i("CitiesNamesLocalDataSourceImpl",dao.getAllCitiesNames().collect().toString())
        dao.insertCityName(city)
        Log.i("CitiesNamesLocalDataSourceImpl",dao.getAllCitiesNames().collect().toString())
    }
}