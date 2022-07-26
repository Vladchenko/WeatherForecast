package com.example.weatherforecast.data.api

import com.example.weatherforecast.BuildConfig
import com.example.weatherforecast.data.models.WeatherForecastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit api service
 */
interface WeatherForecastApiService {

    /**
     * Receives weather forecast data for one [city]
     */
    @GET("data/2.5/weather")
    suspend fun getWeatherForecastResponseForCity(
        @Query("q")
        city:String,
        @Query("appid")
        apiKey: String = BuildConfig.API_KEY
    ): Response<WeatherForecastResponse>

    /**
     * Receives weather forecast data for current [Location]
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