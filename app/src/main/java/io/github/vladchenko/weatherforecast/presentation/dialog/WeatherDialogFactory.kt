package io.github.vladchenko.weatherforecast.presentation.dialog

import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogDelegate
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogFactory
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.dialog.LocationDialogFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for creating weather-specific alert dialogs.
 *
 * This class provides methods to build standardized dialogs used in the weather forecast feature,
 * such as city not found, location permission requests, and geolocation confirmation prompts.
 * It wraps a base [AlertDialogFactory] and delegates some dialog creation to [LocationDialogFactory]
 * to promote reuse across features.
 *
 * Strings are resolved via [ResourceManager] to support localization and testing.
 *
 * @property baseDialogFactory Factory for general-purpose alert dialogs
 * @property locationDialogFactory Reusable component for location-related dialogs (shared with other modules)
 * @property resourceManager Provides access to localized string resources
 */
@Singleton
class WeatherDialogFactory @Inject constructor(
    private val baseDialogFactory: AlertDialogFactory,
    private val locationDialogFactory: LocationDialogFactory,
    private val resourceManager: ResourceManager
) {

    /**
     * Creates a dialog to inform the user that no weather data was found for the given city.
     *
     * Displays a title with the city name and a generic message about missing data.
     * Includes a single "OK" button to dismiss the dialog.
     *
     * @param city Name of the city that could not be found
     * @param onPositive Callback triggered when the user confirms the dialog
     * @return Configured [AlertDialogDelegate] instance ready to be shown
     */
    fun createCityNotFoundDialog(
        city: String,
        onPositive: () -> Unit
    ): AlertDialogDelegate {
        return baseDialogFactory.createInfoDialog(
            title = resourceManager.getString(R.string.forecast_no_data_for_city, city),
            message = resourceManager.getString(R.string.forecast_no_data_message),
            onConfirm = onPositive
        )
    }

    /**
     * Creates a confirmation dialog for a detected location.
     *
     * Asks the user whether they want to use the automatically detected city for weather forecast.
     * Delegates to [LocationDialogFactory] for consistent behavior across features.
     *
     * @param city Detected city name
     * @param onPositive Callback when the user accepts; receives the city name
     * @param onNegative Callback when the user declines
     * @return Configured [AlertDialogDelegate] instance
     */
    fun createGeoLocationConfirmationDialog(
        city: String,
        onPositive: (String) -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return locationDialogFactory.createGeoLocationConfirmationDialog(city, onPositive, onNegative)
    }

    /**
     * Creates a dialog shown when geolocation fails unexpectedly.
     *
     * Offers retry and cancel options. Delegates to shared [LocationDialogFactory].
     *
     * @param onPositive Callback to retry location detection
     * @param onNegative Callback to cancel the operation
     * @return Configured [AlertDialogDelegate] instance
     */
    fun createGeoLocationErrorDialog(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return locationDialogFactory.createGeoLocationErrorDialog(onPositive, onNegative)
    }

    /**
     * Creates a dialog requesting location permission.
     *
     * Explains why location access is needed and offers "Allow" and "Deny" actions.
     * Delegates to shared [LocationDialogFactory] for consistency.
     *
     * @param onPositive Callback to proceed with permission request
     * @param onNegative Callback to decline permission
     * @return Configured [AlertDialogDelegate] instance
     */
    fun createPermissionDialog(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return locationDialogFactory.createLocationPermissionDialog(onPositive, onNegative)
    }

    /**
     * Creates a dialog shown when location permission has been permanently denied.
     *
     * Informs the user that they must enable permission manually via system settings.
     * Offers navigation to settings or cancellation.
     *
     * @param onPositive Callback to open app settings
     * @param onNegative Callback to cancel and close the dialog
     * @return Configured [AlertDialogDelegate] instance
     */
    fun createPermissionPermanentlyDeniedDialog(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return locationDialogFactory.createPermissionPermanentlyDeniedDialog(onPositive, onNegative)
    }
}