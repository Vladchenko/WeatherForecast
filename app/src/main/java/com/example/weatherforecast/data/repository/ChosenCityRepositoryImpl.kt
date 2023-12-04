package com.example.weatherforecast.data.repository

import android.location.Location
import com.example.weatherforecast.data.repository.datasource.ChosenCityDataSource
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.domain.city.ChosenCityRepository
import com.example.weatherforecast.models.domain.CityLocationModel
import kotlinx.coroutines.withContext

/**
 * Implementation of ChosenCityRepository
 *
 * @param chosenCityNameDataSource to provide chosen city saved earlier
 * @param coroutineDispatchers dispatchers for coroutines
 */
class ChosenCityRepositoryImpl(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val chosenCityNameDataSource: ChosenCityDataSource,
) : ChosenCityRepository {

    override suspend fun loadChosenCity(): CityLocationModel = withContext(coroutineDispatchers.io) {
        chosenCityNameDataSource.getCity()
    }

    override suspend fun saveChosenCity(city: String, location: Location) {
        withContext(coroutineDispatchers.io) {
            chosenCityNameDataSource.saveCity(city, location)
        }
    }

    override suspend fun removeCity() {
        withContext(coroutineDispatchers.io) {
            chosenCityNameDataSource.removeCity()
        }
    }
}