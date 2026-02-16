package com.example.weatherforecast.data.workmanager

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Starts the periodic forecast download work
 *
 * @property workManager to provide periodic work
 */
class WorkerStarter @Inject constructor(
    private val workManager: WorkManager
) {
    /**
     * Start the periodic forecast download work
     */
    fun start() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ForecastWorker>(
            30, TimeUnit.MINUTES,
            25, TimeUnit.MINUTES
        )
            .setInitialDelay(5, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            FORECAST_PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    companion object {
        private const val FORECAST_PERIODIC_WORK = "ForecastPeriodicWork"
    }
}