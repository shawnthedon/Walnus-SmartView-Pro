package com.example.smartview.core.di

import android.content.Context
import com.example.smartview.SmartViewApplication
import com.example.smartview.core.dispatchers.DispatcherProvider
import com.example.smartview.core.dispatchers.StandardDispatcherProvider
import com.example.smartview.data.local.SmartViewDatabase
import com.example.smartview.data.repository.MeasurementRepository
import com.example.smartview.data.repository.ProjectRepository
import com.example.smartview.data.repository.RoomProjectRepository
import com.example.smartview.data.repository.SurveyRepository

/**
 * Dependency container providing foundation services.
 * Integrates Room database persistence as the active local data source.
 */
interface SmartViewContainer {
    val dispatchers: DispatcherProvider
    val projectRepository: ProjectRepository
    val surveyRepository: SurveyRepository
    val measurementRepository: MeasurementRepository
    val surveyCaptureRepository: com.example.smartview.data.repository.SurveyCaptureRepository
    val database: SmartViewDatabase
    val applicationContext: Context
}

class DefaultSmartViewContainer(
    context: Context? = null,
    override val dispatchers: DispatcherProvider = StandardDispatcherProvider()
) : SmartViewContainer {

    private val appContext: Context by lazy {
        context?.applicationContext
            ?: SmartViewApplication.instanceOrNull?.applicationContext
            ?: resolveTestApplicationContext()
            ?: throw IllegalStateException("Application Context required to initialize SmartViewContainer")
    }

    override val applicationContext: Context get() = appContext

    override val database: SmartViewDatabase by lazy {
        SmartViewDatabase.getInstance(appContext)
    }

    private val roomRepository: RoomProjectRepository by lazy {
        RoomProjectRepository(database, dispatchers)
    }

    override val projectRepository: ProjectRepository get() = roomRepository
    override val surveyRepository: SurveyRepository get() = roomRepository
    override val measurementRepository: MeasurementRepository get() = roomRepository
    override val surveyCaptureRepository: com.example.smartview.data.repository.SurveyCaptureRepository get() = roomRepository

    companion object {
        private fun resolveTestApplicationContext(): Context? {
            return try {
                val appProviderClass = Class.forName("androidx.test.core.app.ApplicationProvider")
                val method = appProviderClass.getMethod("getApplicationContext")
                method.invoke(null) as? Context
            } catch (_: Throwable) {
                null
            }
        }
    }
}
