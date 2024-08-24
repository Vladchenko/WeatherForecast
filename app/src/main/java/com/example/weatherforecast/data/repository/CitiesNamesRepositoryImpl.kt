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
 * @property coroutineDispatchers dispatchers for coroutines
 * @property localDataSource to download cities names from database
 * @property remoteDataSource to download cities names remotely
 * @property modelsConverter to convert data->domain models
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
                modelsConverter.convert(remoteDataSource.loadCitiesNames(token), "")
            } catch (ex: NoInternetException) {
                CitiesNamesDomainModel(
                    localDataSource.loadCitiesNames(token).toList(),
                    ex.message.orEmpty()
                )
            }
        }

    override fun loadLocalCitiesNames(token: String) =
        localDataSource.loadCitiesNames(token)

    override suspend fun deleteAllCitiesNames() =
        withContext(coroutineDispatchers.io) {
            localDataSource.deleteAllCitiesNames()
        }
}