package com.example.weatherforecast.presentation.alertdialog

import android.content.Context
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.AlertDialogClickListener
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel

/**
 * Alert dialog that asks user if the geo located city is a proper one to provide a weather forecast on.
 * If a user agrees to the located city, its forecast is downloaded, otherwise, user is suggested to choose another city.
 */
class CityApprovalAlertDialogListenerImpl(
    val viewModel: WeatherForecastViewModel,
    val context: Context
) : AlertDialogClickListener {

    override fun onPositiveClick(city: String) {
        viewModel.onUpdateStatus(context.getString(R.string.forecast_downloading_for_city_text, city))
        viewModel.downloadWeatherForecastForCityOrGeoLocation(city)
    }

    override fun onNegativeClick() {
        viewModel.onGotoCitySelection()
    }
}