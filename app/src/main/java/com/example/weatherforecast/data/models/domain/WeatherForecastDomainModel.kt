package com.example.weatherforecast.data.models.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * TODO
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "citiesForecasts")
data class WeatherForecastDomainModel(
    @PrimaryKey
    @SerializedName("city")
    val city: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("temperature")
    val temperature: String,
    @SerializedName("weatherType")
    val weatherType: String,
    @SerializedName("temperatureType")
    val temperatureType: String,
    val serverError: String,
)
