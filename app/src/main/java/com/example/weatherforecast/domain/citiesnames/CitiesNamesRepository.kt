package com.example.weatherforecast.domain.citiesnames

import com.example.weatherforecast.data.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.data.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.data.util.TemperatureType

/**
 * Retrieve cities names. Provides domain-layer data.
 */
interface CitiesNamesRepository {
    /**
     * Retrieve cities names for [city] token matching
     */
    suspend fun loadCitiesForTyping(city: String): CitiesNamesDomainModel
}