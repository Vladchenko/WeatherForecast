package io.github.vladchenko.weatherforecast.core.di

import io.github.vladchenko.weatherforecast.BuildConfig
import io.github.vladchenko.weatherforecast.core.di.DiConstants.HEADER_USER_AGENT
import io.github.vladchenko.weatherforecast.feature.geolocation.data.api.GeoLocationApiConstants.USER_AGENT
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Factory for creating [Retrofit] instances with shared [OkHttpClient].
 *
 * Provides pre-configured methods for common API endpoints (OpenWeatherMap, OpenRouter)
 * while allowing custom base URLs and interceptors via [create].
 *
 * Each created [Retrofit] instance clones the shared [OkHttpClient] via [OkHttpClient.newBuilder]
 * to avoid mutating the original client and to prevent interceptor duplication.
 *
 * @param okHttpClient shared OkHttp client with common interceptors (e.g., logging)
 */
class HiltRetrofitBuilder(private val okHttpClient: OkHttpClient) {

    fun createWeatherRetrofit(): Retrofit {
        return create(
            baseUrl = BuildConfig.API_BASE_URL,
            interceptors = listOf(
                loggingInterceptor(),
                userAgentInterceptor()
            )
        )
    }

    fun createWeatherSummaryRetrofit(): Retrofit {
        return create(
            baseUrl = BuildConfig.OPEN_ROUTER_API_URL,
            interceptors = listOf(userAgentInterceptor())
        )
    }

    private fun loggingInterceptor(): Interceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
    }

    private fun userAgentInterceptor(): Interceptor {
        return Interceptor { chain ->
            chain.request().newBuilder()
                .header(HEADER_USER_AGENT, USER_AGENT)
                .build()
                .let(chain::proceed)
        }
    }

    fun create(baseUrl: String, interceptors: List<Interceptor>): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient.newBuilder().apply {
                interceptors.forEach { addInterceptor(it) }
            }.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}