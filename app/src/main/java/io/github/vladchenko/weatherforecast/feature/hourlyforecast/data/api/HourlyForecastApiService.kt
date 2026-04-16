package io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.api

import io.github.vladchenko.weatherforecast.BuildConfig
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.api.HourlyForecastApiConstants.HOURLY_WEATHER
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.model.HourlyWeatherDto
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API service interface for fetching hourly weather forecast data.
 *
 * This interface defines HTTP endpoints for retrieving hourly weather forecasts
 * using the OpenWeather API. It supports querying by city name or geographic coordinates.
 */
interface HourlyForecastApiService {
    /**
     * Get hourly forecast for a city by name.
     *
     * @param cityName Name of the city
     * @param apiKey API key for authentication
     * @return Response containing hourly forecast data
     */
    @InternalSerializationApi
    @GET(HOURLY_WEATHER)
    suspend fun loadHourlyForecast(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<HourlyWeatherDto>

    @InternalSerializationApi
    @GET(HOURLY_WEATHER)
    suspend fun loadHourlyForecastByLocation(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<HourlyWeatherDto>
}