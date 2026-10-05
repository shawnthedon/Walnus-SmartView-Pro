package com.example.smartview.data.repository

import com.example.smartview.core.dispatchers.DispatcherProvider
import com.example.smartview.core.log.SmartViewLogger
import com.example.smartview.data.local.SmartViewDatabase
import com.example.smartview.data.local.mapper.toDomain
import com.example.smartview.data.local.mapper.toEntity
import com.example.smartview.domain.model.Measurement
import com.example.smartview.domain.model.Project
import com.example.smartview.domain.model.Survey
import com.example.smartview.domain.model.SurveyCapture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Production Room-backed repository implementing the SmartView persistence pipeline.
 * Fully offline-first: SQLite/Room is authoritative on-device storage.
 *
 * Implements [ProjectRepository], [SurveyRepository], [MeasurementRepository], and [SurveyCaptureRepository].
 */
class RoomProjectRepository(
    private val database: SmartViewDatabase,
    private val dispatchers: DispatcherProvider
) : ProjectRepository, SurveyRepository, MeasurementRepository, SurveyCaptureRepository {

    private val projectDao = database.projectDao()
    private val surveyDao = database.surveyDao()
    private val measurementDao = database.measurementDao()
    private val surveyCaptureDao = database.surveyCaptureDao()

    // ==========================================
    // Project Repository Implementation
    // ==========================================

    override fun getProjects(): Flow<List<Project>> {
        return projectDao.getProjects()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun getProjectById(id: String): Project? = withContext(dispatchers.io) {
        try {
            projectDao.getProjectById(id)?.toDomain()
        } catch (e: Exception) {
            SmartViewLogger.e("REPOSITORY", "Error querying project by id: $id", e)
            null
        }
    }

    override suspend fun createProject(project: Project): Project = withContext(dispatchers.io) {
        SmartViewLogger.i("REPOSITORY", "Persisting new project to Room: ${project.name} (${project.id})")
        projectDao.insertProject(project.toEntity())
        project
    }

    override suspend fun updateProject(project: Project): Boolean = withContext(dispatchers.io) {
        try {
            val rows = projectDao.updateProject(project.toEntity())
            rows > 0
        } catch (e: Exception) {
            SmartViewLogger.e("REPOSITORY", "Error updating project: ${project.id}", e)
            false
        }
    }

    override suspend fun deleteProject(id: String): Boolean = withContext(dispatchers.io) {
        try {
            val rows = projectDao.deleteProjectById(id)
            rows > 0
        } catch (e: Exception) {
            SmartViewLogger.e("REPOSITORY", "Error deleting project: $id", e)
            false
        }
    }

    // ==========================================
    // Survey Repository Implementation
    // ==========================================

    override fun getSurveysForProject(projectId: String): Flow<List<Survey>> {
        return surveyDao.getSurveysForProject(projectId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun getSurveyById(id: String): Survey? = withContext(dispatchers.io) {
        try {
            surveyDao.getSurveyById(id)?.toDomain()
        } catch (e: Exception) {
            SmartViewLogger.e("REPOSITORY", "Error querying survey by id: $id", e)
            null
        }
    }

    override suspend fun createSurvey(survey: Survey): Survey = withContext(dispatchers.io) {
        SmartViewLogger.i("REPOSITORY", "Persisting new survey to Room: ${survey.surveyTitle} (${survey.surveyCode})")
        surveyDao.insertSurvey(survey.toEntity())
        projectDao.incrementSurveyCount(survey.projectId)
        survey
    }

    override suspend fun updateSurvey(survey: Survey): Boolean = withContext(dispatchers.io) {
        try {
            val rows = surveyDao.updateSurvey(survey.toEntity())
            rows > 0
        } catch (e: Exception) {
            SmartViewLogger.e("REPOSITORY", "Error updating survey: ${survey.id}", e)
            false
        }
    }

    override suspend fun deleteSurvey(id: String): Boolean = withContext(dispatchers.io) {
        try {
            // Safe cleanup of associated physical media files
            val capturePaths = surveyCaptureDao.getCapturePathsForSurvey(id)
            capturePaths.forEach { path ->
                try {
                    val file = File(path)
                    if (file.exists()) file.delete()
                } catch (e: Exception) {
                    SmartViewLogger.e("REPOSITORY", "Failed to clean up image file on survey delete: $path", e)
                }
            }

            val existing = surveyDao.getSurveyById(id)
            val rows = surveyDao.deleteSurveyById(id)
            if (rows > 0 && existing != null) {
                val remainingCount = surveyDao.countSurveysForProject(existing.projectId)
                projectDao.setSurveyCount(existing.projectId, remainingCount)
            }
            rows > 0
        } catch (e: Exception) {
            SmartViewLogger.e("REPOSITORY", "Error deleting survey: $id", e)
            false
        }
    }

    // ==========================================
    // Measurement Repository Implementation
    // ==========================================

    override fun getMeasurementsForSurvey(surveyId: String): Flow<List<Measurement>> {
        return measurementDao.getMeasurementsForSurvey(surveyId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun getMeasurementById(id: String): Measurement? = withContext(dispatchers.io) {
        try {
            measurementDao.getMeasurementById(id)?.toDomain()
        } catch (e: Exception) {
            SmartViewLogger.e("REPOSITORY", "Error querying measurement by id: $id", e)
            null
        }
    }

    override suspend fun recordMeasurement(measurement: Measurement): Measurement = withContext(dispatchers.io) {
        SmartViewLogger.i("REPOSITORY", "Persisting measurement to Room: ${measurement.label} [${measurement.provenance}]")
        measurementDao.insertMeasurement(measurement.toEntity())
        measurement
    }

    override suspend fun updateMeasurement(measurement: Measurement): Boolean = withContext(dispatchers.io) {
        try {
            val rows = measurementDao.updateMeasurement(measurement.toEntity())
            rows > 0
        } catch (e: Exception) {
            SmartViewLogger.e("REPOSITORY", "Error updating measurement: ${measurement.id}", e)
            false
        }
    }

    override suspend fun deleteMeasurement(id: String): Boolean = withContext(dispatchers.io) {
        try {
            val rows = measurementDao.deleteMeasurementById(id)
            rows > 0
        } catch (e: Exception) {
            SmartViewLogger.e("REPOSITORY", "Error deleting measurement: $id", e)
            false
        }
    }

    // ==========================================
    // Survey Capture Repository Implementation (SV-003)
    // ==========================================

    override fun getCapturesForSurvey(surveyId: String): Flow<List<SurveyCapture>> {
        return surveyCaptureDao.getCapturesForSurvey(surveyId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun getCaptureById(id: String): SurveyCapture? = withContext(dispatchers.io) {
        try {
            surveyCaptureDao.getCaptureById(id)?.toDomain()
        } catch (e: Exception) {
            SmartViewLogger.e("REPOSITORY", "Error querying capture by id: $id", e)
            null
        }
    }

    override suspend fun saveCapture(capture: SurveyCapture): SurveyCapture = withContext(dispatchers.io) {
        SmartViewLogger.i("REPOSITORY", "Persisting survey capture: ${capture.id} for survey: ${capture.surveyId}")
        surveyCaptureDao.insertCapture(capture.toEntity())
        capture
    }

    override suspend fun deleteCapture(id: String): Boolean = withContext(dispatchers.io) {
        try {
            val existing = surveyCaptureDao.getCaptureById(id)
            if (existing != null) {
                try {
                    val file = File(existing.localPath)
                    if (file.exists()) file.delete()
                } catch (e: Exception) {
                    SmartViewLogger.e("REPOSITORY", "Failed to clean up image file on capture delete: ${existing.localPath}", e)
                }
            }
            val rows = surveyCaptureDao.deleteCaptureById(id)
            rows > 0
        } catch (e: Exception) {
            SmartViewLogger.e("REPOSITORY", "Error deleting capture: $id", e)
            false
        }
    }

    override suspend fun deleteCapturesForSurvey(surveyId: String): Int = withContext(dispatchers.io) {
        try {
            val capturePaths = surveyCaptureDao.getCapturePathsForSurvey(surveyId)
            capturePaths.forEach { path ->
                try {
                    val file = File(path)
                    if (file.exists()) file.delete()
                } catch (e: Exception) {
                    SmartViewLogger.e("REPOSITORY", "Failed to clean up image file: $path", e)
                }
            }
            surveyCaptureDao.deleteCapturesForSurvey(surveyId)
        } catch (e: Exception) {
            SmartViewLogger.e("REPOSITORY", "Error deleting captures for survey: $surveyId", e)
            0
        }
    }
}
