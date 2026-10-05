package com.example.smartview.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.smartview.domain.model.MeasurementProvenance
import com.example.smartview.domain.model.MeasurementType

/**
 * Persistent Room entity for field measurements.
 * Maintains an explicit foreign-key relationship to [SurveyEntity]
 * and preserves the required data provenance classifications.
 */
@Entity(
    tableName = "measurements",
    foreignKeys = [
        ForeignKey(
            entity = SurveyEntity::class,
            parentColumns = ["id"],
            childColumns = ["survey_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["survey_id"])
    ]
)
data class MeasurementEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "survey_id")
    val surveyId: String,

    @ColumnInfo(name = "label")
    val label: String,

    @ColumnInfo(name = "type")
    val type: MeasurementType,

    @ColumnInfo(name = "value")
    val value: Double,

    @ColumnInfo(name = "unit")
    val unit: String,

    @ColumnInfo(name = "provenance")
    val provenance: MeasurementProvenance = MeasurementProvenance.MEASURED,

    @ColumnInfo(name = "timestamp_epoch_ms")
    val timestampEpochMs: Long
)
