package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.repository.datasource.ChosenCityDataSource
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityRepository
import com.example.weatherforecast.models.domain.CityLocationModel
import kotlinx.coroutines.withContext

/**
 * [ChosenCityRepository] implementation.
 *
 * @property coroutineDispatchers dispatchers for coroutines
 * @property chosenCityNameDataSource to provide chosen city name, saved earlier
 */
class ChosenCityRepositoryImpl(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val chosenCityNameDataSource: ChosenCityDataSource,
) : ChosenCityRepository {

    override suspend fun loadChosenCity(): CityLocationModel =
        withContext(coroutineDispatchers.io) {
            chosenCityNameDataSource.loadCity()
        }

    override suspend fun saveChosenCity(cityModel: CityLocationModel) =
        withContext(coroutineDispatchers.io) {
            chosenCityNameDataSource.saveCity(cityModel)
        }

    override suspend fun removeCity() =
        withContext(coroutineDispatchers.io) {
            chosenCityNameDataSource.removeCity()
        }
}