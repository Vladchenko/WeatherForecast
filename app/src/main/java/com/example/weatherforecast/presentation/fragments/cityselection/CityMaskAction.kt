package com.example.weatherforecast.presentation.fragments.cityselection

import com.example.weatherforecast.models.domain.CityDomainModel

/**
 * Actions for a text autocomplete city name field.
 */
sealed class CityMaskAction {
    object OnCityMaskAutoCompleteDone : CityMaskAction()
    object OnCityMaskAutoCompleteClear : CityMaskAction()
    object OnCitiesOptionsClear : CityMaskAction()
    data class OnCityMaskChange(val cityMask: String) : CityMaskAction()
    data class OnCitySelected(val selectedCity: CityDomainModel) : CityMaskAction()
}

/**
 * City mask data class.
 *
 * @param cityMask string mask for a cities to choose from.
 */
data class CityItem(var cityMask: String)