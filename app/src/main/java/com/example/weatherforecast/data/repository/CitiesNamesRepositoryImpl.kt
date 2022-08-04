package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.converter.CitiesNamesDataToDomainConverter
import com.example.weatherforecast.data.models.domain.CityDomainModel
import com.example.weatherforecast.data.repository.datasource.CitiesNamesLocalDataSource
import com.example.weatherforecast.data.repository.datasource.CitiesNamesRemoteDataSource
import com.example.weatherforecast.domain.citiesnames.CitiesNamesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * CitiesNamesRepository implementation to retrieve cities names.
 */
class CitiesNamesRepositoryImpl(
    private val localDataSource: CitiesNamesLocalDataSource,
    private val remoteDataSource: CitiesNamesRemoteDataSource,
    private val modelsConverter: CitiesNamesDataToDomainConverter
) : CitiesNamesRepository {

    override suspend fun loadRemoteCitiesNames(token: String) =
        withContext(Dispatchers.IO) {
            modelsConverter.convert(remoteDataSource.getCityNames(token))
        }

    override suspend fun loadLocalCitiesNames(token: String): Flow<List<CityDomainModel>> =
        withContext(Dispatchers.IO) {
            localDataSource.getCitiesNames(token)
        }

    override suspend fun saveCity(city: CityDomainModel) {
        withContext(Dispatchers.IO) {
            localDataSource.saveCity(city)
        }
    }
}