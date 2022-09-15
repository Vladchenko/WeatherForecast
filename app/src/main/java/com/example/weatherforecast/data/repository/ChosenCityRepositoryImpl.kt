package com.example.weatherforecast.data.repository

import android.location.Location
import com.example.weatherforecast.data.repository.datasource.ChosenCityDataSource
import com.example.weatherforecast.domain.city.ChosenCityRepository
import com.example.weatherforecast.models.domain.CityLocationModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of ChosenCityRepository
 */
class ChosenCityRepositoryImpl(
    private val chosenCityNameDataSource: ChosenCityDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ChosenCityRepository {

    override suspend fun loadChosenCity(): CityLocationModel = withContext(ioDispatcher) {
        chosenCityNameDataSource.getCity()
    }

    override suspend fun saveChosenCity(city: String, location: Location) {
        chosenCityNameDataSource.saveCity(city, location)
    }

    override suspend fun removeCity() {
        chosenCityNameDataSource.removeCity()
    }
}