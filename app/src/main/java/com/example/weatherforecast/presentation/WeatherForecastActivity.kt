package com.example.weatherforecast.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.viewmodel.WeatherForecastViewModel
import com.example.weatherforecast.presentation.viewmodel.WeatherForecastViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Weather forecast main activity
 */
@AndroidEntryPoint
class WeatherForecastActivity : FragmentActivity() {

    lateinit var viewModel: WeatherForecastViewModel

    @Inject
    lateinit var mViewModelFactory: WeatherForecastViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_forecast_activity)
        viewModel = ViewModelProvider(this, mViewModelFactory).get(WeatherForecastViewModel::class.java)
        addCurrentTimeForecastFragment()
    }

    private fun addCurrentTimeForecastFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add<CurrentTimeForecastFragment>(R.id.fragment_container_view)
        }
    }
}