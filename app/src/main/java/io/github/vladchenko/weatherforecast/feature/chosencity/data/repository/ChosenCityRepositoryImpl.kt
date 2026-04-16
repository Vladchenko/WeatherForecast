package io.github.vladchenko.weatherforecast.feature.chosencity.data.repository

import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.feature.chosencity.data.repository.datasource.ChosenCityDataSource
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityRepository
import kotlinx.coroutines.withContext

/**
 * [ChosenCityRepository] implementation.
 *
 * @property coroutineDispatchers dispatchers for coroutines
 * @property chosenCityNameDataSource to provide chosen city name, saved earlier
 */
class ChosenCityRepositoryImpl(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val chosenCityNameDataSource: ChosenCityDataSource,
) : ChosenCityRepository {

    override suspend fun loadChosenCity(): CityLocationModel =
        withContext(coroutineDispatchers.io) {
            chosenCityNameDataSource.loadCity()
        }

    override suspend fun saveChosenCity(cityModel: CityLocationModel) =
        withContext(coroutineDispatchers.io) {
            chosenCityNameDataSource.saveCity(cityModel)
        }

    override suspend fun removeCity() =
        withContext(coroutineDispatchers.io) {
            chosenCityNameDataSource.removeCity()
        }
}