package com.example.weatherforecast.presentation

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModel
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModelFactory
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModelFactory
import com.example.weatherforecast.presentation.viewmodel.network.NetworkConnectionViewModel
import com.example.weatherforecast.presentation.viewmodel.network.NetworkConnectionViewModelFactory
import com.example.weatherforecast.presentation.viewmodel.persistence.PersistenceViewModel
import com.example.weatherforecast.presentation.viewmodel.persistence.PersistenceViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Weather forecast main activity
 */
@AndroidEntryPoint
class WeatherForecastActivity : FragmentActivity() {

    lateinit var citiesNamesViewModel: CitiesNamesViewModel
    lateinit var persistenceViewModel: PersistenceViewModel
    lateinit var forecastViewModel: WeatherForecastViewModel
    lateinit var networkViewModel: NetworkConnectionViewModel

    @Inject
    lateinit var forecastViewModelFactory: WeatherForecastViewModelFactory

    @Inject
    lateinit var persistenceViewModelFactory: PersistenceViewModelFactory

    @Inject
    lateinit var networkViewModelFactory: NetworkConnectionViewModelFactory

    @Inject
    lateinit var citiesNamesViewModelFactory: CitiesNamesViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_forecast_activity)
        forecastViewModel = ViewModelProvider(this, forecastViewModelFactory).get(WeatherForecastViewModel::class.java)
        networkViewModel = ViewModelProvider(this, networkViewModelFactory).get(NetworkConnectionViewModel::class.java)
        persistenceViewModel = ViewModelProvider(this, persistenceViewModelFactory).get(PersistenceViewModel::class.java)
        citiesNamesViewModel = ViewModelProvider(this, citiesNamesViewModelFactory).get(CitiesNamesViewModel::class.java)
    }
}