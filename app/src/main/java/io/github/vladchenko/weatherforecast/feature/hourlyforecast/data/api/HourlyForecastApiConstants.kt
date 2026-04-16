package io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.api


/**
 * Constants for API endpoints used to retrieve hourly weather forecast data.
 *
 * This object defines the endpoint for the OpenWeather Forecast API,
 * which provides weather predictions every 3 hours for up to 5 days.
 */
object HourlyForecastApiConstants {

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