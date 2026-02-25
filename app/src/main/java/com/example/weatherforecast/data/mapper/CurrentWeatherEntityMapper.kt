package com.example.weatherforecast.data.mapper

import com.example.weatherforecast.data.util.TemperatureConversionUtils.convertKelvinToCelsiusDegrees
import com.example.weatherforecast.data.util.TemperatureConversionUtils.convertKelvinToFahrenheitDegrees
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.data.database.CurrentWeatherEntity
import com.example.weatherforecast.models.domain.Coordinate
import com.example.weatherforecast.models.domain.CurrentWeather
import kotlin.math.roundToInt

/**
 * Mapper class that converts [CurrentWeatherEntity] (database model) into [CurrentWeather] (domain model).
 *
 * Applies temperature unit conversion based on the user's preference ([TemperatureType]).
 * Formats raw Kelvin values from the database into Celsius, Fahrenheit, or Kelvin with proper rounding.
 * Includes logic to attach the correct temperature unit symbol (e.g. °C, °F) in the result.
 *
 * @see toDomain for main transformation function
 * @see formatTemperature for unit-specific formatting
 * @see getTemperatureSign for unit symbol lookup
 */
class CurrentWeatherEntityMapper {

    /**
     * Converts a [CurrentWeatherEntity] into a domain-layer [CurrentWeather] object.
     *
     * Uses the provided [temperatureType] to determine how to format the temperature:
     * - Converts from Kelvin to target unit
     * - Rounds to nearest integer
     * - Attaches appropriate unit symbol via [getTemperatureSign]
     *
     * The [serverError] field is initialized as an empty string, indicating no error at this stage.
     * All other fields are mapped directly or derived from the entity.
     *
     * @param entity The database entity containing current weather data.
     * @param temperatureType The preferred temperature unit (Celsius, Fahrenheit, Kelvin).
     *
     * @return A fully populated [CurrentWeather] object ready for UI presentation.
     */
    fun toDomain(
        entity: CurrentWeatherEntity,
        temperatureType: TemperatureType
    ): CurrentWeather {
        val formattedTemp = formatTemperature(entity.temperature, temperatureType)
        val tempSign = getTemperatureSign(temperatureType)

        return CurrentWeather(
            city = entity.city,
            coordinate = Coordinate(entity.latitude, entity.longitude),
            dateTime = entity.dateTime.toString(),
            temperature = formattedTemp,
            iconCode = entity.weatherIcon,
            weatherType = entity.weatherDescription,
            temperatureType = tempSign,
            serverError = ""
        )
    }

    /**
     * Formats a temperature value from Kelvin to the specified unit.
     *
     * @param kelvin Temperature in Kelvin to convert.
     * @param type Target temperature unit.
     * @return Rounded string representation of the temperature in the selected unit.
     */
    private fun formatTemperature(kelvin: Double, type: TemperatureType): String {
        return when (type) {
            TemperatureType.CELSIUS -> convertKelvinToCelsiusDegrees(kelvin).roundToInt().toString()
            TemperatureType.FAHRENHEIT -> convertKelvinToFahrenheitDegrees(kelvin).roundToInt().toString()
            TemperatureType.KELVIN -> kelvin.roundToInt().toString()
        }
    }

    /**
     * Returns the display symbol for the given temperature unit.
     *
     * @param type The temperature unit.
     * @return Corresponding symbol: "°C", "°F", or "K".
     */
    private fun getTemperatureSign(type: TemperatureType): String {
        return when (type) {
            TemperatureType.CELSIUS -> "°C"
            TemperatureType.FAHRENHEIT -> "°F"
            TemperatureType.KELVIN -> "K"
        }
    }
}