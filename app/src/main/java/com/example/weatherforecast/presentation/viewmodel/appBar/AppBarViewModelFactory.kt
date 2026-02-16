package com.example.weatherforecast.presentation.viewmodel.appBar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.presentation.converter.appbar.AppBarStateConverter
import com.example.weatherforecast.utils.ResourceManager

/**
 * AppBarViewModel factory
 *
 * @property resourceManager to get string resources
 * @property appBarStateConverter to convert forecast ui state to appbar ui state
 */
class AppBarViewModelFactory(
    val resourceManager: ResourceManager,
    val appBarStateConverter: AppBarStateConverter
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppBarViewModel(
            resourceManager,
            appBarStateConverter
        ) as T
    }
}