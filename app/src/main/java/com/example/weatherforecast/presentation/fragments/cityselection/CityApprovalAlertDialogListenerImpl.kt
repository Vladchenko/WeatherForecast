package com.example.weatherforecast.presentation.fragments.cityselection

import android.content.Context
import android.location.Location
import com.example.weatherforecast.R
import com.example.weatherforecast.data.util.TemperatureType
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

    override fun onPositiveClick(locationName: String, location: Location?) {
        viewModel.onUpdateStatus(context.getString(R.string.network_forecast_downloading_for_city_text, locationName))
        viewModel.downloadWeatherForecast(
            TemperatureType.CELSIUS,
            location?: Location("")     //TODO
        )
    }

    override fun onNegativeClick() {
        viewModel.onGotoCitySelection()
    }
}