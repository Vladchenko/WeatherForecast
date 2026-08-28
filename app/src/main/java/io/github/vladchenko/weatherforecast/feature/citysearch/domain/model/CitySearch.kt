package io.github.vladchenko.weatherforecast.feature.citysearch.domain.model

import io.github.vladchenko.weatherforecast.core.domain.model.CityModel
import kotlinx.collections.immutable.ImmutableList
import javax.annotation.concurrent.Immutable

/**
 * Data model for cities names retrieval.
 *
 * @property cities data models list
 * @property error message if cities list failed to be fetched
 */
@Immutable
data class CitySearch(
    val cities: ImmutableList<CityModel>,
    val error: String
)
