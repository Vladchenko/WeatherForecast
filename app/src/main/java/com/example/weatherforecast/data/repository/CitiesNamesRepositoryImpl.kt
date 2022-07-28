package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.converter.CitiesNamesDataToDomainConverter
import com.example.weatherforecast.data.repository.datasource.CitiesNamesDataSource
import com.example.weatherforecast.domain.citiesnames.CitiesNamesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * CitiesNamesRepository implementation to retrieve cities names.
 */
class CitiesNamesRepositoryImpl(
    private val dataSource: CitiesNamesDataSource,
    private val modelsConverter: CitiesNamesDataToDomainConverter
) : CitiesNamesRepository {

    override suspend fun loadCitiesForTyping(city: String) =
        withContext(Dispatchers.IO) {
            modelsConverter.convert(dataSource.getCityNamesForTyping(city))
        }
}