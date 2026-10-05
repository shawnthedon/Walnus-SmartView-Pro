package com.example.smartview

import android.app.Application
import android.content.Context
import com.example.smartview.core.di.DefaultSmartViewContainer
import com.example.smartview.core.di.SmartViewContainer
import com.example.smartview.core.log.SmartViewLogger

/**
 * Top-level Android Application for SmartView Pro™.
 * Initializes persistent dependency container attached to the application lifecycle.
 */
class SmartViewApplication : Application() {

    lateinit var container: SmartViewContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instanceOrNull = this
        SmartViewLogger.i("APP", "Initializing SmartViewApplication and Room persistence container")
        container = DefaultSmartViewContainer(this)
    }

    companion object {
        @Volatile
        var instanceOrNull: SmartViewApplication? = null
            private set

        val appContext: Context
            get() = instanceOrNull?.applicationContext
                ?: throw IllegalStateException("SmartViewApplication has not been initialized")
    }
}
