package com.example.smartview.data.local.mapper

import com.example.smartview.data.local.entity.MeasurementEntity
import com.example.smartview.data.local.entity.ProjectEntity
import com.example.smartview.data.local.entity.SurveyEntity
import com.example.smartview.domain.model.Measurement
import com.example.smartview.domain.model.Project
import com.example.smartview.domain.model.Survey

/**
 * Bi-directional mapping between persistent Room entities and core domain models.
 * Preserves clean architectural boundaries between storage and domain logic.
 */

fun ProjectEntity.toDomain(): Project = Project(
    id = id,
    name = name,
    clientName = clientName,
    siteAddress = siteAddress,
    status = status,
    createdAtEpochMs = createdAtEpochMs,
    surveyCount = surveyCount
)

fun Project.toEntity(): ProjectEntity = ProjectEntity(
    id = id,
    name = name,
    clientName = clientName,
    siteAddress = siteAddress,
    status = status,
    createdAtEpochMs = createdAtEpochMs,
    surveyCount = surveyCount
)

fun SurveyEntity.toDomain(measurements: List<Measurement> = emptyList()): Survey = Survey(
    id = id,
    projectId = projectId,
    surveyTitle = surveyTitle,
    surveyCode = surveyCode,
    status = status,
    timestampEpochMs = timestampEpochMs,
    notes = notes,
    measurements = measurements
)

fun Survey.toEntity(): SurveyEntity = SurveyEntity(
    id = id,
    projectId = projectId,
    surveyTitle = surveyTitle,
    surveyCode = surveyCode,
    status = status,
    timestampEpochMs = timestampEpochMs,
    notes = notes
)

fun MeasurementEntity.toDomain(): Measurement = Measurement(
    id = id,
    surveyId = surveyId,
    label = label,
    type = type,
    value = value,
    unit = unit,
    provenance = provenance,
    timestampEpochMs = timestampEpochMs
)

fun Measurement.toEntity(): MeasurementEntity = MeasurementEntity(
    id = id,
    surveyId = surveyId,
    label = label,
    type = type,
    value = value,
    unit = unit,
    provenance = provenance,
    timestampEpochMs = timestampEpochMs
)

fun com.example.smartview.data.local.entity.SurveyCaptureEntity.toDomain(): com.example.smartview.domain.model.SurveyCapture =
    com.example.smartview.domain.model.SurveyCapture(
        id = id,
        surveyId = surveyId,
        localPath = localPath,
        captureType = captureType,
        width = width,
        height = height,
        createdAtEpochMs = createdAtEpochMs
    )

fun com.example.smartview.domain.model.SurveyCapture.toEntity(): com.example.smartview.data.local.entity.SurveyCaptureEntity =
    com.example.smartview.data.local.entity.SurveyCaptureEntity(
        id = id,
        surveyId = surveyId,
        localPath = localPath,
        captureType = captureType,
        width = width,
        height = height,
        createdAtEpochMs = createdAtEpochMs
    )
