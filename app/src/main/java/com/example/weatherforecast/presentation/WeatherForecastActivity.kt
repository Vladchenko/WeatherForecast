package com.example.weatherforecast.presentation

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.work.WorkManager
import com.example.weatherforecast.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Weather forecast main activity
 */
@AndroidEntryPoint
class WeatherForecastActivity : FragmentActivity() {

    @Inject
    lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_forecast_activity)
    }
}