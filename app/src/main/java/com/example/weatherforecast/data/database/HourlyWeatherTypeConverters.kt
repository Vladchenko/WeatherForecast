package com.example.weatherforecast.data.database

import androidx.room.TypeConverter
import com.example.weatherforecast.models.data.City
import com.example.weatherforecast.models.data.HourlyWeatherItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.InternalSerializationApi

/**
 * Type converters for Room database to handle complex data types.
 */
class HourlyWeatherTypeConverters {
    private val gson = Gson()

    @TypeConverter
    @InternalSerializationApi
    fun fromHourlyWeatherList(value: List<HourlyWeatherItem>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    @InternalSerializationApi
    fun toHourlyWeatherList(value: String): List<HourlyWeatherItem> {
        val listType = object : TypeToken<List<HourlyWeatherItem>>() {}.type
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