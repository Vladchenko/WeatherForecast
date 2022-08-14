package com.example.weatherforecast.geolocation

import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.WeatherForecastApp
import com.example.weatherforecast.presentation.fragments.CurrentTimeForecastFragment
import com.example.weatherforecast.presentation.viewmodel.WeatherForecastViewModel

/**
 * TODO
 */
class GeoLocationListenerImpl(
    private val viewModel: WeatherForecastViewModel,
    private val sharedPreferences: SharedPreferences
) : GeoLocationListener {

    override fun onGeoLocationSuccess(
        location: Location,
        locationName: String
    ) {
        Log.d("GeoLocationListenerImpl", "onGeoLocationSuccess")
        sharedPreferences.edit().putString(CurrentTimeForecastFragment.CITY_ARGUMENT_KEY, locationName).apply()
        // localLocation = location
        viewModel.onGeoLocationSuccess(locationName)
    }

    override fun onGeoLocationFail() {
        viewModel.onGeoLocationFail(
            (viewModel.getApplication() as WeatherForecastApp).applicationContext
                .getString(R.string.location_fail_error_text)
        )
    }
}