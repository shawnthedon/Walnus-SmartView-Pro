package com.example.smartview.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.smartview.domain.model.ProjectStatus

/**
 * Persistent Room entity for construction projects.
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "client_name")
    val clientName: String,

    @ColumnInfo(name = "site_address")
    val siteAddress: String,

    @ColumnInfo(name = "status")
    val status: ProjectStatus,

    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,

    @ColumnInfo(name = "survey_count")
    val surveyCount: Int = 0
)
