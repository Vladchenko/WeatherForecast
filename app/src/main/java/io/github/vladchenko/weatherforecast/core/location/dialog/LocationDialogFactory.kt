package io.github.vladchenko.weatherforecast.core.location.dialog

import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogDelegate
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogFactory
import io.github.vladchenko.weatherforecast.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Фабрика для создания диалогов, связанных с геолокацией и разрешениями
 */
@Singleton
class LocationDialogFactory @Inject constructor(
    private val baseDialogFactory: AlertDialogFactory,
    private val resourceManager: ResourceManager
) {

    /**
     * Диалог запроса разрешения на геолокацию
     */
    fun createLocationPermissionDialog(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return baseDialogFactory.createBasicDialog(
            title = resourceManager.getString(R.string.geo_permission_denied),
            message = resourceManager.getString(R.string.geo_permission_request_message),
            onPositive = onPositive,
            onNegative = onNegative
        )
    }

    /**
     * Диалог, когда разрешение на геолокацию отклонено навсегда
     */
    fun createPermissionPermanentlyDeniedDialog(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return baseDialogFactory.createCustomButtonsDialog(
            title = resourceManager.getString(R.string.geo_permission_denied),
            message = resourceManager.getString(R.string.geo_permission_denied_permanently),
            positiveButtonTextRes = android.R.string.ok,
            negativeButtonTextRes = android.R.string.cancel,
            onPositive = onPositive,
            onNegative = onNegative
        )
    }

    /**
     * Диалог подтверждения города, определенного через геолокацию
     */
    fun createGeoLocationConfirmationDialog(
        city: String,
        onPositive: (String) -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return baseDialogFactory.createBasicDialog(
            title = resourceManager.getString(R.string.geo_title),
            message = resourceManager.getString(R.string.geo_confirm_message, city),
            onPositive = { onPositive(city) },
            onNegative = onNegative
        )
    }

    /**
     * Диалог ошибки геолокации
     */
    fun createGeoLocationErrorDialog(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return baseDialogFactory.createBasicDialog(
            title = resourceManager.getString(R.string.geo_fail_title),
            message = resourceManager.getString(R.string.geo_fail_subtitle),
            onPositive = onPositive,
            onNegative = onNegative
        )
    }
}