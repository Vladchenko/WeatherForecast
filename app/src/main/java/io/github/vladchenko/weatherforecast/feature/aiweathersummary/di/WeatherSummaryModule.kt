package io.github.vladchenko.weatherforecast.feature.aiweathersummary.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.data.mapper.MarkdownParser
import io.github.vladchenko.weatherforecast.core.database.WeatherForecastDatabase
import io.github.vladchenko.weatherforecast.core.di.HiltRetrofitBuilder
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.api.WeatherSummaryApiService
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.api.WeatherSummaryConstants.WEATHER_SUMMARY_RETROFIT_NAME
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.local.AISummaryDAO
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.local.AISummaryLocalDataSource
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.local.AISummaryLocalDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.remote.WeatherSummaryRemoteDataSource
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.remote.WeatherSummaryRemoteDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.mapper.DataErrorToSummaryErrorMapper
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.repository.WeatherSummaryRepositoryImpl
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.DefaultWeatherPromptBuilder
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.WeatherPromptBuilder
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.WeatherSummaryInteractor
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.WeatherSummaryRepository
import io.github.vladchenko.weatherforecast.feature.currentweather.domain.CurrentWeatherRepository
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain.HourlyWeatherRepository
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module for providing dependencies for the AI Weather Summary feature.
 *
 * Defines how components such as DAO, data sources, repository, and interactor
 * are created and injected. All bindings are tied to [SingletonComponent],
 * ensuring single instances throughout the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class WeatherSummaryModule {

    @Provides
    @Singleton
    fun provideAiDAO(database: WeatherForecastDatabase): AISummaryDAO {
        return database.getAiDAO()
    }

    @Singleton
    @Provides
    @Named(WEATHER_SUMMARY_RETROFIT_NAME)
    fun provideWeatherSummaryRetrofit(hiltRetrofitBuilder: HiltRetrofitBuilder): Retrofit {
        return hiltRetrofitBuilder.createWeatherSummaryRetrofit()
    }

    @Provides
    fun provideAiApiService(
        @Named(WEATHER_SUMMARY_RETROFIT_NAME) retrofit: Retrofit
    ): WeatherSummaryApiService {
        return retrofit.create(WeatherSummaryApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideAiLocalDataSource(dao: AISummaryDAO): AISummaryLocalDataSource {
        return AISummaryLocalDataSourceImpl(dao)
    }

    @Singleton
    @Provides
    fun provideAiRemoteDataSource(apiService: WeatherSummaryApiService): WeatherSummaryRemoteDataSource {
        return WeatherSummaryRemoteDataSourceImpl(apiService)
    }

    @Singleton
    @Provides
    fun provideWeatherPromptBuilder(@ApplicationContext context: Context): WeatherPromptBuilder {
        return DefaultWeatherPromptBuilder(
            DefaultWeatherPromptBuilder.SectionHeaders(
                systemPrompt = context.getString(R.string.ai_system_prompt),
                currentWeatherTitle = context.getString(R.string.ai_current_weather_title),
                hourlyForecastTitle = context.getString(R.string.ai_hourly_forecast_title),
                dataUnavailable = context.getString(R.string.ai_data_unavailable),
                noDetailedForecast = context.getString(R.string.ai_no_detailed_forecast),
                cityLabel = context.getString(R.string.ai_city_label),
                timeLabel = context.getString(R.string.ai_time_label),
                temperatureLabel = context.getString(R.string.ai_temperature_label),
                conditionsLabel = context.getString(R.string.ai_conditions_label),
                timezoneLabel = context.getString(R.string.ai_timezone_label),
                feelsLikeLabel = context.getString(R.string.ai_feels_like_label),
                humidityLabel = context.getString(R.string.ai_humidity_label),
                windLabel = context.getString(R.string.ai_wind_label),
                forecastSeparator = context.getString(R.string.ai_forecast_separator)
            )
        )
    }

    @Singleton
    @Provides
    fun provideDataErrorToAiSummaryErrorMapper(): DataErrorToSummaryErrorMapper {
        return DataErrorToSummaryErrorMapper()
    }

    @Singleton
    @Provides
    fun provideMarkdownParser(): MarkdownParser {
        return MarkdownParser()
    }

    @Singleton
    @Provides
    fun provideWeatherSummaryRepository(
        markdownParser: MarkdownParser,
        localDataSource: AISummaryLocalDataSource,
        errorMapper: DataErrorToSummaryErrorMapper,
        coroutineDispatchers: CoroutineDispatchers,
        remoteDataSource: WeatherSummaryRemoteDataSource
    ): WeatherSummaryRepository {
        return WeatherSummaryRepositoryImpl(
            errorMapper = errorMapper,
            markdownParser = markdownParser,
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            coroutineDispatchers = coroutineDispatchers
        )
    }

    @Singleton
    @Provides
    fun provideWeatherSummaryInteractor(
        promptBuilder: WeatherPromptBuilder,
        summaryRepository: WeatherSummaryRepository,
        hourlyWeatherRepository: HourlyWeatherRepository,
        currentWeatherRepository: CurrentWeatherRepository
    ): WeatherSummaryInteractor {
        return WeatherSummaryInteractor(
            promptBuilder = promptBuilder,
            summaryRepository = summaryRepository,
            hourlyWeatherRepository = hourlyWeatherRepository,
            currentWeatherRepository = currentWeatherRepository
        )
    }

}
