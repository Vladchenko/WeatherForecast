package com.example.weatherforecast.data.workmanager

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherforecast.data.converter.CurrentForecastModelConverter
import com.example.weatherforecast.data.preferences.PreferencesManager
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.domain.city.ChosenCityRepository
import com.example.weatherforecast.domain.forecast.WeatherForecastRepository
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.models.domain.WeatherForecast
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Worker to systematically download weather forecast from network
 *
 * @constructor creates an instance with a dependencies provided
 *
 * @param context to create a worker for WorkManager
 * @param params adjust the worker
 * @property preferencesManager to provide temperature type
 * @property chosenCityRepository to download chosen city
 * @property converter to convert forecast data to domain model
 * @property weatherForecastRepository to perform downloading of forecast
 */
@HiltWorker
class ForecastWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val preferencesManager: PreferencesManager,
    private val chosenCityRepository: ChosenCityRepository,
    private val converter: CurrentForecastModelConverter,
    private val weatherForecastRepository: WeatherForecastRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        try {
            var tempType = TemperatureType.CELSIUS
            try {
                tempType = preferencesManager.temperatureType.first()
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
            var city = ""
            try {
                city = chosenCityRepository.loadChosenCity().city
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
            var forecastResponse: LoadResult<WeatherForecast>? = null
            try {
                forecastResponse =
                    weatherForecastRepository.loadAndSaveRemoteForecastForCity(
                        tempType,
                        city
                    )
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
            // Только если это Remote — логируем успешный запуск
            if (forecastResponse is LoadResult.Remote) {
                Log.i(
                    TAG,
                    LOG_MESSAGE + SimpleDateFormat(TIMESTAMP_PATTERN, Locale.getDefault())
                        .format(Date(forecastResponse.data.dateTime.toLong() * 1000))
                )
                Result.success()
            } else {
                Log.w(TAG, "Прогноз не был загружен: $forecastResponse")
                Result.retry() // или success(), в зависимости от логики
            }
        } catch (e: Throwable) {
            Log.e(TAG, "КРИТИЧЕСКАЯ ОШИБКА в doWork()", e)
            Result.failure()
        }


    companion object {
        private const val TAG = "ForecastWorker"
        private const val LOG_MESSAGE = "ForecastWorker ran at "
        private const val TIMESTAMP_PATTERN = "dd/MM/yyyy HH:mm:ss"
    }
}