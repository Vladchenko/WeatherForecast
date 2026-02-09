package com.example.weatherforecast.presentation.view.fragments.cityselection

import androidx.compose.runtime.Immutable
import com.example.weatherforecast.models.domain.CityDomainModel

/**
 * Sealed class representing user actions performed on the city name autocomplete field.
 *
 * These actions are used to communicate between UI and business logic in a type-safe way.
 * Each action triggers a specific behavior such as updating the input, selecting a city,
 * or clearing data.
 */
sealed class CityMaskAction {
    object OnCityMaskAutoCompleteDone : CityMaskAction()
    object OnCityMaskAutoCompleteClear : CityMaskAction()
    object OnCitiesOptionsClear : CityMaskAction()
    data class OnCityMaskChange(val cityMask: String) : CityMaskAction()
    data class OnCitySelected(val selectedCity: CityDomainModel) : CityMaskAction()
}

/**
 * Data class holding the current state of the city search input.
 *
 * Wraps the [cityMask] string to allow mutable state observation in Compose.
 * Marked with [@Immutable] to indicate it should be treated as immutable for UI stability.
 *
 * @property cityMask The current text input used to filter city suggestions
 */
@Immutable
data class CityItem(var cityMask: String)