package io.github.vladchenko.weatherforecast.feature.currentweather.data.api

/**
 * Constants for API endpoints used to retrieve current weather data.
 *
 * This object defines the endpoint for the OpenWeather One Call API
 * used to fetch current weather conditions by geographic coordinates or city name.
 */
object CurrentWeatherApiConstants {

    /**
     * Endpoint for retrieving current weather data for a specific location.
     *
     * Accepts coordinates (lat/lon), city name, or city ID.
     * Returns a JSON object with temperature, humidity, wind speed, and weather condition.
     *
     * Example: `data/2.5/weather?q=London&appid={API key}`
     */
    const val CURRENT_WEATHER = "data/2.5/weather"
}