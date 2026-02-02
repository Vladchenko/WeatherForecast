package com.example.weatherforecast.presentation.viewmodel.appBar

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.presentation.converter.appbar.AppBarStateConverter

/**
 * AppBarViewModel factory
 *
 * @property app to get string resources
 * @property appBarStateConverter to convert forecast ui state to appbar ui state
 */
class AppBarViewModelFactory(
    val app: Application,
    val appBarStateConverter: AppBarStateConverter
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppBarViewModel(
            app,
            appBarStateConverter
        ) as T
    }
}