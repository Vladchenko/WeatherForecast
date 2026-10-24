package io.github.vladchenko.weatherforecast.feature.aiweathersummary.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.vladchenko.weatherforecast.BuildConfig
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.di.DiConstants.AI_SUMMARY_RETROFIT_NAME
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.data.MarkdownParser
import io.github.vladchenko.weatherforecast.data.database.WeatherForecastDatabase
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.api.AISummaryApiService
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.local.AISummaryDAO
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.local.AISummaryLocalDataSource
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.local.AISummaryLocalDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.remote.AISummaryRemoteDataSource
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.remote.AISummaryRemoteDataSourceImpl
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.mapper.DataErrorToAiSummaryErrorMapper
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.repository.WeatherSummaryRepositoryImpl
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.DefaultWeatherPromptBuilder
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.WeatherPromptBuilder
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.WeatherSummaryInteractor
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.WeatherSummaryRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module for providing dependencies for the AI weather summary feature.
 *
 * Contains providers for:
 * - Room DAO for working with local AI summary database
 * - Retrofit API service for interacting with OpenRouter AI
 * - Local and remote data sources
 * - Repository and interactor for business logic
 * - Prompt builder for forming requests to the AI model
 */
@Module
@InstallIn(SingletonComponent::class)
class AiWeatherSummaryModule {

    @Provides
    @Singleton
    fun provideAiDAO(database: WeatherForecastDatabase): AISummaryDAO {
        return database.getAiDAO()
    }

    @Singleton
    @Provides
    fun provideAiApiService(@Named(AI_SUMMARY_RETROFIT_NAME) retrofit: Retrofit): AISummaryApiService {
        return retrofit.create(AISummaryApiService::class.java)
    }

    @Singleton
    @Provides
    @Named(AI_SUMMARY_RETROFIT_NAME)
    fun provideAiRetrofit(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        val userAgentInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(userAgentInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.OPEN_ROUTER_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

/**
 * Creates local data source for working with AI summary database.
 */
    @Singleton
    @Provides
//    @Named(GEMMA_LOCAL_DATASOURCE_NAME)
    fun provideAiLocalDataSource(dao: AISummaryDAO): AISummaryLocalDataSource {
        return AISummaryLocalDataSourceImpl(dao)
    }

    /**
 * Creates remote data source for working with OpenRouter AI API.
 */
    @Singleton
    @Provides
//    @Named(GEMMA_REMOTE_DATASOURCE_NAME)
    fun provideAiRemoteDataSource(apiService: AISummaryApiService): AISummaryRemoteDataSource {
        return AISummaryRemoteDataSourceImpl(apiService)
    }

    /**
 * Creates prompt builder for forming requests to the AI model.
 * Uses string resources for formatting request sections.
 */
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
    fun provideDataErrorToAiSummaryErrorMapper(): DataErrorToAiSummaryErrorMapper {
        return DataErrorToAiSummaryErrorMapper()
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
        coroutineDispatchers: CoroutineDispatchers,
        remoteDataSource: AISummaryRemoteDataSource,
        errorMapper: DataErrorToAiSummaryErrorMapper
    ): WeatherSummaryRepository {
        return WeatherSummaryRepositoryImpl(
            errorMapper = errorMapper,
            markdownParser = markdownParser,
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            coroutineDispatchers = coroutineDispatchers
        )
    }

/**
 * Creates interactor for AI weather summary business logic.
 * Integrates prompt builder, repository, and weather data sources.
 */
    @Singleton
    @Provides
    fun provideWeatherSummaryInteractor(
        promptBuilder: WeatherPromptBuilder,
        summaryRepository: WeatherSummaryRepository,
        currentWeatherRepository: io.github.vladchenko.weatherforecast.data.repository.CurrentWeatherRepository,
        hourlyWeatherRepository: io.github.vladchenko.weatherforecast.data.repository.HourlyWeatherRepository
    ): WeatherSummaryInteractor {
        return WeatherSummaryInteractor(
            promptBuilder = promptBuilder,
            summaryRepository = summaryRepository,
            hourlyWeatherRepository = hourlyWeatherRepository,
            currentWeatherRepository = currentWeatherRepository
        )
    }

}
