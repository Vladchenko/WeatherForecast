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
 * Retrofit api service
 */
interface WeatherForecastApiService {

    /**
     * Receive cities names that match the requested criteria, i.e. [city] string
     */
    @GET(GEO_DIRECT)
    @ExceptionsMapper(value = WeatherForecastExceptionMapper::class)
    suspend fun loadCitiesNames(
        @Query("q") city: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<List<WeatherForecastCityResponse>>

    /**
     * Receive weather forecast data for [city]
     *
     * Please note that built-in API requests by city name, zip-codes and city id will be deprecated soon.
     * (from https://openweathermap.org/current#other)
     */
    @GET(WEATHER_DATA)
    @ExceptionsMapper(value = WeatherForecastExceptionMapper::class)
    suspend fun loadWeatherForecastForCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<WeatherForecastResponse>

    /**
     * Receive weather forecast data for current location [latitude] and [longitude],
     * having an [apiKey] provided.
     */
    @GET(WEATHER_DATA)
    @ExceptionsMapper(value = WeatherForecastExceptionMapper::class)
    suspend fun loadWeatherForecastForLocation(
        @Query("lat")  latitude: Double,
        @Query("lon")  longitude: Double,
        @Query("appid")  apiKey: String = BuildConfig.API_KEY
    ): Response<WeatherForecastResponse>

    /**
     * Receive hourly weather forecast data for [city]
     */
    @GET(HOURLY_FORECAST)
    @ExceptionsMapper(value = WeatherForecastExceptionMapper::class)
    suspend fun loadHourlyForecastForCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<HourlyForecastResponse>

    /**
     * Receive hourly weather forecast data for current location [latitude] and [longitude]
     */
    @GET(HOURLY_FORECAST)
    @ExceptionsMapper(value = WeatherForecastExceptionMapper::class)
    suspend fun loadHourlyForecastForLocation(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<HourlyForecastResponse>
}