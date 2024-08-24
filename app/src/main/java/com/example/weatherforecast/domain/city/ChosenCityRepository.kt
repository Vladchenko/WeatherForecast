package com.example.weatherforecast.domain.city

import android.location.Location
import com.example.weatherforecast.models.domain.CityLocationModel

/**
 * Retrieve city name and its location to provide a weather forecast for.
 */
interface ChosenCityRepository {

    /**
     * Download data model for city
     */
    suspend fun loadChosenCity(): CityLocationModel

    /**
     * Save [city] and its [location] to some storage.
     */
    suspend fun saveChosenCity(city: String, location: Location)

    /**
     * Remove city from storage.
     */
    suspend fun removeCity()
}