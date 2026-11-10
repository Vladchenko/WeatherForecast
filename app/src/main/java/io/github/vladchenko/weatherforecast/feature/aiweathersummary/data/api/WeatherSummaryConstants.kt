package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.api

object WeatherSummaryConstants {

    /**
     * Named binding for the Retrofit instance used with the AI Weather Summary API.
     *
     * Used in [WeatherSummaryModule] to provide [WeatherSummaryApiService].
     */
    const val WEATHER_SUMMARY_RETROFIT_NAME = "WeatherSummaryRetrofit"
}