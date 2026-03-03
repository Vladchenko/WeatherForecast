package com.example.weatherforecast.presentation.alertdialog

import com.example.weatherforecast.presentation.alertdialog.delegates.AlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.GeoLocationAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.GeoLocationErrorDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.LocationPermissionAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.NoLocationPermissionPermanentlyAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.SelectedCityNotFoundAlertDialogDelegate
import com.example.weatherforecast.utils.ResourceManager

/**
 * Factory class for creating different types of alert dialog delegates used throughout the application.
 *
 * This factory encapsulates the creation logic for various [AlertDialogDelegate] implementations,
 * each corresponding to a specific user interaction scenario, such as:
 * - Requesting location permission
 * - Handling permanently denied location permissions
 * - Confirming a city found via geolocation
 * - Notifying the user when a selected city is not found
 * - Handling geolocation errors
 *
 * Each method returns a configured [AlertDialogDelegate] instance with appropriate
 * text resources (via [ResourceManager]) and callback handlers.
 *
 * @property resourceManager Provides access to string resources for localized dialog content
 *
 * @constructor Creates an AlertDialogFactory with the given resource manager
 */
class AlertDialogFactory(
    private val resourceManager: ResourceManager
) {

    /**
     * Creates an alert dialog delegate to request location permission from the user.
     *
     * @param onPositive Called when the user agrees to grant location permission
     * @param onNegative Called when the user declines the permission request
     * @return Configured [AlertDialogDelegate] instance for location permission request
     */
    fun createLocationPermissionDelegate(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return LocationPermissionAlertDialogDelegate(resourceManager, onPositive, onNegative)
    }

    /**
     * Creates an alert dialog delegate for the case when location permission has been
     * permanently denied by the user.
     *
     * In this case, the app cannot request permission again, so the user must be
     * redirected to app settings manually.
     *
     * @param onPositive Called when the user chooses to go to settings (e.g., opens app settings)
     * @param onNegative Called when the user cancels and does not wish to change settings
     * @return Configured [AlertDialogDelegate] instance for permanently denied permission
     */
    fun createPermissionPermanentlyDeniedDelegate(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return NoLocationPermissionPermanentlyAlertDialogDelegate(
            resourceManager,
            { onPositive() },
            onNegative
        )
    }

    /**
     * Creates an alert dialog delegate to confirm a city detected via geolocation.
     *
     * Shows the detected city name and asks the user whether to use it.
     *
     * @param city The name of the city detected by geolocation
     * @param onPositive Called when the user confirms using the detected city; receives the city name
     * @param onNegative Called when the user decides not to use the detected city
     * @return Configured [AlertDialogDelegate] instance for geolocation confirmation
     */
    fun createGeoLocationDelegate(
        city: String,
        onPositive: (String) -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return GeoLocationAlertDialogDelegate(city, resourceManager, onPositive, onNegative)
    }

    /**
     * Creates an alert dialog delegate to notify the user that the selected city was not found.
     *
     * This can happen if the city was removed or there's an issue with data lookup.
     *
     * @param city The name of the city that could not be found
     * @param onPositive Called when the user acknowledges the message
     * @param onNegative Optional; called if the user chooses to cancel or dismiss negatively
     * @return Configured [AlertDialogDelegate] instance for "city not found" case
     */
    fun createCityNotFoundDelegate(
        city: String,
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return SelectedCityNotFoundAlertDialogDelegate(
            city,
            resourceManager,
            onPositive,
            onNegative
        )
    }

    /**
     * Creates an alert dialog delegate to handle errors during geolocation process.
     *
     * This dialog informs the user that an error occurred while trying to determine
     * their current location, possibly due to disabled GPS or network issues.
     *
     * @param onPositive Called when the user wants to retry or proceed despite the error
     * @param onNegative Called when the user wants to cancel the operation
     * @return Configured [AlertDialogDelegate] instance for geolocation errors
     */
    fun createGeoLocationErrorDelegate(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return GeoLocationErrorDelegate(
            resourceManager,
            onPositive,
            onNegative
        )
    }
}