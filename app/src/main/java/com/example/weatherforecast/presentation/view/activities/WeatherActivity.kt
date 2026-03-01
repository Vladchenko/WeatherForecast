package com.example.weatherforecast.presentation.view.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkManager
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.presentation.coordinator.NetworkStatusCoordinator
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.utils.ResourceManager
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

    @Inject lateinit var workManager: WorkManager
    @Inject lateinit var connectivityObserver: ConnectivityObserver
    @Inject lateinit var statusRenderer: StatusRenderer
    @Inject lateinit var resourceManager: ResourceManager
    private lateinit var networkCoordinator: NetworkStatusCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkCoordinator = NetworkStatusCoordinator(
            connectivityObserver = connectivityObserver,
            statusRenderer = statusRenderer,
            resourceManager = resourceManager
        )

        lifecycle.addObserver(networkCoordinator)
        setContentView(R.layout.weather_forecast_activity)
    }
}