package io.github.vladchenko.weatherforecast.feature.chosencity.domain

import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel

/**
 * Retrieve city name and its location to provide a weather forecast for.
 */
interface ChosenCityRepository {

    /**
     * Download data model for city
     */
    suspend fun loadChosenCity(): CityLocationModel

    /**
     * Save [cityModel] to some storage.
     */
    suspend fun saveChosenCity(cityModel: CityLocationModel)

    /**
     * Remove city from storage.
     */
    suspend fun removeCity()
}