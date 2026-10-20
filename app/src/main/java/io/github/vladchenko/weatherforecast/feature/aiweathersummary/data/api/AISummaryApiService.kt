package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.api

import io.github.vladchenko.weatherforecast.BuildConfig
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryRequest
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming

/**
 * Retrofit API service for interacting with OpenRouter AI API.
 *
 * Handles all HTTP requests for generating AI-powered weather summaries
 * using large language models (Gemma, MiniMax, Qwen, etc.).
 */
interface AISummaryApiService {

    /**
     * Fetches an AI-generated weather summary for the given city.
     *
     * Sends current weather and hourly forecast data to the AI model
     * and receives a practical weather recommendation.
     *
     * @param authorization Bearer token for OpenRouter API authentication
     * @param request AI chat completion request containing messages
     * @return AI response with generated weather summary
     */
    @POST("v1/chat/completions")
    @Headers("Content-Type: application/json")
    suspend fun fetchWeatherSummary(
        @Header("Authorization") authorization: String = "Bearer ${BuildConfig.OPEN_ROUTER_API_KEY}",
        @Body request: AISummaryRequest
    ): retrofit2.Response<AISummaryResponse>

    /**
     * Fetches an AI-generated weather summary using streaming response.
     *
     * Suitable for displaying partial results as they are generated.
     *
     * @param authorization Bearer token for OpenRouter API authentication
     * @param request AI chat completion request containing messages
     * @return Retrofit response with raw response body for streaming parsing
     */
    @POST("v1/chat/completions")
    @Headers("Content-Type: application/json")
    @Streaming
    fun fetchWeatherSummaryStreaming(
        @Header("Authorization") authorization: String = "Bearer ${BuildConfig.OPEN_ROUTER_API_KEY}",
        @Body request: AISummaryRequest
    ): retrofit2.Response<okhttp3.ResponseBody>
}