package io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.vladchenko.weatherforecast.core.network.NetworkStateHolder
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.presentation.converter.appbar.AppBarStateMapper

/**
 * Factory for creating [AppBarViewModel] instances with dependency injection.
 *
 * This factory follows the `ViewModelProvider.Factory` pattern to provide pre-configured
 * dependencies to the ViewModel during instantiation. It encapsulates the creation logic
 * for [AppBarViewModel], ensuring consistent initialization across the application.
 *
 * @property stateHolder manages and broadcasts UI status messages
 * @property resourceManager provides access to application string resources
 * @property appBarStateMapper converts domain forecast states to UI-appropriate app bar states
 * @property networkStateHolder manages network connectivity (connect or disconnect)
 */
class AppBarViewModelFactory(
    private val stateHolder: StatusStateHolder,
    private val resourceManager: ResourceManager,
    private val appBarStateMapper: AppBarStateMapper,
    private val networkStateHolder: NetworkStateHolder
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the requested ViewModel.
     *
     * @param modelClass the class type of the ViewModel to create
     * @return the newly created ViewModel instance
     * @throws IllegalArgumentException if the model class is not supported
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppBarViewModel(
            stateHolder,
            resourceManager,
            appBarStateMapper,
            networkStateHolder,
        ) as T
    }
}