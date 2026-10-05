package com.example.smartview.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartview.data.local.entity.SurveyEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Survey entities.
 */
@Dao
interface SurveyDao {

    @Query("SELECT * FROM surveys WHERE project_id = :projectId ORDER BY timestamp_epoch_ms DESC")
    fun getSurveysForProject(projectId: String): Flow<List<SurveyEntity>>

    @Query("SELECT * FROM surveys ORDER BY timestamp_epoch_ms DESC")
    fun getAllSurveys(): Flow<List<SurveyEntity>>

    @Query("SELECT * FROM surveys WHERE id = :id LIMIT 1")
    suspend fun getSurveyById(id: String): SurveyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurvey(survey: SurveyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurveys(surveys: List<SurveyEntity>)

    @Update
    suspend fun updateSurvey(survey: SurveyEntity): Int

    @Query("DELETE FROM surveys WHERE id = :id")
    suspend fun deleteSurveyById(id: String): Int

    @Query("DELETE FROM surveys WHERE project_id = :projectId")
    suspend fun deleteSurveysForProject(projectId: String): Int

    @Query("SELECT COUNT(*) FROM surveys WHERE project_id = :projectId")
    suspend fun countSurveysForProject(projectId: String): Int

    @Query("SELECT COUNT(*) FROM surveys")
    suspend fun getTotalSurveyCount(): Int
}
