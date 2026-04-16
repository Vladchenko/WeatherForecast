package io.github.vladchenko.weatherforecast.feature.chosencity.data.repository.datasource

import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel

/**
 * Data source for city retrieval/persistence in storage.
 */
interface ChosenCityDataSource {
    /**
     * Retrieve city model.
     */
    suspend fun loadCity(): CityLocationModel

    /**
     * Save [CityLocationModel] to some storage.
     */
    suspend fun saveCity(cityModel: CityLocationModel)

    /**
     * Remove city model from storage.
     */
    suspend fun removeCity()
}