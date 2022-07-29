package com.example.weatherforecast.presentation

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.fragments.CurrentTimeForecastFragment
import com.example.weatherforecast.presentation.viewmodel.CitiesNamesViewModel
import com.example.weatherforecast.presentation.viewmodel.CitiesNamesViewModelFactory
import com.example.weatherforecast.presentation.viewmodel.WeatherForecastViewModel
import com.example.weatherforecast.presentation.viewmodel.WeatherForecastViewModelFactory
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