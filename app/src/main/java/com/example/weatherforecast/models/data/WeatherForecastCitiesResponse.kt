package com.example.weatherforecast.models.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * Data model for city's weather forecast.
 *
 * @property city name to provide weather forecast for
 * @property latitude 1st ordinate to locate city
 * @property longitude 2nd ordinate to locate city
 * @property country that city belongs to
 * @property state that city belongs to
 */
@Serializable
@Entity(tableName = "citiesNames")
data class WeatherForecastCityResponse(
    @PrimaryKey
    @SerializedName("city")
    val city: String,
    @SerializedName("lat")
    val latitude: Double,
    @SerializedName("lon")
    val longitude: Double,
    @SerializedName("country")
    val country: String,
    @SerializedName("state")
    val state: String? = null
)