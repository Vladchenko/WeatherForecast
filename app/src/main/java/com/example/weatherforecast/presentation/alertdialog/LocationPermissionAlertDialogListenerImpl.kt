package com.example.weatherforecast.presentation.alertdialog

import android.content.Context
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.AlertDialogClickListener
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel
import kotlin.system.exitProcess

/**
 * Alert dialog that asks user if the geo located city is a proper one to provide a weather forecast on.
 * If a user agrees to the located city, its forecast is downloaded, otherwise, user is suggested to choose another city.
 *
 * @param viewModel to call specific actions on alert dialog clicks
 * @param context to get string from app's resources
 */
class LocationPermissionAlertDialogListenerImpl(
    val viewModel: WeatherForecastViewModel,
    val context: Context
) : AlertDialogClickListener {

    override fun onPositiveClick(city: String) {
        viewModel.onUpdateStatus(context.getString(R.string.geo_location_permission_required))
        viewModel.requestGeoLocationPermissionOrLoadForecast()
    }

    override fun onNegativeClick() {
        exitProcess(0)
    }
}