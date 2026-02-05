package com.example.weatherforecast.presentation.alertdialog.dialogcontroller

/**
 * Manages forecast dialogs showing
 */
interface ForecastDialogController {

    /**
     * Shows dialog when city that user picked up is not found
     *
     * @param city that user picked up
     * @param onPositive callback when user clicks positive button
     */
    fun showChosenCityNotFound(city: String, onPositive: (String) -> Unit)

    /**
     * Shows dialog when location is defined
     *
     * @param message to show
     * @param onPositive callback when user clicks positive button
     * @param onNegative callback when user clicks negative button
     */
    fun showLocationDefined(
        message: String,
        onPositive: (String) -> Unit,
        onNegative: () -> Unit
    )

    /**
     * Shows dialog when permission to provide geo location for the current device is not granted
     *
     * @param onPositive callback when user clicks positive button
     * @param onNegative callback when user clicks negative button
     */
    fun showNoPermission(onPositive: () -> Unit, onNegative: () -> Unit)

    /**
     * Shows dialog when permission to provide geo location for the current device is permanently denied
     *
     * @param onPositive callback when user clicks positive button
     * @param onNegative callback when user clicks negative button
     */
    fun showPermissionPermanentlyDenied(onPositive: () -> Unit,
                                        onNegative: () -> Unit)
}