package com.example.smartview.domain.model

/**
 * High-level construction project/site entity.
 */
data class Project(
    val id: String,
    val name: String,
    val clientName: String,
    val siteAddress: String,
    val status: ProjectStatus = ProjectStatus.ACTIVE,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val surveyCount: Int = 0
)

enum class ProjectStatus {
    ACTIVE,
    SCHEDULED,
    COMPLETED,
    ARCHIVED
}

/**
 * Site survey session capturing geometric, spatial, and surface data.
 */
data class Survey(
    val id: String,
    val projectId: String,
    val surveyTitle: String,
    val surveyCode: String,
    val status: SurveyStatus = SurveyStatus.DRAFT,
    val timestampEpochMs: Long = System.currentTimeMillis(),
    val notes: String = "",
    val measurements: List<Measurement> = emptyList()
)

enum class SurveyStatus {
    DRAFT,
    IN_PROGRESS,
    PENDING_CALCULATION,
    COMPLETED
}

/**
 * Minimal foundational representation of a field measurement.
 * To be expanded in SV-002+ with AR spatial points, plane bounds, and surface areas.
 */
data class Measurement(
    val id: String,
    val surveyId: String,
    val label: String,
    val type: MeasurementType,
    val value: Double,
    val unit: String,
    val provenance: MeasurementProvenance = MeasurementProvenance.MEASURED,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

enum class MeasurementType {
    LINEAR_DISTANCE,
    SURFACE_AREA,
    VOLUME_ESTIMATE,
    ANGULAR_PITCH
}

/**
 * Data provenance tracking for construction measurements.
 * Fundamental architectural rule: Inferences and AI suggestions must never
 * be silently presented as physical measurements.
 */
enum class MeasurementProvenance {
    MEASURED,
    INFERRED,
    ESTIMATED,
    USER_CONFIRMED,
    AI_SUGGESTED
}

/**
 * Photographic survey capture representing a persistent local image file
 * associated with a specific survey session.
 */
data class SurveyCapture(
    val id: String,
    val surveyId: String,
    val localPath: String,
    val captureType: CaptureType = CaptureType.SURVEY_STILL,
    val width: Int? = null,
    val height: Int? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

enum class CaptureType {
    SURVEY_STILL,
    ELEVATION,
    SUBSTRATE,
    DEFECT
}
