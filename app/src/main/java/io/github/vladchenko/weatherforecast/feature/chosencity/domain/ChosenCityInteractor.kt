package io.github.vladchenko.weatherforecast.feature.chosencity.domain

import android.location.Location
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService

/**
 * User chosen city interactor.
 *
 * @property chosenCityRepository provides data-layer data.
 */
class ChosenCityInteractor(
    private val loggingService: LoggingService,
    private val chosenCityRepository: ChosenCityRepository
) {

    /**
     * Download model of city chosen by user, consisting its name and [Location]
     *
     * @return data model for city
     */
    suspend fun loadChosenCity(): CityLocationModel {
        val city = chosenCityRepository.loadChosenCity()
        if (city.city.isBlank()) {
            loggingService.logDebugEvent(TAG, "City is empty")
        } else {
            loggingService.logDebugEvent(TAG, "Loaded city is $city")
        }
        return chosenCityRepository.loadChosenCity()
    }

    /**
     * Save model of city chosen by user, consisting its name, i.e.[cityModel]
     */
    suspend fun saveChosenCity(cityModel: CityLocationModel) {
        chosenCityRepository.saveChosenCity(cityModel)
    }

    /**
     * Delete a city model from storage.
     */
    suspend fun removeCity() {
        chosenCityRepository.removeCity()
    }

    companion object {
        private const val TAG = "ChosenCityInteractor"
    }
}