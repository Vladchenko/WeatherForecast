package io.github.vladchenko.weatherforecast.core.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.chosencity.domain.ChosenCityRepository
import io.github.vladchenko.weatherforecast.feature.currentweather.domain.CurrentWeatherRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Worker for systematically loading weather forecast from the network.
 *
 * This worker is triggered periodically via [androidx.work.WorkManager] to update
 * current weather data for the chosen city. Uses the latest temperature unit setting
 * and logs execution time.
 *
 * @property context context for creating the WorkManager worker
 * @property params worker configuration parameters
 * @property loggingService centralized service for structured logging
 * @property preferencesManager manager for retrieving temperature unit
 * @property chosenCityRepository repository for loading the chosen city
 * @property currentWeatherRepository repository for performing forecast loading
 */
@HiltWorker
class WeatherWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val loggingService: LoggingService,
    private val preferencesManager: PreferencesManager,
    private val chosenCityRepository: ChosenCityRepository,
    private val currentWeatherRepository: CurrentWeatherRepository,
) : CoroutineWorker(context, params) {

    /**
     * Main worker logic.
     *
     * Performs loading of up-to-date weather data for the chosen city:
     * 1. Retrieves temperature unit from user settings.
     * 2. Loads the chosen city from the repository.
     * 3. Performs weather refresh via [CurrentWeatherRepository].
     * 4. Logs execution time or error.
     *
     * @return [Result.success] on successful update, [Result.failure] on error.
     */
    override suspend fun doWork(): Result =
        try {
            val tempType = preferencesManager.temperatureUnit.first()
            val coordinate = chosenCityRepository.loadChosenCity().coordinate
            val weatherResponse =
                currentWeatherRepository.refreshWeatherForLocation(
                    city = chosenCityRepository.loadChosenCity().city,
                    temperatureUnit = tempType,
                    latitude = coordinate.latitude,
                    longitude = coordinate.longitude
                )

            val timestamp = SimpleDateFormat(TIMESTAMP_PATTERN, Locale.getDefault()).format(
                Date((weatherResponse as LoadResult.Remote).data.dateTime.toLong() * 1000)
            )

            loggingService.logInfoEvent(TAG, LOG_MESSAGE + timestamp)
            Result.success()

        } catch (e: Exception) {
            loggingService.logError(TAG, "Failed to update weather in background", e)
            Result.failure()
        }

    companion object {
        /**
         * Tag for logging.
         */
        private const val TAG = "WeatherWorker"

        /**
         * Log message prefix with execution time.
         */
        private const val LOG_MESSAGE = "WeatherWorker ran at "

        /**
         * Time format for logging.
         */
        private const val TIMESTAMP_PATTERN = "dd/MM/yyyy HH:mm:ss"
    }
}