package com.example.weatherforecast.domain.citiesnames

import com.example.weatherforecast.data.models.domain.CitiesNamesDomainModel

/**
 * Cities names interactor.
 *
 * @property citiesNamesRepository provides data-layer data.
 */
class CitiesNamesInteractor(private val citiesNamesRepository: CitiesNamesRepository) {

    suspend fun loadCitiesNames(city: String): CitiesNamesDomainModel {
        return citiesNamesRepository.loadCitiesForTyping(city)
    }
}