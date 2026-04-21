package io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.presentation.converter.appbar.AppBarStateMapper
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer

/**
 * AppBarViewModel factory
 *
 * @property statusRenderer displays loading, success, warning, or error statuses
 * @property resourceManager to get string resources
 * @property appBarStateMapper to convert forecast ui state to appbar ui state
 */
class AppBarViewModelFactory(
    private val statusRenderer: StatusRenderer,
    private val resourceManager: ResourceManager,
    private val appBarStateMapper: AppBarStateMapper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppBarViewModel(
            statusRenderer,
            resourceManager,
            appBarStateMapper
        ) as T
    }
}