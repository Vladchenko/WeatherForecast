package com.example.weatherforecast.data.mapper

import com.example.weatherforecast.data.util.TemperatureConversionUtils.convertKelvinToCelsiusDegrees
import com.example.weatherforecast.data.util.TemperatureConversionUtils.convertKelvinToFahrenheitDegrees
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.data.database.HourlyWeatherEntity
import com.example.weatherforecast.models.domain.HourlyItemDomainModel
import com.example.weatherforecast.models.domain.HourlyWeatherDomainModel
import kotlinx.collections.immutable.persistentListOf
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Mapper class responsible for converting [HourlyWeatherEntity] (database model) into [HourlyWeatherDomainModel] (domain model).
 *
 * Transforms stored hourly forecast data into a UI-ready format. Applies temperature unit conversion
 * based on user preference ([TemperatureType]) and formats timestamps for display.
 *
 * Uses [SimpleDateFormat] to extract and format the hour and minute from Unix timestamps.
 * Converts temperature from Kelvin to Celsius, Fahrenheit, or keeps in Kelvin with proper rounding.
 * Ensures immutability by using [persistentListOf] for the forecast list.
 *
 * @see toDomain for the main transformation function
 * @see convertTemperature for unit-specific formatting
 * @see getUnitSymbol for temperature unit symbol lookup
 */
class HourlyWeatherEntityMapper {

    private val timeFormat: SimpleDateFormat by lazy {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    /**
     * Converts a [HourlyWeatherEntity] into a domain-layer [HourlyWeatherDomainModel].
     *
     * Maps each [HourlyWeatherEntity.hourlyForecasts] item into a [HourlyItemDomainModel], applying:
     * - Temperature conversion using [convertTemperature]
     * - Timestamp formatting via [timeFormat]
     * - Immutable list creation using [persistentListOf]
     *
     * The resulting [HourlyWeatherDomainModel] contains the city name and a list of formatted hourly items,
     * ready for presentation in the UI.
     *
     * @param entity The database entity containing hourly weather data.
     * @param temperatureType The preferred temperature unit (Celsius, Fahrenheit, Kelvin).
     * @return A fully populated [HourlyWeatherDomainModel] with formatted temperatures and times.
     *
     * @see convertTemperature
     * @see getUnitSymbol
     */
    fun toDomain(
        entity: HourlyWeatherEntity,
        temperatureType: TemperatureType
    ): HourlyWeatherDomainModel {
        val forecasts = entity.hourlyForecasts.map { item ->
            HourlyItemDomainModel(
                timestamp = item.timestamp,
                temperature = convertTemperature(item.temperature, temperatureType),
                feelsLike = convertTemperature(item.feelsLike, temperatureType),
                humidity = item.humidity.toInt(),
                windSpeed = item.windSpeed,
                weatherDescription = item.weatherDescription,
                weatherIcon = item.weatherIcon,
                dateText = timeFormat.format(item.timestamp * 1000L)
            )
        }.let { persistentListOf(*it.toTypedArray()) }

        return HourlyWeatherDomainModel(
            city = entity.cityName,
            hourlyForecasts = forecasts
        )
    }

    /**
     * Converts a temperature value from Kelvin to the specified unit and formats it as a string
     * with the appropriate unit symbol.
     *
     * @param kelvin Temperature in Kelvin to convert.
     * @param temperatureType Target temperature unit.
     * @return Formatted temperature string (e.g., "23°C", "73°F", "296K").
     */
    private fun convertTemperature(kelvin: Double, temperatureType: TemperatureType): String {
        val value = when (temperatureType) {
            TemperatureType.CELSIUS -> convertKelvinToCelsiusDegrees(kelvin).roundToInt()
            TemperatureType.FAHRENHEIT -> convertKelvinToFahrenheitDegrees(kelvin).roundToInt()
            TemperatureType.KELVIN -> kelvin.roundToInt()
        }
        return "$value${getUnitSymbol(temperatureType)}"
    }

    /**
     * Returns the display symbol for the given temperature unit.
     *
     * @param temperatureType The temperature unit.
     * @return Corresponding symbol: "°C", "°F", or "K".
     */
    private fun getUnitSymbol(temperatureType: TemperatureType): String {
        return when (temperatureType) {
            TemperatureType.CELSIUS -> "°C"
            TemperatureType.FAHRENHEIT -> "°F"
            TemperatureType.KELVIN -> "K"
        }
    }
}