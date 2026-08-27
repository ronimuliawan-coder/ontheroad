package com.ontheroad

import android.app.Application
import com.ontheroad.core.data.local.OnTheRoadDatabase

class OnTheRoadApplication : Application() {

    val database: OnTheRoadDatabase by lazy {
        OnTheRoadDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
