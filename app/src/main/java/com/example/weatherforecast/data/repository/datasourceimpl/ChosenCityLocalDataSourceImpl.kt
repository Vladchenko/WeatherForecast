package com.example.weatherforecast.data.repository.datasourceimpl

import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import com.example.weatherforecast.data.repository.datasource.ChosenCityDataSource
import com.example.weatherforecast.models.domain.CityLocationModel

/**
 * [ChosenCityDataSource] implementation
 *
 * @property sharedPreferences local storage.
 */
class ChosenCityLocalDataSourceImpl(private val sharedPreferences: SharedPreferences) : ChosenCityDataSource {

    override suspend fun loadCity(): CityLocationModel {
        return CityLocationModel(
            sharedPreferences.getString(SAVED_CITY_ARGUMENT_KEY, "").orEmpty(),
            loadChosenCityLocationLocally()
        )
    }

    override suspend fun saveCity(city: String, location: Location) {
        sharedPreferences.edit().apply {
            putString(SAVED_CITY_ARGUMENT_KEY, city)
            putString(SAVED_CITY_LATITUDE_ARGUMENT_KEY, location.latitude.toString())
            putString(SAVED_CITY_LONGITUDE_ARGUMENT_KEY, location.longitude.toString())
        }.apply()
    }

    override suspend fun removeCity() {
        sharedPreferences.edit().clear().apply()
    }

    private fun loadChosenCityLocationLocally(): Location {
        val location = Location(LocationManager.NETWORK_PROVIDER)
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
        const val SAVED_CITY_ARGUMENT_KEY = "Saved_city_argument"
        const val SAVED_CITY_LATITUDE_ARGUMENT_KEY = "Saved_city_latitude_argument"
        const val SAVED_CITY_LONGITUDE_ARGUMENT_KEY = "Saved_city_longitude_argument"
    }
}