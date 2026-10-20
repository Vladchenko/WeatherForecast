package io.github.vladchenko.weatherforecast.feature.chosencity.domain

import io.github.vladchenko.weatherforecast.core.domain.ChosenCityInteractor
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService

/**
 * Implementation of [ChosenCityInteractor] for managing the user's chosen city.
 *
 * @param loggingService service for structured logging.
 * @param chosenCityRepository repository for working with chosen city data.
 */
class ChosenCityInteractorImpl(
    private val loggingService: LoggingService,
    private val chosenCityRepository: ChosenCityRepository
) : ChosenCityInteractor {

    /**
     * Fetches the model of the user's chosen city, including name and coordinates.
     */
    override suspend fun loadChosenCity(): CityLocationModel {
        val city = chosenCityRepository.loadChosenCity()
        if (city.city.isBlank()) {
            loggingService.logDebugEvent(TAG, "City is empty")
        } else {
            loggingService.logDebugEvent(TAG, "Loaded city is $city")
        }
        return chosenCityRepository.loadChosenCity()
    }

    /**
     * Saves the user's chosen city model.
     *
     * @param cityModel city model to save.
     */
    override suspend fun saveChosenCity(cityModel: CityLocationModel) {
        chosenCityRepository.saveChosenCity(cityModel)
    }

    /**
     * Removes the city from storage.
     */
    override suspend fun removeCity() {
        chosenCityRepository.removeCity()
    }

    companion object {
        private const val TAG = "ChosenCityInteractor"
    }
}