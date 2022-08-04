package com.example.weatherforecast.domain.citiesnames

import com.example.weatherforecast.data.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.data.models.domain.CityDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * Cities names interactor.
 *
 * @property citiesNamesRepository provides data-layer data.
 */
class CitiesNamesInteractor(private val citiesNamesRepository: CitiesNamesRepository) {

    suspend fun loadRemoteCitiesNames(city: String): CitiesNamesDomainModel {
        return citiesNamesRepository.loadRemoteCitiesNames(city)
    }

    fun loadLocalCitiesNames(city: String): Flow<List<CityDomainModel>> {
        return citiesNamesRepository.loadLocalCitiesNames(city)
    }

    suspend fun saveCitiesNames(city: CityDomainModel) {
        citiesNamesRepository.saveCity(city)
    }
}