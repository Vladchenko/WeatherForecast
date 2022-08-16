package com.example.weatherforecast.presentation

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModel
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModelFactory
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Weather forecast main activity
 */
@AndroidEntryPoint
class WeatherForecastActivity : FragmentActivity() {

    lateinit var forecastViewModel: WeatherForecastViewModel
    lateinit var citiesNamesViewModel: CitiesNamesViewModel

    @Inject
    lateinit var forecastViewModelFactory: WeatherForecastViewModelFactory

    @Inject
    lateinit var citiesNamesViewModelFactory: CitiesNamesViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_forecast_activity)
        forecastViewModel = ViewModelProvider(this, forecastViewModelFactory).get(WeatherForecastViewModel::class.java)
        citiesNamesViewModel = ViewModelProvider(this, citiesNamesViewModelFactory).get(CitiesNamesViewModel::class.java)
    }
}