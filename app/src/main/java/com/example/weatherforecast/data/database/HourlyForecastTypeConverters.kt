package com.example.weatherforecast.data.database

import androidx.room.TypeConverter
import com.example.weatherforecast.models.data.City
import com.example.weatherforecast.models.data.HourlyForecastItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.InternalSerializationApi

/**
 * Type converters for Room database to handle complex data types.
 */
class HourlyForecastTypeConverters {
    private val gson = Gson()

    @TypeConverter
    @InternalSerializationApi
    fun fromHourlyForecastList(value: List<HourlyForecastItem>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    @InternalSerializationApi
    fun toHourlyForecastList(value: String): List<HourlyForecastItem> {
        val listType = object : TypeToken<List<HourlyForecastItem>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromCity(value: City): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCity(value: String): City {
        return gson.fromJson(value, City::class.java)
    }
} 