package com.example.weatherforecast.data.repository.datasource

import android.location.Location
import com.example.weatherforecast.models.domain.CityLocationModel

/**
 * Data source for city retrieval/persistence in storage.
 */
interface ChosenCityDataSource {
    /**
     * Retrieve city model.
     */
    suspend fun loadCity(): CityLocationModel

    /**
     * Save [city] and its [location] to some storage.
     */
    suspend fun saveCity(city: String, location: Location)

    /**
     * Remove city model from storage.
     */
    suspend fun removeCity()
}