package io.github.vladchenko.weatherforecast.feature.currentweather.data.api

import io.github.vladchenko.weatherforecast.BuildConfig
import io.github.vladchenko.weatherforecast.feature.currentweather.data.api.CurrentWeatherApiConstants.CURRENT_WEATHER
import io.github.vladchenko.weatherforecast.feature.currentweather.data.model.CurrentWeatherDto
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API service interface for fetching current and hourly weather data.
 *
 * This interface defines HTTP endpoints for retrieving current weather conditions
 * and hourly forecasts using the OpenWeather API. Requests can be made by city name
 * or geographic coordinates (latitude and longitude).
 */
interface CurrentWeatherApiService {
    /**
     * Get [io.github.vladchenko.weatherforecast.feature.currentweather.data.model.CurrentWeatherDto] current weather forecast for a city by [cityName],
     * providing [apiKey] for authentication on server.
     */
    @InternalSerializationApi
    @GET(CURRENT_WEATHER)
    suspend fun loadCurrentWeatherForCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<CurrentWeatherDto>


    /**
     * Get [CurrentWeatherDto] current weather forecast for a location by [lat] and [lon] coordinate,
     * providing [apiKey] for authentication on server.
     */
    @InternalSerializationApi
    @GET(CURRENT_WEATHER)
    suspend fun loadCurrentWeatherForLocation(
        @Query("lat")
        lat: Double,
        @Query("lon")
        lon: Double,
        @Query("appid")
        apiKey: String = BuildConfig.API_KEY
    ): Response<CurrentWeatherDto>
}