package com.example.weatherforecast.presentation.viewmodel.geolocation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * GeoLocationViewModel factory
 *
 * @property app custom [Application] implementation for Hilt
 */
class GeoLocationViewModelFactory(
    private val app: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GeoLocationViewModel(
            app
        ) as T
    }
}