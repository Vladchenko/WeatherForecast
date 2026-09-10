package io.github.vladchenko.weatherforecast.feature.currentweather.data.mapper

import io.github.vladchenko.weatherforecast.core.domain.model.TemperatureUnit
import io.github.vladchenko.weatherforecast.core.utils.TemperatureConversionUtils
import io.github.vladchenko.weatherforecast.feature.currentweather.data.model.CurrentWeatherEntity
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.Coordinate
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather
import kotlin.math.roundToInt

/**
 * Mapper class that converts [CurrentWeatherEntity] (database model) into [CurrentWeather] (domain model).
 *
 * Applies temperature unit conversion based on the user's preference ([TemperatureUnit]).
 * Formats raw Kelvin values from the database into Celsius, Fahrenheit, or Kelvin with proper rounding.
 * Includes logic to attach the correct temperature unit symbol (e.g. °C, °F) in the result.
 */
class CurrentWeatherEntityMapper {

    /**
     * Converts a [CurrentWeatherEntity] into a domain-layer [CurrentWeather] object.
     *
     * Uses the provided [temperatureUnit] to determine how to format the temperature:
     * - Converts from Kelvin to target unit
     * - Rounds to nearest integer
     * - Attaches appropriate unit symbol via [getTemperatureSign]
     *
     * @param entity The database entity containing current weather data.
     * @param temperatureUnit The preferred temperature unit (Celsius, Fahrenheit, Kelvin).
     *
     * @return A fully populated [CurrentWeather] object ready for UI presentation.
     */
    fun toDomain(
        entity: CurrentWeatherEntity,
        temperatureUnit: TemperatureUnit
    ): CurrentWeather {
        val formattedTemp = formatTemperature(entity.temperature, temperatureUnit)
        val tempSign = getTemperatureSign(temperatureUnit)

        return CurrentWeather(
            city = entity.city,
            coordinate = Coordinate(entity.latitude, entity.longitude),
            dateTime = entity.dateTime.toString(),
            timezone = entity.timezoneOffset.toString(),
            temperature = formattedTemp,
            iconCode = entity.weatherIcon,
            weatherType = entity.weatherDescription,
            temperatureType = tempSign
        )
    }

    /**
     * Formats a temperature value from Kelvin to the specified unit.
     *
     * @param kelvin Temperature in Kelvin to convert.
     * @param type Target temperature unit.
     * @return Rounded string representation of the temperature in the selected unit.
     */
    private fun formatTemperature(kelvin: Double, type: TemperatureUnit): String {
        return when (type) {
            TemperatureUnit.CELSIUS -> TemperatureConversionUtils.convertKelvinToCelsiusDegrees(
                kelvin
            ).roundToInt().toString()
            TemperatureUnit.FAHRENHEIT -> TemperatureConversionUtils.convertKelvinToFahrenheitDegrees(
                kelvin
            ).roundToInt().toString()
            TemperatureUnit.KELVIN -> kelvin.roundToInt().toString()
        }
    }

    /**
     * Returns the display symbol for the given temperature unit.
     *
     * @param type The temperature unit.
     * @return Corresponding symbol: "°C", "°F", or "K".
     */
    private fun getTemperatureSign(type: TemperatureUnit): String {
        return when (type) {
            TemperatureUnit.CELSIUS -> "°C"
            TemperatureUnit.FAHRENHEIT -> "°F"
            TemperatureUnit.KELVIN -> "K"
        }
    }
}