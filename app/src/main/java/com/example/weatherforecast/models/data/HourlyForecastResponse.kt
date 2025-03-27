package com.example.weatherforecast.models.data

import com.google.gson.annotations.SerializedName

data class HourlyForecastResponse(
    @SerializedName("list")
    val hourlyForecasts: List<HourlyForecastItem>,
    @SerializedName("city")
    val city: City
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