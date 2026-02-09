package com.example.weatherforecast.geolocation

import android.Manifest
import androidx.activity.result.ActivityResultLauncher
import javax.inject.Inject

/**
 * Handles location permission request flow.
 *
 * @constructor
 * @property launcher to request runtime permission (from Fragment or Activity)
 * @property onPermissionResult callback invoked when permission result is received, true if granted
 */
class PermissionResolver @Inject constructor() {
    private var launcher: ActivityResultLauncher<String>? = null
    private var onPermissionResult: ((Boolean) -> Unit)? = null

    /**
     * Connects the resolver to the Fragment's [launcher] and [onPermissionResult] callback.
     */
    fun connect(
        launcher: ActivityResultLauncher<String>,
        onPermissionResult: (Boolean) -> Unit
    ) {
        this.launcher = launcher
        this.onPermissionResult = onPermissionResult
    }

    /**
     * Requests location permission.
     */
    fun requestLocationPermission() {
        launcher?.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            ?: error("ActivityResultLauncher not connected. Call connect() first.")
    }

    /**
     * Invokes permission result handling, providing result with [isGranted] flag.
     */
    fun handlePermissionResult(isGranted: Boolean) {
        onPermissionResult?.invoke(isGranted)
    }
}