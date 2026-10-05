package com.example.smartview.data.local.converter

import androidx.room.TypeConverter
import com.example.smartview.domain.model.MeasurementProvenance
import com.example.smartview.domain.model.MeasurementType
import com.example.smartview.domain.model.ProjectStatus
import com.example.smartview.domain.model.SurveyStatus

/**
 * Type converters for persistent Room storage of SmartView domain enumerations.
 */
class SmartViewTypeConverters {

    @TypeConverter
    fun fromProjectStatus(status: ProjectStatus): String = status.name

    @TypeConverter
    fun toProjectStatus(value: String): ProjectStatus = try {
        ProjectStatus.valueOf(value)
    } catch (_: Exception) {
        ProjectStatus.ACTIVE
    }

    @TypeConverter
    fun fromSurveyStatus(status: SurveyStatus): String = status.name

    @TypeConverter
    fun toSurveyStatus(value: String): SurveyStatus = try {
        SurveyStatus.valueOf(value)
    } catch (_: Exception) {
        SurveyStatus.DRAFT
    }

    @TypeConverter
    fun fromMeasurementType(type: MeasurementType): String = type.name

    @TypeConverter
    fun toMeasurementType(value: String): MeasurementType = try {
        MeasurementType.valueOf(value)
    } catch (_: Exception) {
        MeasurementType.LINEAR_DISTANCE
    }

    @TypeConverter
    fun fromMeasurementProvenance(provenance: MeasurementProvenance): String = provenance.name

    @TypeConverter
    fun toMeasurementProvenance(value: String): MeasurementProvenance = try {
        MeasurementProvenance.valueOf(value)
    } catch (_: Exception) {
        MeasurementProvenance.MEASURED
    }

    @TypeConverter
    fun fromCaptureType(type: com.example.smartview.domain.model.CaptureType): String = type.name

    @TypeConverter
    fun toCaptureType(value: String): com.example.smartview.domain.model.CaptureType = try {
        com.example.smartview.domain.model.CaptureType.valueOf(value)
    } catch (_: Exception) {
        com.example.smartview.domain.model.CaptureType.SURVEY_STILL
    }
}
