package com.example.weatherforecast.data.workmanager

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.weatherforecast.data.converter.ForecastDataToDomainModelsConverter
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.domain.city.ChosenCityRepository
import com.example.weatherforecast.domain.forecast.WeatherForecastRepository
import com.example.weatherforecast.models.domain.LoadResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
 * @property coroutineScope to run coroutines
 * @property temperatureType weather type
 * @property chosenCityRepository to download chosen city
 * @property converter to convert forecast data to domain model
 * @property weatherForecastRepository to perform downloading of forecast
 */
@HiltWorker
class ForecastWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val coroutineScope: CoroutineScope,
    private val temperatureType: TemperatureType,
    private val chosenCityRepository: ChosenCityRepository,
    private val converter: ForecastDataToDomainModelsConverter,
    private val weatherForecastRepository: WeatherForecastRepository,
) : Worker(context, params) {

    override fun doWork(): Result {
        coroutineScope.launch {
            try {
                val city = chosenCityRepository.loadChosenCity().city
                val forecastResponse =
                    weatherForecastRepository.loadAndSaveRemoteForecastForCity(
                        temperatureType,
                        city
                    )
                Log.i(
                    TAG,
                    LOG_MESSAGE +
                            SimpleDateFormat(
                                TIMESTAMP_PATTERN,
                                Locale.getDefault()
                            ).format(Date((forecastResponse as LoadResult.Remote).data.dateTime.toLong() * 1000))
                )
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                Result.failure()
            }
        }
        return Result.success()
    }

    companion object {
        private const val TAG = "ForecastWorker"
        private const val LOG_MESSAGE = "ForecastWorker ran at "
        private const val TIMESTAMP_PATTERN = "dd/MM/yyyy HH:mm:ss"
    }
}