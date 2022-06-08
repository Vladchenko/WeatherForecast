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
     * Receives weather forecast data for one city
     *
     * @property city to have a weather forecast on
     */
    @GET("data/2.5/weather")
    suspend fun getWeatherForecastResponse(
        @Query("q")
        city:String,
        @Query("appid")
        apiKey: String = BuildConfig.API_KEY
    ): Response<WeatherForecastResponse>
}