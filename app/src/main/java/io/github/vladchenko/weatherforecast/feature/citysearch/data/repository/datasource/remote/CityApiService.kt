package io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource.remote

import io.github.vladchenko.weatherforecast.BuildConfig
import io.github.vladchenko.weatherforecast.data.api.ApiConstants.GEO_DIRECT
import io.github.vladchenko.weatherforecast.feature.citysearch.data.model.CitySearchResultDto
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface for city-related API operations.
 */
interface CityApiService {
    /**
     * Search for cities by name.
     *
     * @param cityName Name or partial name of the city to search for
     * @param limit Maximum number of results to return
     * @param apiKey API key for authentication
     * @return Response containing list of matching cities
     */
    @InternalSerializationApi
    @GET(GEO_DIRECT)
    suspend fun searchCities(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 10,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<List<CitySearchResultDto>>
}