package com.example.weatherforecast.models.data
// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json    = Json(JsonConfiguration.Stable)
// val welcome = json.parse(Welcome.serializer(), jsonString)

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.weatherforecast.data.database.WeatherForecastTypeConverters
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * Weather forecast server response model.
 * This class represents the complete weather forecast data for a city.
 *
 * @property coordinate Geographical coordinates of the city
 * @property weather List of weather conditions
 * @property base Source of the weather data (e.g., "stations")
 * @property main Main weather parameters (temperature, pressure, etc.)
 * @property visibility Visibility range in meters
 * @property wind Wind information
 * @property clouds Cloud coverage information
 * @property dateTime Timestamp of the forecast
 * @property system System information including country and sunrise/sunset times
 * @property timezone Timezone offset in seconds
 * @property id City ID
 * @property city City name
 * @property code Response code
 */
@Serializable
@Entity(tableName = "citiesForecasts")
@TypeConverters(WeatherForecastTypeConverters::class)
data class WeatherForecastResponse(
    @SerializedName("coord")
    val coordinate: Coordinate,
    @SerializedName("weather")
    val weather: List<Weather>,
    @SerializedName("base")
    val base: String,
    @SerializedName("main")
    val main: Main,
    @SerializedName("visibility")
    val visibility: Long,
    @SerializedName("wind")
    val wind: Wind,
    @SerializedName("clouds")
    val clouds: Clouds,
    @SerializedName("dt")
    val dateTime: Long,
    @SerializedName("sys")
    val system: System,
    @SerializedName("timezone")
    val timezone: Long,
    @SerializedName("id")
    val id: Long,
    @PrimaryKey
    @SerializedName("name")
    val city: String,
    @SerializedName("cod")
    val code: Long
) {
    override fun toString(): String {
        return "WeatherForecastResponse(coord=$coordinate, weather=$weather, base='$base', main=$main, visibility=$visibility, wind=$wind, clouds=$clouds, dt=$dateTime, sys=$system, timezone=$timezone, id=$id, name='$city', cod=$code)"
    }
}

/**
 * Geographical coordinates.
 *
 * @property latitude Latitude coordinate
 * @property longitude Longitude coordinate
 */
@Serializable
data class Coordinate(
    @SerializedName("lat")
    val latitude: Double,
    @SerializedName("lon")
    val longitude: Double
) {
    override fun toString(): String {
        return "Coordinate(latitude=$latitude, longitude=$longitude)"
    }
}

/**
 * Cloud coverage information.
 *
 * @property all Cloud coverage percentage
 */
@Serializable
data class Clouds(
    @SerializedName("all")
    val all: Long
) {
    override fun toString(): String {
        return "Clouds(all=$all)"
    }
}

/**
 * Main weather parameters.
 *
 * @property temp Current temperature
 * @property feelsLike "Feels like" temperature
 * @property tempMin Minimum temperature
 * @property tempMax Maximum temperature
 * @property pressure Atmospheric pressure
 * @property humidity Humidity percentage
 */
@Serializable
data class Main(
    @SerializedName("temp")
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    @SerializedName("temp_min")
    val tempMin: Double,
    @SerializedName("temp_max")
    val tempMax: Double,
    @SerializedName("pressure")
    val pressure: Long,
    @SerializedName("humidity")
    val humidity: Long
) {
    override fun toString(): String {
        return "Main(temp=$temp, feelsLike=$feelsLike, tempMin=$tempMin, tempMax=$tempMax, pressure=$pressure, humidity=$humidity)"
    }
}

/**
 * System information.
 *
 * @property type System type
 * @property id System ID
 * @property country Country code
 * @property sunrise Sunrise time
 * @property sunset Sunset time
 */
@Serializable
data class System(
    @SerializedName("type")
    val type: Long,
    @SerializedName("id")
    val id: Long,
    @SerializedName("country")
    val country: String,
    @SerializedName("sunrise")
    val sunrise: Long,
    @SerializedName("sunset")
    val sunset: Long
) {
    override fun toString(): String {
        return "Sys(type=$type, id=$id, country='$country', sunrise=$sunrise, sunset=$sunset)"
    }
}

/**
 * Weather condition information.
 *
 * @property id Weather condition ID
 * @property main Main weather condition
 * @property description Detailed weather description
 * @property icon Weather icon code
 */
@Serializable
data class Weather(
    @SerializedName("id")
    val id: Long,
    @SerializedName("main")
    val main: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("icon")
    val icon: String
) {
    override fun toString(): String {
        return "Weather(id=$id, main='$main', description='$description', icon='$icon')"
    }
}

/**
 * Wind information.
 *
 * @property speed Wind speed
 * @property degrees Wind direction in degrees
 * @property gust Wind gust speed
 */
@Serializable
data class Wind(
    @SerializedName("speed")
    val speed: Double,
    @SerializedName("deg")
    val degrees: Long,
    @SerializedName("gust")
    val gust: Double
) {
    override fun toString(): String {
        return "Wind(speed=$speed, deg=$degrees, gust=$gust)"
    }
}
