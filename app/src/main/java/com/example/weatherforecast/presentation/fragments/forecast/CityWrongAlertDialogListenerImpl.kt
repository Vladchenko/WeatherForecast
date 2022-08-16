package com.example.weatherforecast.presentation.fragments.forecast

import android.content.Context
import com.example.weatherforecast.presentation.AlertDialogClickListener
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel

/**
 * Alert dialog that asks user if the geo located city is a proper one to provide a weather forecast on.
 * If a user agrees to the located city, its forecast is downloaded, otherwise, user is suggested to choose another city.
 */
class CityWrongAlertDialogListenerImpl(
    val viewModel: WeatherForecastViewModel,
    val context: Context
) : AlertDialogClickListener {

    override fun onPositiveClick(locationName: String) {
        viewModel.onGotoCitySelection()
    }

    override fun onNegativeClick() {
        // Not used
    }
}