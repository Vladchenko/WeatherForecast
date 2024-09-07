package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.api.customexceptions.NoInternetException
import com.example.weatherforecast.data.converter.CitiesNamesDataToDomainConverter
import com.example.weatherforecast.data.repository.datasource.CitiesNamesLocalDataSource
import com.example.weatherforecast.data.repository.datasource.CitiesNamesRemoteDataSource
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.citiesnames.CitiesNamesRepository
import kotlinx.coroutines.withContext
import retrofit2.Response

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
                modelsConverter.convert(
                    Response.success(localDataSource.loadCitiesNames(token)),
                    ex.message.orEmpty()
                )
            }
        }

    override suspend fun deleteAllCitiesNames() =
        withContext(coroutineDispatchers.io) {
            localDataSource.deleteAllCitiesNames()
        }
}