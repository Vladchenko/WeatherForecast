package com.example.weatherforecast.domain.city

import android.location.Location
import com.example.weatherforecast.models.domain.CityLocationModel

/**
 * Retrieve city name and its location that one need to have a forecast for.
 */
interface ChosenCityRepository {

    /**
     * Download model of city, consisting its name and [Location]
     */
    suspend fun loadChosenCity(): CityLocationModel

    /**
     * Save [city] and its [location] to some storage.
     */
    suspend fun saveChosenCity(city: String, location: Location)
}