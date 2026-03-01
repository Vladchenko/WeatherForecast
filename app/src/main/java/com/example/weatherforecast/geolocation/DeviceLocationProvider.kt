package com.example.weatherforecast.geolocation

import android.content.Context
import android.location.Location
import com.example.weatherforecast.R
import com.example.weatherforecast.data.util.LoggingService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Defines geo location of a device using Google Play Services.
 *
 * This class retrieves the device's current location with high accuracy and notifies
 * the caller via [GeoLocationListener]. It handles:
 * - Location retrieval using [FusedLocationProviderClient]
 * - Success, failure, and cancellation callbacks
 * - Security exceptions related to permissions
 * - Structured logging via [LoggingService]
 *
 * @property loggingService Service for consistent and testable logging
 * @property context Application context used to access system services
 */
class DeviceLocationProvider @Inject constructor(
    private val loggingService: LoggingService,
    @ApplicationContext private val context: Context
) {

    /**
     * Attempts to retrieve the device's current location.
     *
     * Uses high-accuracy priority and handles:
     * - Successful location retrieval
     * - Failures (e.g. disabled location, timeouts)
     * - Cancellation of the request
     * - Security exceptions due to missing permissions
     *
     * Callbacks are delivered via [locationListener].
     *
     * @param locationListener Listener to receive success or error events
     */
    fun defineCurrentLocation(locationListener: GeoLocationListener) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                        CancellationTokenSource().token
                    override fun isCancellationRequested() = false
                })
                .addOnSuccessListener { location: Location? ->
                    onDefineLocationSuccess(location, locationListener, context)
                }
                .addOnFailureListener { exception ->
                    onDefineLocationFailure(exception, locationListener)
                }
                .addOnCanceledListener {
                    loggingService.logError(TAG, "Geo location cancelled")
                    locationListener.onCurrentGeoLocationFail(context.getString(R.string.geo_cancelled))
                }
        } catch (sec: SecurityException) {
            loggingService.logError(TAG, "SecurityException in location request: ", sec)
            locationListener.onNoGeoLocationPermission()
        }
    }

    private fun onDefineLocationFailure(
        it: Exception,
        locationListener: GeoLocationListener
    ) {
        val errorMessage = it.message.toString()
        loggingService.logError(TAG, "Location request failed: $errorMessage", it)
        if (errorMessage.contains("permission", ignoreCase = true)) {
            locationListener.onNoGeoLocationPermission()
        } else {
            locationListener.onCurrentGeoLocationFail(it.message.orEmpty())
        }
    }

    private fun onDefineLocationSuccess(
        location: Location?,
        locationListener: GeoLocationListener,
        appContext: Context
    ) {
        loggingService.logDebugEvent(TAG, "Location retrieved: $location")
        if (location == null) {
            loggingService.logError(TAG, "Location is null after successful callback")
            locationListener.onCurrentGeoLocationFail(appContext.getString(R.string.geo_resolution_failed))
        } else {
            locationListener.onCurrentGeoLocationSuccess(location)
        }
    }

    companion object {
        const val TAG = "WeatherForecastGeoLocator"
    }
}