package com.example.weatherforecast.models.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Data model for cities names retrieval.
 *
 * @property cities data models list
 * @property error message if cities list failed to be fetched
 */
data class CitiesNamesDomainModel(
    val cities: List<CityDomainModel>,
    val error: String
)

/**
 * Data model for city geo location.
 *
 * @property name city name
 * @property lat latitude for city
 * @property lon longitude for city
 * @property country that city located in
 * @property state that city located in
 * @property serverError message if data retrieval failed
 */
@Entity(tableName = "citiesNames")
data class CityDomainModel(
    @PrimaryKey
    @SerializedName("city")
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null,
    val serverError: String? = null,
)
