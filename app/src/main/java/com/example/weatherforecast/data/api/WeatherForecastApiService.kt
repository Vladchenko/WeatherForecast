package com.example.weatherforecast.data.api

import com.example.weatherforecast.BuildConfig
import com.example.weatherforecast.data.models.data.WeatherForecastCityResponse
import com.example.weatherforecast.data.models.data.WeatherForecastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit api service
 */
interface WeatherForecastApiService {

    /**
     * Receive cities names that match the requested criteria, i.e. [city]
     */
    @GET("geo/1.0/direct")
    suspend fun getCityNamesForTyping(
        @Query("q")
        city:String,
        @Query("limit")
        limit: Int = 5,
        @Query("appid")
        apiKey: String = BuildConfig.API_KEY
    ): Response<List<WeatherForecastCityResponse>>

    /**
     * Receive weather forecast data for one [city]
     *
     * Please note that built-in API requests by city name, zip-codes and city id will be deprecated soon.
     * (from https://openweathermap.org/current#other)
     */
    @GET("data/2.5/weather")
    suspend fun getWeatherForecastResponseForCity(
        @Query("q")
        city:String,
        @Query("appid")
        apiKey: String = BuildConfig.API_KEY
    ): Response<WeatherForecastResponse>

    /**
     * Receive weather forecast data for current [Location]
     */
    @GET("data/2.5/weather")
    suspend fun getWeatherForecastResponseForLocation(
        @Query("lat")
        lat:Double,
        @Query("lon")
        lon:Double,
        @Query("appid")
        apiKey: String = BuildConfig.API_KEY
    ): Response<WeatherForecastResponse>
}