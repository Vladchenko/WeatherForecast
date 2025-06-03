package com.example.weatherforecast.models.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.weatherforecast.data.database.HourlyForecastTypeConverters
import com.google.gson.annotations.SerializedName

@Entity(tableName = "hourlyForecasts")
@TypeConverters(HourlyForecastTypeConverters::class)
data class HourlyForecastResponse(
    @PrimaryKey
    @SerializedName("city")
    val city: City,
    @SerializedName("list")
    val hourlyForecasts: List<HourlyForecastItem>
)

data class HourlyForecastItem(
    @SerializedName("dt")
    val timestamp: Long,
    @SerializedName("main")
    val main: Main,
    @SerializedName("weather")
    val weather: List<Weather>,
    @SerializedName("wind")
    val wind: Wind,
    @SerializedName("dt_txt")
    val dateText: String
)

data class City(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("country")
    val country: String
) 