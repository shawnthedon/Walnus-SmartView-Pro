package com.example.smartview.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartview.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Project entities.
 */
@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects ORDER BY created_at_epoch_ms DESC")
    fun getProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)

    @Update
    suspend fun updateProject(project: ProjectEntity): Int

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String): Int

    @Query("DELETE FROM projects")
    suspend fun deleteAllProjects(): Int

    @Query("UPDATE projects SET survey_count = survey_count + 1 WHERE id = :projectId")
    suspend fun incrementSurveyCount(projectId: String)

    @Query("UPDATE projects SET survey_count = :count WHERE id = :projectId")
    suspend fun setSurveyCount(projectId: String, count: Int)

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getProjectCount(): Int
}
