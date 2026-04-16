package io.github.vladchenko.weatherforecast.data.api

import io.github.vladchenko.weatherforecast.BuildConfig
import io.github.vladchenko.weatherforecast.data.api.ApiConstants.CURRENT_WEATHER
import io.github.vladchenko.weatherforecast.data.api.ApiConstants.GEO_DIRECT
import io.github.vladchenko.weatherforecast.data.api.ApiConstants.HOURLY_WEATHER
import io.github.vladchenko.weatherforecast.feature.citysearch.data.model.CitySearchResultDto
import io.github.vladchenko.weatherforecast.feature.currentweather.data.model.CurrentWeatherDto
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.model.HourlyWeatherDto
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

    /**
     * Get hourly forecast for a city by name.
     *
     * @param cityName Name of the city
     * @param apiKey API key for authentication
     * @return Response containing hourly forecast data
     */
    @InternalSerializationApi
    @GET(HOURLY_WEATHER)
    suspend fun loadHourlyWeather(
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
        @Query("limit") limit: Int = 10,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<List<CitySearchResultDto>>
}