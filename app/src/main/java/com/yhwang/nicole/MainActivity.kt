package com.yhwang.nicole

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.theapache64.removebg.RemoveBg
import com.userstar.oqrticket.timber.ReleaseTree
import com.userstar.oqrticket.timber.ThreadIncludedDebugTree
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (BuildConfig.DEBUG) {
            Timber.plant(ThreadIncludedDebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }

        RemoveBg.init("1UzzjTXNCaz991c8qooWVr86")
    }
}