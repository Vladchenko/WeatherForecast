package com.example.weatherforecast.data.database

import androidx.room.TypeConverter
import com.example.weatherforecast.models.data.database.HourlyWeatherItemEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

/**
 * Type converters for Room database to handle serialization and deserialization
 * of complex data types that cannot be stored directly in the database.
 *
 * These converters are used to transform [ImmutableList<HourlyWeatherItemEntity>] into a JSON string
 * when saving to the database, and restore it from a JSON string when reading from the database.
 *
 * The conversion is handled using Gson for JSON serialization/deserialization.
 *
 * This class should be registered at the database, entity, or DAO level using [@TypeConverters]
 * so that Room can automatically use these methods when dealing with [ImmutableList<HourlyWeatherItemEntity>].
 */
class HourlyWeatherTypeConverters {
    private val gson = Gson()

    /**
     * Converts an [ImmutableList] of [HourlyWeatherItemEntity] objects into a JSON string.
     *
     * This method is called by Room before inserting or updating the data in the database.
     *
     * @param items The list of hourly weather items to serialize.
     * @return A JSON string representation of the list.
     *
     * Example output:
     * json * [{"time":"12:00","temperature":20,"condition":"Sunny"},...] *
          */
    @TypeConverter
    fun fromHourlyForecastItems(items: ImmutableList<HourlyWeatherItemEntity>): String {
        return gson.toJson(items)
    }

    /**
     * Converts a JSON string back into an [ImmutableList] of [HourlyWeatherItemEntity] objects.
     *
     * This method is called by Room when querying the data from the database.
     *
     * @param json The JSON string to deserialize.
     * @return An [ImmutableList] of [HourlyWeatherItemEntity] reconstructed from the JSON.
     *
     * Uses Gson with [TypeToken] to correctly infer the generic type during deserialization,
     * then converts the resulting mutable list to an immutable persistent list using [toPersistentList].
     */
    @TypeConverter
    fun toHourlyForecastItems(json: String): ImmutableList<HourlyWeatherItemEntity> {
        val type = object : TypeToken<List<HourlyWeatherItemEntity>>() {}.type
        return gson.fromJson<List<HourlyWeatherItemEntity>>(json, type).toPersistentList()
    }
}