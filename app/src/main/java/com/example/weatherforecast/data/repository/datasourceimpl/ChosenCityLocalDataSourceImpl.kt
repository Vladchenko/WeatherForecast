package com.example.weatherforecast.data.repository.datasourceimpl

import android.content.SharedPreferences
import android.location.Location
import androidx.core.content.edit
import com.example.weatherforecast.data.repository.datasource.ChosenCityDataSource
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.presentation.viewmodel.geolocation.createLocation

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

    override suspend fun saveCity(cityModel: CityLocationModel) {
        sharedPreferences.edit {
            putString(SAVED_CITY_ARGUMENT_KEY, cityModel.city)
            putString(SAVED_CITY_LATITUDE_ARGUMENT_KEY, cityModel.location.latitude.toString())
            putString(SAVED_CITY_LONGITUDE_ARGUMENT_KEY, cityModel.location.longitude.toString())
        }
    }

    override suspend fun removeCity() {
        sharedPreferences.edit { clear() }
    }

    private fun loadChosenCityLocationLocally(): Location {
        return createLocation(
            sharedPreferences.getString(SAVED_CITY_LATITUDE_ARGUMENT_KEY, "0d")
                ?.toDouble()
                ?: 0.0,
                sharedPreferences.getString(SAVED_CITY_LONGITUDE_ARGUMENT_KEY, "0d")
                ?.toDouble()
                ?: 0.0
        )
    }

    companion object {
        const val SAVED_CITY_ARGUMENT_KEY = "Saved_city_argument"
        const val SAVED_CITY_LATITUDE_ARGUMENT_KEY = "Saved_city_latitude_argument"
        const val SAVED_CITY_LONGITUDE_ARGUMENT_KEY = "Saved_city_longitude_argument"
    }
}