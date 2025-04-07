package com.example.weatherforecast.data.api

import com.example.weatherforecast.BuildConfig
import com.example.weatherforecast.data.api.ApiConstants.GEO_DIRECT
import com.example.weatherforecast.data.api.ApiConstants.HOURLY_FORECAST
import com.example.weatherforecast.data.api.ApiConstants.WEATHER_DATA
import com.example.weatherforecast.data.api.customexceptions.ExceptionsMapper
import com.example.weatherforecast.data.api.customexceptions.WeatherForecastExceptionMapper
import com.example.weatherforecast.models.data.HourlyForecastResponse
import com.example.weatherforecast.models.data.WeatherForecastCityResponse
import com.example.weatherforecast.models.data.WeatherForecastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Base interface for weather forecast API services.
 */
interface WeatherForecastApiService {
    /**
     * Get current weather forecast for a city by name.
     *
     * @param cityName Name of the city
     * @param apiKey API key for authentication
     * @return Response containing weather forecast data
     */
    @ExceptionsMapper(WeatherForecastExceptionMapper::class)
    @GET(WEATHER_DATA)
    suspend fun getWeatherForecast(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<WeatherForecastResponse>

    /**
     * Get hourly forecast for a city by name.
     *
     * @param cityName Name of the city
     * @param apiKey API key for authentication
     * @return Response containing hourly forecast data
     */
    @ExceptionsMapper(WeatherForecastExceptionMapper::class)
    @GET(HOURLY_FORECAST)
    suspend fun getHourlyForecast(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<HourlyForecastResponse>

    @GET("data/2.5/forecast")
    suspend fun getHourlyForecastByLocation(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = BuildConfig.API_KEY,
        @Query("units") units: String = "metric"
    ): Response<HourlyForecastResponse>
}

/**
 * Interface for city-related API operations.
 */
interface CityApiService {
    /**
     * Search for cities by name.
     *
     * @param cityName Name or partial name of the city to search for
     * @param limit Maximum number of results to return
     * @param apiKey API key for authentication
     * @return Response containing list of matching cities
     */
    @ExceptionsMapper(WeatherForecastExceptionMapper::class)
    @GET(GEO_DIRECT)
    suspend fun searchCities(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<List<WeatherForecastCityResponse>>
}