package com.example.weatherforecast.presentation

import android.app.Application
import com.example.weatherforecast.data.workmanager.WorkerStarter
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for Hilt implementation
 */
@HiltAndroidApp
class WeatherForecastApp : Application() {

    @Inject lateinit var workerStarter: WorkerStarter

    override fun onCreate() {
        super.onCreate()
        workerStarter.start()
    }
}