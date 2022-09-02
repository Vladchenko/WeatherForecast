package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.converter.CitiesNamesDataToDomainConverter
import com.example.weatherforecast.data.repository.datasource.CitiesNamesLocalDataSource
import com.example.weatherforecast.data.repository.datasource.CitiesNamesRemoteDataSource
import com.example.weatherforecast.domain.citiesnames.CitiesNamesRepository
import com.example.weatherforecast.models.domain.CityDomainModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * CitiesNamesRepository implementation to retrieve cities names.
 */
class CitiesNamesRepositoryImpl(
    private val localDataSource: CitiesNamesLocalDataSource,
    private val remoteDataSource: CitiesNamesRemoteDataSource,
    private val modelsConverter: CitiesNamesDataToDomainConverter,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CitiesNamesRepository {

    override suspend fun loadRemoteCitiesNames(token: String) =
        withContext(ioDispatcher) {
            modelsConverter.convert(remoteDataSource.getCityNames(token))
        }

    override fun loadLocalCitiesNames(token: String): Flow<CityDomainModel> =
        localDataSource.getCitiesNames(token)

    override suspend fun deleteAllCitiesNames() {
        localDataSource.deleteAllCitiesNames()
    }
}