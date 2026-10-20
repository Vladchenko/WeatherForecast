package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryEntity
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.CachedWeatherSummary

@Dao
interface AISummaryDAO {

    /**
     * Inserts the AI weather summary entity into the database.
     * The existing record is replaced on conflict.
     *
     * @param summary entity with AI result to save.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherSummary(summary: AISummaryEntity)

    /**
     * Fetches the saved AI response for the specified city.
     * The query searches for a record matching the city name and coordinates exactly.
     *
     * @param city city name.
     * @param latitude latitude.
     * @param longitude longitude.
     * @return AI response text or `null` if record not found.
     */
    @Query("SELECT response FROM llm_interactions WHERE city = :city AND latitude = :latitude AND longitude = :longitude")
    suspend fun fetchWeatherSummary(
        city: String,
        latitude: Double,
        longitude: Double
    ): String?

    /**
     * Fetches the saved AI response, timestamp, and LLM model for the specified city.
     *
     * @param city city name.
     * @param latitude latitude.
     * @param longitude longitude.
     * @return cached summary with response text, timestamp, and LLM model, or `null` if not found.
     */
    @Query(
        "SELECT response AS summary, timestamp, llm FROM llm_interactions " +
                "WHERE city = :city AND latitude = :latitude AND longitude = :longitude"
    )
    suspend fun fetchWeatherSummaryWithTimestamp(
        city: String,
        latitude: Double,
        longitude: Double
    ): CachedWeatherSummary?

    /**
     * Fully saves the AI model interaction for the specified city,
     * including request, response, timestamp, and execution status.
     *
     * @param llm ai model identifier.
     * @param city city name.
     * @param latitude latitude.
     * @param longitude longitude.
     * @param request request text sent to the model.
     * @param response model response (may be `null` on error).
     * @param timestamp request timestamp.
     * @param isSuccess flag indicating successful execution.
     */
    @Query(
        "INSERT OR REPLACE INTO llm_interactions (city, latitude, longitude, request, response, llm, timestamp, isSuccess) " +
                "VALUES (:city, :latitude, :longitude, :request, :response, :llm, :timestamp, :isSuccess)"
    )
    suspend fun saveWeatherSummary(
        llm: String,
        city: String,
        latitude: Double,
        longitude: Double,
        request: String,
        response: String?,
        timestamp: Long,
        isSuccess: Boolean
    )
}

