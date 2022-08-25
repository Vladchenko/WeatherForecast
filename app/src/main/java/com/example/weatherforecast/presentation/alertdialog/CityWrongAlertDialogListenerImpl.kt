package com.example.weatherforecast.presentation.alertdialog

import android.content.Context
import com.example.weatherforecast.presentation.AlertDialogClickListener
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel

/**
 * Alert dialog that tells user a forecast for city is not available and if user wishes to check
 * another city.
 */
class CityWrongAlertDialogListenerImpl(
    val viewModel: WeatherForecastViewModel,
    val context: Context
) : AlertDialogClickListener {

    override fun onPositiveClick(city: String) {
        viewModel.onGotoCitySelection()
    }

    override fun onNegativeClick() {
        // Not used
    }
}