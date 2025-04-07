package com.example.weatherforecast.data.database

import androidx.room.TypeConverter
import com.example.weatherforecast.models.data.*
import org.json.JSONObject

/**
 * Type converters for Room database to handle complex data types.
 */
class WeatherForecastTypeConverters {
    @TypeConverter
    fun fromCoordinate(source: Coordinate): String {
        return JSONObject().apply {
            put("lat", source.latitude)
            put("lon", source.longitude)
        }.toString()
    }

    @TypeConverter
    fun toCoordinate(source: String): Coordinate {
        val json = JSONObject(source)
        return Coordinate(
            json.getDouble("lat"),
            json.getDouble("lon")
        )
    }

    @TypeConverter
    fun fromWeather(source: List<Weather>): String {
        return JSONObject().apply {
            put("weather", source.map { weather ->
                JSONObject().apply {
                    put("id", weather.id)
                    put("main", weather.main)
                    put("description", weather.description)
                    put("icon", weather.icon)
                }
            })
        }.toString()
    }

    @TypeConverter
    fun toWeather(stringList: String): List<Weather> {
        val json = JSONObject(stringList)
        val weatherArray = json.getJSONArray("weather")
        return List(weatherArray.length()) { index ->
            val weatherJson = weatherArray.getJSONObject(index)
            Weather(
                weatherJson.getLong("id"),
                weatherJson.getString("main"),
                weatherJson.getString("description"),
                weatherJson.getString("icon")
            )
        }
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
            json.getDouble("temp"),
            json.getDouble("feels_like"),
            json.getDouble("temp_min"),
            json.getDouble("temp_max"),
            json.getLong("pressure"),
            json.getLong("humidity")
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
            json.getDouble("speed"),
            json.getLong("deg"),
            json.getDouble("gust")
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
            json.getLong("all")
        )
    }

    @TypeConverter
    fun fromSystem(source: System): String {
        return JSONObject().apply {
            put("type", source.type)
            put("id", source.id)
            put("country", source.country)
            put("sunrise", source.sunrise)
            put("sunset", source.sunset)
        }.toString()
    }

    @TypeConverter
    fun toSystem(source: String): System {
        val json = JSONObject(source)
        return System(
            json.getLong("type"),
            json.getLong("id"),
            json.getString("country"),
            json.getLong("sunrise"),
            json.getLong("sunset")
        )
    }
} 