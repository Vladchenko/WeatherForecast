package com.example.weatherforecast.data.repository

import android.location.Location
import com.example.weatherforecast.data.repository.datasource.ChosenCityDataSource
import com.example.weatherforecast.domain.city.ChosenCityRepository
import com.example.weatherforecast.models.domain.CityLocationModel

/**
 * Implementation of ChosenCityRepository
 */
class ChosenCityRepositoryImpl(private val chosenCityNameDataSource: ChosenCityDataSource): ChosenCityRepository {

    override suspend fun loadChosenCity(): CityLocationModel {
        return chosenCityNameDataSource.getCity()
    }

    override suspend fun saveChosenCity(city: String, location: Location) {
        chosenCityNameDataSource.saveCity(city, location)
    }

    override suspend fun removeCity() {
        chosenCityNameDataSource.removeCity()
    }
}