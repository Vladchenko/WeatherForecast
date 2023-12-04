package com.example.weatherforecast.domain.city

import android.location.Location
import com.example.weatherforecast.models.domain.CityLocationModel

/**
 * User chosen city interactor.
 *
 * @property chosenCityRepository provides data-layer data.
 */
class ChosenCityInteractor(private val chosenCityRepository: ChosenCityRepository) {

    /**
     * Download model of city chosen by user, consisting its name and [Location]
     */
    suspend fun loadChosenCityModel(): CityLocationModel {
        return chosenCityRepository.loadChosenCity()
    }

    /**
     * Save model of city chosen by user, consisting its name, i.e.[city] and [Location]
     */
    suspend fun saveChosenCity(city: String, location: Location) {
        chosenCityRepository.saveChosenCity(city, location)
    }

    /**
     * Delete a city model from storage.
     */
    suspend fun removeCity() {
        chosenCityRepository.removeCity()
    }
}