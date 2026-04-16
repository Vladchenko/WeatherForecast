package io.github.vladchenko.weatherforecast.feature.recentcities.domain.model

import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel

/**
 * Immutable list of recently used cities for weather forecast lookup.
 *
 * This class holds a collection of [io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel] instances representing cities the user
 * has previously searched for. The list is typically ordered by recency (most recent first)
 * and displayed in the city selection screen when the search query is empty.
 *
 * Designed to be used as part of state in ViewModels and UI layers, especially with Jetpack Compose,
 * where immutability ensures predictable recomposition.
 *
 * @property cities an immutable list of [io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel] objects, representing recent city entries
 */
data class RecentCities(
    val cities: List<CityDomainModel>
)