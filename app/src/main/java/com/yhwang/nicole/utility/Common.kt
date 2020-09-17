package com.yhwang.nicole.utility

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.*
import timber.log.Timber
import java.lang.Exception


fun checkArCompatibility(activity: Activity, callback: (Boolean) -> Unit) {
    val availability = ArCoreApk.getInstance().checkAvailability(activity)
    if (availability.isTransient) {
        // Re-query at 5Hz while compatibility is checked in the background.
        Handler(Looper.getMainLooper()).postDelayed({
            checkArCompatibility(activity, callback)
        }, 200)
    } else if (availability.isSupported) {
        callback(availability.isSupported)
    } else {
        callback(availability.isUnsupported)
    }
}

fun checkPermission(
    activity: AppCompatActivity,
    permission: String,
    callback: (Boolean) -> Unit
) {
    Timber.i("Check $permission.")
    activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Timber.i("Is granted.")
            callback(true)
        } else {
            Timber.i("Is not granted.")
            val message = when (permission) {
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> "允許讀取外部儲存空間以支援將相片存到相簿中"
                Manifest.permission.CAMERA -> "允許存取相機以支援AR"
                else -> "錯誤"
            }

            val listener: (DialogInterface, Int) -> Unit
            val rightButtonTitle: String
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                rightButtonTitle = "重試"
                listener =  { _, _ ->
                    checkPermission(activity, permission, callback)
                }
            } else {
                rightButtonTitle = "設定"
                listener = { _, _ ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.parse("package:" + activity.packageName)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activity.startActivity(intent)
                }
            }

            MaterialAlertDialogBuilder(activity)
                .setMessage(message)
                .setNegativeButton("取消") { _, _ -> callback(false)}
                .setPositiveButton(rightButtonTitle, listener)
                .setCancelable(false)
                .show()
        }
    }.launch(permission)
}

private var isNotRequested = true
fun checkArCoreCompatibility(activity: Activity, onAvailable: () -> Unit) {
    try {
        if (ArCoreApk.getInstance().requestInstall(activity, isNotRequested) == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
            isNotRequested = !isNotRequested
        } else {
            onAvailable()
        }
    } catch (e: Exception) {
        val message = when (e) {
            is UnavailableArcoreNotInstalledException -> "UnavailableArcoreNotInstalledException, please install ARCore."
            is UnavailableUserDeclinedInstallationException -> "UnavailableUserDeclinedInstallationException, please install ARCore."
            is UnavailableApkTooOldException -> "UnavailableApkTooOldException, please update ARCore."
            is UnavailableSdkTooOldException -> "UnavailableSdkTooOldException, please update this app."
            is UnavailableDeviceNotCompatibleException -> "UnavailableDeviceNotCompatibleException, this device does not support AR."
            else -> "Unknown exception, failed to start AR session."
        }
        MaterialAlertDialogBuilder(activity)
            .setTitle("開啟AR session失敗")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setCancelable(false)
            .show()
    }
}