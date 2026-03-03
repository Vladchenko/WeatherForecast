package com.example.weatherforecast.presentation.alertdialog.dialogcontroller

/**
 * Manages forecast dialogs showing
 */
interface WeatherDialogController {

    /**
     * Shows dialog when city that user picked up is not found
     *
     * @param city that user picked up
     * @param onPositiveClick callback when user clicks positive button click
     */
    fun showChosenCityNotFound(city: String, onPositiveClick: () -> Unit)

    /**
     * Shows dialog when location is defined
     *
     * @param message to show
     * @param onPositiveClick callback when user clicks positive button
     * @param onNegativeClick callback when user clicks negative button
     */
    fun showLocationDefined(
        message: String,
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit
    )

    /**
     * Shows dialog when permission to provide geo location for the current device is not granted
     *
     * @param onPositiveClick callback when user clicks positive button
     * @param onNegativeClick callback when user clicks negative button
     */
    fun showNoPermission(onPositiveClick: () -> Unit, onNegativeClick: () -> Unit)

    /**
     * Shows dialog when permission to provide geo location for the current device is permanently denied
     *
     * @param onPositiveClick callback when user clicks positive button
     * @param onNegativeClick callback when user clicks negative button
     */
    fun showPermissionPermanentlyDenied(onPositiveClick: () -> Unit,
                                        onNegativeClick: () -> Unit)

    /**
     * Shows an error dialog when geolocation fails after multiple attempts or due to a critical issue.
     *
     * This dialog informs the user that the app could not determine their location,
     * and offers options to either select a city manually or retry the geolocation process.
     *
     * @param onPositiveClick Called when the user chooses to proceed (e.g., open city selection)
     * @param onNegativeClick Called when the user chooses to retry geolocation or dismiss the error
     */
    fun showGeoLocationError(onPositiveClick: () -> Unit,
                             onNegativeClick: () -> Unit)
}