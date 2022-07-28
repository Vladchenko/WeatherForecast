package com.example.weatherforecast.data.models.domain

/**
 * Data model for cities names retrieval.
 */
data class CitiesNamesDomainModel(
    val cities: List<CityDomainModel>
)

/**
 * Data model for city geo location.
 */
data class CityDomainModel(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
)
