package io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models

import androidx.compose.runtime.Immutable

/**
 * Data model representing a weather forecast response.
 *
 * This class encapsulates all the information required to display weather details
 * for a specific location, including coordinates, timestamps, and current weather conditions.
 *
 * @property requestedCity the original city input provided by the user (if any).
 *                         Can be null if no original input was provided or available.
 *                         Used to show exactly what the user searched for.
 * @property city the canonical name of the city for which the forecast is generated.
 *                This is typically the resolved or standardized name returned by the weather API
 *                (e.g., "New York, NY" instead of "new york").
 * @property coordinate geographical coordinates (latitude and longitude) of the city.
 * @property dateTime timestamp representing the date and time for which the forecast is valid.
 * @property timezone time zone identifier for the requested location (e.g., "America/New_York").
 * @property temperature formatted string representing the current temperature value (e.g., "20°C").
 * @property iconCode identifier string for the weather icon to be displayed.
 * @property weatherType description of the current weather condition (e.g., "Rain", "Clear Sky").
 * @property temperatureType unit of temperature measurement (e.g., "C", "F", "K").
 */
data class CurrentWeather(
    val requestedCity: City? = null,
    val city: String,
    val coordinate: Coordinate,
    val dateTime: String,
    val timezone: String,
    val temperature: String,
    val iconCode: String,
    val weatherType: String,
    val temperatureType: String,
) {
    override fun toString(): String {
        return "WeatherForecast(city='$city', coordinate=$coordinate, date='$dateTime', temperature='$temperature', weatherType='$weatherType', temperatureType='$temperatureType')"
    }
}

/**
 * Coordinate of a location to provide a weather forecast for
 *
 * @property latitude 1st ordinate to locate city
 * @property longitude 2nd ordinate to locate city
 */
@Immutable
data class Coordinate(
    val latitude: Double,
    val longitude: Double,
) {
    override fun toString(): String {
        return "Coordinate(latitude=$latitude, longitude=$longitude)"
    }
}

@Immutable
data class City(val name: String,
                val latitude: Double,
                val longitude: Double,)
{
    override fun toString(): String {
        return "City(name='$name', latitude=$latitude, longitude=$longitude)"
    }
}
