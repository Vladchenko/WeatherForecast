package com.example.weatherforecast.presentation

import android.app.Application
import androidx.work.Configuration
import com.example.weatherforecast.data.workmanager.WorkerStarter
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class responsible for initializing the app and starting background workers.
 * Uses Dagger Hilt for dependency injection. The [WorkerStarter] is injected and triggered
 * during app startup to ensure periodic weather data updates via WorkManager.
 */
@HiltAndroidApp
class WeatherForecastApp() : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    @Inject
    lateinit var workerStarter: WorkerStarter

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        workerStarter.start()
    }
}