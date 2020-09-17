package com.yhwang.nicole


import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.theapache64.removebg.RemoveBg
import com.userstar.oqrticket.timber.ReleaseTree
import com.userstar.oqrticket.timber.ThreadIncludedDebugTree
import com.yhwang.nicole.utility.ProgressListener
import com.yhwang.nicole.utility.Updater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException


enum class Mode {
    OBJECT_2D, OBJECT_3D
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (BuildConfig.DEBUG) {
            Timber.plant(ThreadIncludedDebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }

        // cool890104@gmail.com ESJkDD4fTYSiGpLZAqQ6rd2T
        RemoveBg.init("ESJkDD4fTYSiGpLZAqQ6rd2T")

        runUpdate()
    }

    private fun runUpdate() {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + "goods" + ".apk")
        Updater(this).apply {
            auto(file, object : ProgressListener {
                lateinit var dialog: AlertDialog
                override fun onStart() {
                    GlobalScope.launch(Dispatchers.Main) {
                        dialog = AlertDialog.Builder(this@MainActivity)
                            .setView(R.layout.dialog_progress)
                            .setCancelable(false)
                            .show()
                    }
                }

                override fun onDownloading(percentage: Long) {
                    Timber.i("Download... $percentage%")
                }

                override fun onDone() {

                }

                override fun onFinished(file: File) {
                    dialog.dismiss()
                    startInstallIntent(this@MainActivity, file)
                }
            })
        }
    }
}