package io.github.vladchenko.weatherforecast.core.di

import javax.inject.Qualifier

/**
 * Qualifier for the Retrofit instance used with OpenWeatherMap API.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WeatherRetrofit

/**
 * Qualifier for the Retrofit instance used with OpenRouter API (AI Weather Summary).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WeatherSummaryRetrofit

/**
 * Qualifier for the Retrofit instance used with Nominatim (OpenStreetMap) API.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NominatimRetrofit
