package com.example.weatherforecast.presentation.alertdialog.delegates

import android.content.Context
import androidx.appcompat.app.AlertDialog

/**
 * Delegate for AlertDialog
 */
interface AlertDialogDelegate {
    /**
     * get AlertDialog Builder using [context]
     */
    fun getAlertDialogBuilder(context: Context): AlertDialog.Builder
}