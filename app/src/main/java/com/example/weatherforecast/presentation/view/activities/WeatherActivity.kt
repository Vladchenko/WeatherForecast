package com.example.weatherforecast.presentation.view.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkManager
import com.example.weatherforecast.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main activity of the Weather Forecast application.
 *
 * Serves as the entry point for the app UI and hosts navigation components.
 * Uses Hilt for dependency injection, including [WorkManager] for background tasks.
 *
 * The layout is defined in [R.layout.weather_forecast_activity] and typically contains
 * a NavHostFragment to manage screen navigation.
 */
@AndroidEntryPoint
class WeatherActivity : AppCompatActivity() {

    @Inject
    lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_forecast_activity)
    }
}