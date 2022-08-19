package com.example.weatherforecast.data.repository.datasourceimpl

import android.content.SharedPreferences
import android.location.Location
import com.example.weatherforecast.data.repository.datasource.ChosenCityDataSource
import com.example.weatherforecast.models.domain.CityLocationModel

/**
 * Implementation of CityDataSource for local storage.
 */
class ChosenCityLocalDataSourceImpl(private val sharedPreferences: SharedPreferences) : ChosenCityDataSource {

    override suspend fun getCity(): CityLocationModel {
        return CityLocationModel(
            sharedPreferences.getString(SAVED_CITY_ARGUMENT_KEY, "") ?: "",
            getChosenCityLocation()
        )
    }

    override suspend fun saveCity(city: String, location: Location) {
        sharedPreferences.edit().putString(SAVED_CITY_ARGUMENT_KEY, city).apply()
        sharedPreferences.edit().putString(SAVED_CITY_LATITUDE_ARGUMENT_KEY, location.latitude.toString()).apply()
        sharedPreferences.edit().putString(SAVED_CITY_LONGITUDE_ARGUMENT_KEY, location.longitude.toString()).apply()
    }

    private fun getChosenCityLocation(): Location {
        val location = Location("")
        location.latitude =
            (sharedPreferences.getString(SAVED_CITY_LATITUDE_ARGUMENT_KEY, "0d")
                ?.toDouble()
                ?: 0.0)
        location.longitude =
            (sharedPreferences.getString(SAVED_CITY_LONGITUDE_ARGUMENT_KEY, "0d")
                ?.toDouble()
                ?: 0.0)
        return location
    }

    companion object {
        const val SAVED_CITY_ARGUMENT_KEY = "Saved city argument"
        const val SAVED_CITY_LATITUDE_ARGUMENT_KEY = "Saved city latitude argument"
        const val SAVED_CITY_LONGITUDE_ARGUMENT_KEY = "Saved city longitude argument"
    }
}