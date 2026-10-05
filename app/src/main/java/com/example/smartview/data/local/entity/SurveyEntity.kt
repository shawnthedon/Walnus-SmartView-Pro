package com.example.smartview.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.smartview.domain.model.SurveyStatus

/**
 * Persistent Room entity for site survey sessions.
 * Maintains an explicit foreign-key relationship to [ProjectEntity].
 */
@Entity(
    tableName = "surveys",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["project_id"])
    ]
)
data class SurveyEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "project_id")
    val projectId: String,

    @ColumnInfo(name = "survey_title")
    val surveyTitle: String,

    @ColumnInfo(name = "survey_code")
    val surveyCode: String,

    @ColumnInfo(name = "status")
    val status: SurveyStatus,

    @ColumnInfo(name = "timestamp_epoch_ms")
    val timestampEpochMs: Long,

    @ColumnInfo(name = "notes")
    val notes: String = ""
)
