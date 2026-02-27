package com.example.weatherforecast.data.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherforecast.data.preferences.PreferencesManager
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.domain.city.ChosenCityRepository
import com.example.weatherforecast.domain.forecast.CurrentWeatherRepository
import com.example.weatherforecast.models.domain.LoadResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Worker to systematically download weather forecast from network.
 *
 * This worker runs periodically via [WorkManager] to refresh the current weather data
 * for the chosen city. It uses the latest temperature unit preference and logs execution time.
 *
 * @property context to create a worker for WorkManager
 * @property params adjust the worker
 * @property loggingService centralized service for structured logging
 * @property preferencesManager to provide temperature type
 * @property chosenCityRepository to load the chosen city
 * @property currentWeatherRepository to perform downloading of forecast
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

    override suspend fun doWork(): Result =
        try {
            val tempType = preferencesManager.temperatureTypeStateFlow.first()
            val city = chosenCityRepository.loadChosenCity().city
            val weatherResponse =
                currentWeatherRepository.refreshWeatherForCity(
                    tempType,
                    city
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
        private const val TAG = "WeatherWorker"
        private const val LOG_MESSAGE = "WeatherWorker ran at "
        private const val TIMESTAMP_PATTERN = "dd/MM/yyyy HH:mm:ss"
    }
}