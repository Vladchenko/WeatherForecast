package io.github.vladchenko.weatherforecast.presentation.dialog

/**
 * Controller interface for displaying weather-related dialogs.
 *
 * Defines methods to show standardized alert dialogs used throughout the weather forecast feature,
 * such as city not found, permission requests, and geolocation confirmation prompts.
 * Each method abstracts the dialog's content and actions, promoting consistent UX and separation of concerns.
 *
 * Implementations are responsible for creating and showing dialogs using injected components like
 * [WeatherDialogFactory] and [AlertDialogHelper].
 */
interface WeatherDialogController {

    /**
     * Shows a dialog indicating that no weather data was found for the specified city.
     *
     * @param city Name of the city that could not be resolved
     * @param onPositiveClick Callback triggered when the user confirms the dialog (e.g., "OK")
     */
    fun showChosenCityNotFound(
        city: String,
        onPositiveClick: () -> Unit
    )

    /**
     * Prompts the user to confirm whether they want to use an automatically detected location.
     *
     * @param city Detected city name
     * @param onPositiveClick Callback triggered when the user accepts; receives the city name
     * @param onNegativeClick Callback triggered when the user declines
     */
    fun showLocationDefined(
        city: String,
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit
    )

    /**
     * Informs the user that location permission is denied and offers a way to retry.
     *
     * @param onPositiveClick Callback to request permission again
     * @param onNegativeClick Callback to cancel and close the dialog
     */
    fun showNoPermission(
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    )

    /**
     * Alerts the user that location permission has been permanently denied.
     *
     * Guides the user to manually enable it via system settings.
     *
     * @param onPositiveClick Callback to open app settings
     * @param onNegativeClick Callback to cancel and dismiss the dialog
     */
    fun showPermissionPermanentlyDenied(
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    )

    /**
     * Shows a dialog when geolocation fails unexpectedly.
     *
     * Offers options to retry or cancel the operation.
     *
     * @param onPositiveClick Callback to retry location detection
     * @param onNegativeClick Callback to cancel and close the dialog
     */
    fun showGeoLocationError(
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    )
}