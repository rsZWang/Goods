package com.yhwang.nicole.utility

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.google.ar.core.ArCoreApk


fun checkArCompatibility(activity: Activity, callback: (Boolean) -> Unit) {
    val availability = ArCoreApk.getInstance().checkAvailability(activity)
    if (availability.isTransient) {
        // Re-query at 5Hz while compatibility is checked in the background.
        Handler(Looper.getMainLooper()).postDelayed({
            checkArCompatibility(activity, callback)
        }, 200)
    } else if (availability.isSupported) {
        when (ArCoreApk.getInstance().requestInstall(activity, true)) {
            ArCoreApk.InstallStatus.INSTALLED -> {
                callback(availability.isSupported)
            }

            ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                checkArCompatibility(activity, callback)
            }
        }
    } else {
        callback(availability.isUnsupported)
    }
}