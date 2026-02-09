package com.example.weatherforecast.presentation

import android.app.Application
import com.example.weatherforecast.data.workmanager.WorkerStarter
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class responsible for initializing the app and starting background workers.
 * Uses Dagger Hilt for dependency injection. The [WorkerStarter] is injected and triggered
 * during app startup to ensure periodic weather data updates via WorkManager.
 */
@HiltAndroidApp
class WeatherForecastApp : Application() {

    @Inject lateinit var workerStarter: WorkerStarter

    override fun onCreate() {
        super.onCreate()
        workerStarter.start()
    }
}