package com.example.weatherforecast.models.data
// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json    = Json(JsonConfiguration.Stable)
// val welcome = json.parse(Welcome.serializer(), jsonString)

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.JSONObject

/**
 * Weather forecast server response model.
 *
 * @property coordinate of longitude and latitude
 * @property weather some compound model of weather information
 * @property base satellite or stations provide weather data
 * @property main weather forecast information
 * @property visibility range of visibility
 * @property wind wind information
 * @property clouds clouds information
 * @property dateTime timestamp
 * @property system information
 * @property timezone time zone
 * @property id some id
 * @property city name of city
 * @property code of region
 */
@Serializable
@Entity(tableName = "citiesForecasts")
@TypeConverters(SourceTypeConverter::class)
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

@Serializable
data class Coordinate(
    @SerializedName("lat")
    val latitude: Double,
    @SerializedName("lon")
    val longitude: Double,
) {
    override fun toString(): String {
        return "Coordinate(latitude=$latitude, longitude=$longitude)"
    }
}

@Serializable
data class Clouds(
    @SerializedName("all")
    val all: Long
) {
    override fun toString(): String {
        return "Clouds(all=$all)"
    }
}

@Serializable
data class Main(
    @SerializedName("temp")
    val temp: Double,

    @SerialName("feels_like")
    @SerializedName("feels_like")
    val feelsLike: Double,

    @SerialName("temp_min")
    @SerializedName("temp_min")
    val tempMin: Double,

    @SerialName("temp_max")
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

/**
 * Type converters for ROOM database.
 */
class SourceTypeConverter {
    @TypeConverter
    fun fromCoordinate(source: Coordinate): String {
        return JSONObject().apply {
            put("lat", source.latitude)
            put("long", source.longitude)
        }.toString()
    }

    @TypeConverter
    fun toCoordinate(source: String): Coordinate {
        val json = JSONObject(source)
        return Coordinate(
            json.getString("lat").toDouble(),
            json.getString("long").toDouble()
        )
    }

    @TypeConverter
    fun fromWeather(source: List<Weather>): String {
        return JSONObject().apply {
            source.map { source ->
                put("id", source.id)
                put("main", source.main)
                put("description", source.description)
                put("icon", source.icon)
            }
        }.toString()
    }

    @TypeConverter
    fun toWeather(stringList: String): List<Weather> {
        val result = ArrayList<Weather>()
        val split = stringList.replace("[", "").replace("]", "").replace(" ", "").split(",")
        Weather(split[0].toLong(), split[1], split[2], split[3])
        return result
    }

    @TypeConverter
    fun fromMain(source: Main): String {
        return JSONObject().apply {
            put("temp", source.temp)
            put("feels_like", source.feelsLike)
            put("temp_min", source.tempMin)
            put("temp_max", source.tempMax)
            put("pressure", source.pressure)
            put("humidity", source.humidity)
        }.toString()
    }

    @TypeConverter
    fun toMain(source: String): Main {
        val json = JSONObject(source)
        return Main(
            json.getString("temp").toDouble(),
            json.getString("feels_like").toDouble(),
            json.getString("temp_min").toDouble(),
            json.getString("temp_max").toDouble(),
            json.getString("pressure").toLong(),
            json.getString("humidity").toLong(),
        )
    }

    @TypeConverter
    fun fromWind(source: Wind): String {
        return JSONObject().apply {
            put("speed", source.speed)
            put("deg", source.degrees)
            put("gust", source.gust)
        }.toString()
    }

    @TypeConverter
    fun toWind(source: String): Wind {
        val json = JSONObject(source)
        return Wind(
            json.getString("speed").toDouble(),
            json.getString("deg").toLong(),
            json.getString("gust").toDouble()
        )
    }

    @TypeConverter
    fun fromClouds(source: Clouds): String {
        return JSONObject().apply {
            put("all", source.all)
        }.toString()
    }

    @TypeConverter
    fun toClouds(source: String): Clouds {
        val json = JSONObject(source)
        return Clouds(
            json.getString("all").toLong()
        )
    }

    @TypeConverter
    fun fromSys(source: System): String {
        return JSONObject().apply {
            put("type", source.type)
            put("id", source.id)
            put("country", source.country)
            put("sunrise", source.sunrise)
            put("sunset", source.sunset)
        }.toString()
    }

    @TypeConverter
    fun toSys(source: String): System {
        val json = JSONObject(source)
        return System(
            json.getString("type").toLong(),
            json.getString("id").toLong(),
            json.getString("country"),
            json.getString("sunrise").toLong(),
            json.getString("sunset").toLong()
        )
    }
}
