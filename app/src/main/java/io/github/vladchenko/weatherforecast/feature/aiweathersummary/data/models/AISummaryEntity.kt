package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models

import androidx.room.Entity

/**
 * Database entity for storing AI model interaction results.
 * Represents the `llm_interactions` table with a key by city and coordinates.
 *
 * @param llm AI model name.
 * @param city city name.
 * @param latitude latitude.
 * @param longitude longitude.
 * @param request request text sent to the AI model.
 * @param response AI model response (may be `null` on error).
 * @param timestamp request timestamp (default: current time).
 * @param isSuccess flag indicating successful execution (determined by presence of response).
 * @param errorMessage error description if the request was unsuccessful.
 */
@Entity(tableName = "llm_interactions", primaryKeys = ["city", "latitude", "longitude"])
data class AISummaryEntity(
    val llm: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val request: String,
    val response: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = response != null,
    val errorMessage: String? = null
)