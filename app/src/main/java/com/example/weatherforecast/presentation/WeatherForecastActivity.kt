package com.example.weatherforecast.presentation

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.weatherforecast.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Weather forecast main activity
 */
@AndroidEntryPoint
class WeatherForecastActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_forecast_activity)
    }
}