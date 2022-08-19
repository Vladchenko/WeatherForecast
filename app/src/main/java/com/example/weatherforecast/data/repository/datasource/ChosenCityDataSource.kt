package com.example.weatherforecast.data.repository.datasource

import android.location.Location
import com.example.weatherforecast.models.domain.CityLocationModel

/**
 * Data source for city retrieval from storage.
 */
interface ChosenCityDataSource {
    /**
     * Retrieve city model.
     */
    suspend fun getCity(): CityLocationModel

    /**
     * Save [city] and its [location] to storage.
     */
    suspend fun saveCity(city: String, location: Location)
}