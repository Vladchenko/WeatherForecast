package io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel

import android.location.Location
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel

/**
 * Sealed class representing the possible events during the geolocation workflow.
 *
 * Used to communicate state changes and user actions within the geolocation feature.
 * The ViewModel emits these events to the UI layer, which reacts accordingly.
 *
 * - [SelectCity]: Request to manually select a city.
 * - [DefineLocationFail]: Failure to resolve a city from user input or state.
 * - [DeviceGeoLocationSuccess]: Successful acquisition of raw location coordinates.
 * - [DefineCityNameByLocationSuccess]: Successful resolution of a city name from coordinates.
 * - [GeoLocationPermission]: Change in the location permission state.
 */
sealed class GeoLocationEvent {

    /**
     * Indicates that the user has requested to manually select a city.
     */
    object SelectCity : GeoLocationEvent()

    /**
     * Indicates that the attempt to resolve a city (from user input or state) has failed.
     */
    object DefineLocationFail : GeoLocationEvent()

    /**
     * Carries the raw location data successfully acquired from the device.
     *
     * @property location The device location containing latitude, longitude, and other metadata.
     */
    data class DeviceGeoLocationSuccess(val location: Location) : GeoLocationEvent()

    /**
     * Carries the resolved city information obtained after reverse geocoding.
     *
     * @property cityModel The resolved city and its location.
     */
    data class DefineCityNameByLocationSuccess(val cityModel: CityLocationModel) : GeoLocationEvent()

    /**
     * Sealed class representing the possible states of location permission in the app.
     */
    sealed class GeoLocationPermission: GeoLocationEvent() {

        /**
         * Location permission has been granted by the user.
         */
        object Granted : GeoLocationPermission()

        /**
         * Location permission has been denied by the user (but not permanently).
         */
        object Denied : GeoLocationPermission()

        /**
         * Location permission has been permanently denied by the user.
         */
        object PermanentlyDenied : GeoLocationPermission()

        /**
         * Location permission is currently being requested.
         */
        object Requested : GeoLocationPermission()
    }
}