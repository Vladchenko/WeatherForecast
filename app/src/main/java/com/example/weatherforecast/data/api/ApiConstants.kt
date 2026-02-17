package com.example.weatherforecast.data.api

import com.example.weatherforecast.data.api.ApiConstants.CURRENT_WEATHER
import com.example.weatherforecast.data.api.ApiConstants.GEO_DIRECT
import com.example.weatherforecast.data.api.ApiConstants.HOURLY_WEATHER


/**
 * Object containing constant API endpoints used throughout the application.
 *
 * These paths are appended to the base URL (defined in [com.example.weatherforecast.BuildConfig.API_BASE_URL])
 * when making network requests via Retrofit.
 *
 * All constants represent relative paths for different OpenWeatherMap API services:
 * - [GEO_DIRECT]: Geocoding API for converting city names to coordinates
 * - [CURRENT_WEATHER]: Current weather data for a specific location
 * - [HOURLY_WEATHER]: 5-day / 3-hour forecast data (used for hourly forecast display)
 */
object ApiConstants {
    /**
     * Endpoint for the OpenWeatherMap Geocoding API (Direct geocoding).
     *
     * Converts a city name into geographic coordinates (latitude and longitude).
     * Used when the user inputs a city name manually.
     *
     * Example: `geo/1.0/direct?q=London&appid={API key}`
     */
    const val GEO_DIRECT = "geo/1.0/direct"

    /**
     * Endpoint for retrieving current weather data for a specific location.
     *
     * Accepts coordinates (lat/lon), city name, or city ID.
     * Returns a JSON object with temperature, humidity, wind speed, and weather condition.
     *
     * Example: `data/2.5/weather?q=London&appid={API key}`
     */
    const val CURRENT_WEATHER = "data/2.5/weather"

    /**
     * Endpoint for retrieving 5-day / 3-hour forecast data.
     *
     * Provides multiple weather snapshots every 3 hours for the next 5 days.
     * Used to populate the hourly forecast section in the UI.
     *
     * Example: `data/2.5/forecast?q=London&appid={API key}`
     */
    const val HOURLY_WEATHER = "data/2.5/forecast"
}