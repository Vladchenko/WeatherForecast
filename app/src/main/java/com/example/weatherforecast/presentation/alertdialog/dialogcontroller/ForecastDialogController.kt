package com.example.weatherforecast.presentation.alertdialog.dialogcontroller

/**
 * Manages forecast dialogs showing
 */
interface ForecastDialogController {

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
}