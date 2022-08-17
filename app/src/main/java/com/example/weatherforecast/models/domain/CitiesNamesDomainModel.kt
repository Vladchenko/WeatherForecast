package com.example.weatherforecast.models.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Data model for cities names retrieval.
 */
data class CitiesNamesDomainModel(
    val cities: List<CityDomainModel>
)

/**
 * Data model for city geo location.
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
