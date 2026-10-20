package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models

/**
 * Cached AI weather summary retrieved from the local database.
 *
 * @param llm LLM model identifier used to generate the summary.
 * @param summary AI-generated weather forecast text.
 * @param timestamp timestamp when the summary was generated.
 */
data class CachedWeatherSummary(
    val llm: String,
    val summary: String,
    val timestamp: Long
)