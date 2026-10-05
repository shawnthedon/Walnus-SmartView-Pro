package com.example.smartview.core.log

import android.util.Log

/**
 * SmartView Pro™ Unified Diagnostic Logger.
 *
 * Provides structured diagnostic logging for application startup, navigation events,
 * system lifecycle, and unexpected errors without leaking sensitive client or site data.
 */
object SmartViewLogger {

    private const val TAG = "SmartViewPro"

    var isDebugLoggingEnabled: Boolean = true

    fun d(category: String, message: String) {
        if (isDebugLoggingEnabled) {
            try {
                Log.d(TAG, "[$category] $message")
            } catch (_: Throwable) {
                println("DEBUG: [$TAG] [$category] $message")
            }
        }
    }

    fun i(category: String, message: String) {
        try {
            Log.i(TAG, "[$category] $message")
        } catch (_: Throwable) {
            println("INFO: [$TAG] [$category] $message")
        }
    }

    fun w(category: String, message: String, throwable: Throwable? = null) {
        try {
            Log.w(TAG, "[$category] $message", throwable)
        } catch (_: Throwable) {
            println("WARN: [$TAG] [$category] $message")
        }
    }

    fun e(category: String, message: String, throwable: Throwable? = null) {
        try {
            Log.e(TAG, "[$category] $message", throwable)
        } catch (_: Throwable) {
            println("ERROR: [$TAG] [$category] $message")
        }
    }

    fun logStartup(version: String) {
        i("STARTUP", "SmartView Pro™ initialized. Version: $version | Platform: Android | Foundation: SV-001")
    }

    fun logNavigation(fromRoute: String?, toRoute: String) {
        d("NAVIGATION", "Navigating: ${fromRoute ?: "ROOT"} -> $toRoute")
    }

    fun logLifecycle(event: String) {
        d("LIFECYCLE", "Event: $event")
    }
}
