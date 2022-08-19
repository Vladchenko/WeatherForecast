package com.example.weatherforecast.models.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

/**
 * Weather forecast response model
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "citiesForecasts")
@TypeConverters(SourceTypeConverter::class)
data class WeatherForecastDomainModel(
    @PrimaryKey
    @SerializedName("city")
    val city: String,
    @SerializedName("coord")
    val coordinate: Coordinate,
    @SerializedName("date")
    val date: String,
    @SerializedName("temperature")
    val temperature: String,
    @SerializedName("weatherType")
    val weatherType: String,
    @SerializedName("temperatureType")
    val temperatureType: String,
    val serverError: String
) {
    override fun toString(): String {
        return "WeatherForecastDomainModel(city='$city', coordinate=$coordinate, date='$date', temperature='$temperature', weatherType='$weatherType', temperatureType='$temperatureType', serverError='$serverError')"
    }
}

@kotlinx.serialization.Serializable
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
}

