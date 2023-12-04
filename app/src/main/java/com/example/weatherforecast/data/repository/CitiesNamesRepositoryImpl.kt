package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.converter.CitiesNamesDataToDomainConverter
import com.example.weatherforecast.data.repository.datasource.CitiesNamesLocalDataSource
import com.example.weatherforecast.data.repository.datasource.CitiesNamesRemoteDataSource
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.citiesnames.CitiesNamesRepository
import com.example.weatherforecast.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.models.domain.CityDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext

/**
 * CitiesNamesRepository implementation to retrieve cities names.
 *
 * @param coroutineDispatchers dispatchers for coroutines
 * @param localDataSource to download cities names from database
 * @param remoteDataSource to download cities names remotely
 * @param modelsConverter to convert data->domain models
 */
class CitiesNamesRepositoryImpl(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val localDataSource: CitiesNamesLocalDataSource,
    private val remoteDataSource: CitiesNamesRemoteDataSource,
    private val modelsConverter: CitiesNamesDataToDomainConverter,
) : CitiesNamesRepository {

    override suspend fun loadCitiesNames(token: String) =
        withContext(coroutineDispatchers.io) {
            return@withContext try {
                modelsConverter.convert(remoteDataSource.loadCityNames(token), "")
            } catch (ex: NoInternetException) {
                CitiesNamesDomainModel(
                    localDataSource.getCitiesNames(token).toList(),
                    ex.message.orEmpty()
                )
            }
        }

    override fun loadLocalCitiesNames(token: String): Flow<CityDomainModel> =
        localDataSource.getCitiesNames(token)

    override suspend fun deleteAllCitiesNames() {
        withContext(coroutineDispatchers.io) {
            localDataSource.deleteAllCitiesNames()
        }
    }
}