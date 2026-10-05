package com.example.smartview.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.smartview.domain.model.CaptureType

/**
 * Persistent Room entity for site survey camera captures.
 * Maintains an explicit foreign-key relationship to [SurveyEntity].
 */
@Entity(
    tableName = "survey_captures",
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
data class SurveyCaptureEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "survey_id")
    val surveyId: String,

    @ColumnInfo(name = "local_path")
    val localPath: String,

    @ColumnInfo(name = "capture_type")
    val captureType: CaptureType = CaptureType.SURVEY_STILL,

    @ColumnInfo(name = "width")
    val width: Int? = null,

    @ColumnInfo(name = "height")
    val height: Int? = null,

    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
