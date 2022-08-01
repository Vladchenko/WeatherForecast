package com.example.weatherforecast.presentation.fragments

import android.view.View
import androidx.navigation.NavController
import com.example.weatherforecast.R

/**
 * Click listener that navigates to city chooser fragment.
 */
class CityClickListener(
    private val navController: NavController
): View.OnClickListener {
    override fun onClick(v: View?) {
        navController.navigate(R.id.action_currentTimeForecastFragment_to_citiesNamesFragment)
    }
}