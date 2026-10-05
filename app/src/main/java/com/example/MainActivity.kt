package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.smartview.core.log.SmartViewLogger
import com.example.smartview.ui.SmartViewApp
import com.example.smartview.ui.theme.SmartViewTheme

/**
 * Main Activity host for Walnus SmartView Pro™.
 * Initializes the application foundation and delegates UI to the Compose navigation tree.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SmartViewLogger.logStartup("1.0.0-SV003")
        enableEdgeToEdge()

        val container = (application as? com.example.smartview.SmartViewApplication)?.container
            ?: com.example.smartview.core.di.DefaultSmartViewContainer(applicationContext)

        setContent {
            SmartViewTheme {
                SmartViewApp(container = container)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SmartViewLogger.logLifecycle("MainActivity.onResume")
    }

    override fun onPause() {
        super.onPause()
        SmartViewLogger.logLifecycle("MainActivity.onPause")
    }
}
