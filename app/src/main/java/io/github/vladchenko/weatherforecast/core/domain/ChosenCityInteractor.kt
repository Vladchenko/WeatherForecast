package io.github.vladchenko.weatherforecast.core.domain

import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel

/**
 * Interface for managing the user's chosen city.
 *
 * Provides methods to load, save, and remove the city selection,
 * including the city name and geographic coordinates.
 */
interface ChosenCityInteractor {

    /**
     * Fetches the model of the user's chosen city.
     *
     * @return city location model containing name and coordinates.
     */
    suspend fun loadChosenCity(): CityLocationModel

    /**
     * Saves the user's chosen city.
     *
     * @param cityModel city model to save.
     */
    suspend fun saveChosenCity(cityModel: CityLocationModel)

    /**
     * Removes the currently saved city from storage.
     */
    suspend fun removeCity()

}