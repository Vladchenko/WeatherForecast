package io.github.vladchenko.weatherforecast.presentation.dialog.dialogcontroller

import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogHelper
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogControllerImpl
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogController
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory class for creating instances of [WeatherDialogController].
 *
 * This factory encapsulates the creation logic for dialog controllers,
 * allowing dependency injection of required components such as [WeatherDialogFactory]
 * and [AlertDialogHelper]. It provides a clean way to instantiate the controller,
 * which can be useful if additional configuration or parameters are needed in the future.
 *
 * The factory pattern ensures flexibility and testability, especially when different
 * controller implementations might be used under varying conditions.
 *
 * @property dialogFactory Factory responsible for creating specific weather-related dialogs
 * @property dialogHelper Helper for building and showing standard alert dialogs
 */
@Singleton
class WeatherDialogControllerFactory @Inject constructor(
    private val dialogFactory: WeatherDialogFactory,
    private val dialogHelper: AlertDialogHelper
) {

    /**
     * Creates and returns a new instance of [WeatherDialogController].
     *
     * Currently returns a [WeatherDialogControllerImpl] with injected dependencies.
     * Can be extended to support different implementations based on runtime conditions.
     *
     * @return A fully configured [WeatherDialogController] instance ready for use
     */
    fun create(): WeatherDialogController {
        return WeatherDialogControllerImpl(dialogFactory, dialogHelper)
    }
}