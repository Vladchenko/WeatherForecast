package com.example.weatherforecast.data.api

import com.example.weatherforecast.BuildConfig
import com.example.weatherforecast.data.api.ApiConstants.CURRENT_WEATHER
import com.example.weatherforecast.data.api.ApiConstants.GEO_DIRECT
import com.example.weatherforecast.data.api.ApiConstants.HOURLY_WEATHER
import com.example.weatherforecast.models.data.network.CitiesSearchResultDto
import com.example.weatherforecast.models.data.network.CurrentWeatherDto
import com.example.weatherforecast.models.data.network.HourlyWeatherDto
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Base interface for weather forecast API services.
 */
interface WeatherApiService {
    /**
     * Get [CurrentWeatherDto] current weather forecast for a city by [cityName],
     * providing [apiKey] for authentication on server.
     */
    @InternalSerializationApi
    @GET(CURRENT_WEATHER)
    suspend fun getCurrentWeatherForCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<CurrentWeatherDto>


    /**
     * Get [CurrentWeatherDto] current weather forecast for a location by [lat] and [lon] coordinate,
     * providing [apiKey] for authentication on server.
     */
    @InternalSerializationApi
    @GET(CURRENT_WEATHER)
    suspend fun getCurrentWeatherForLocation(
        @Query("lat")
        lat: Double,
        @Query("lon")
        lon: Double,
        @Query("appid")
        apiKey: String = BuildConfig.API_KEY
    ): Response<CurrentWeatherDto>

    /**
     * Get hourly forecast for a city by name.
     *
     * @param cityName Name of the city
     * @param apiKey API key for authentication
     * @return Response containing hourly forecast data
     */
    @InternalSerializationApi
    @GET(HOURLY_WEATHER)
    suspend fun getHourlyWeather(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<HourlyWeatherDto>

    @InternalSerializationApi
    @GET(HOURLY_WEATHER)
    suspend fun getHourlyForecastByLocation(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = BuildConfig.API_KEY,
        @Query("units") units: String = "metric"
    ): Response<HourlyWeatherDto>
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
    @InternalSerializationApi
    @GET(GEO_DIRECT)
    suspend fun searchCities(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<List<CitiesSearchResultDto>>
}