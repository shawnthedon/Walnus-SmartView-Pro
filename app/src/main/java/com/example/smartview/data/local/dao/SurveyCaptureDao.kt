package com.example.smartview.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartview.data.local.entity.SurveyCaptureEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Survey photographic capture entities.
 */
@Dao
interface SurveyCaptureDao {

    @Query("SELECT * FROM survey_captures WHERE survey_id = :surveyId ORDER BY created_at_epoch_ms DESC")
    fun getCapturesForSurvey(surveyId: String): Flow<List<SurveyCaptureEntity>>

    @Query("SELECT * FROM survey_captures ORDER BY created_at_epoch_ms DESC")
    fun getAllCaptures(): Flow<List<SurveyCaptureEntity>>

    @Query("SELECT * FROM survey_captures WHERE id = :id LIMIT 1")
    suspend fun getCaptureById(id: String): SurveyCaptureEntity?

    @Query("SELECT local_path FROM survey_captures WHERE survey_id = :surveyId")
    suspend fun getCapturePathsForSurvey(surveyId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapture(capture: SurveyCaptureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaptures(captures: List<SurveyCaptureEntity>)

    @Update
    suspend fun updateCapture(capture: SurveyCaptureEntity): Int

    @Query("DELETE FROM survey_captures WHERE id = :id")
    suspend fun deleteCaptureById(id: String): Int

    @Query("DELETE FROM survey_captures WHERE survey_id = :surveyId")
    suspend fun deleteCapturesForSurvey(surveyId: String): Int

    @Query("SELECT COUNT(*) FROM survey_captures WHERE survey_id = :surveyId")
    suspend fun countCapturesForSurvey(surveyId: String): Int

    @Query("SELECT COUNT(*) FROM survey_captures")
    suspend fun getTotalCaptureCount(): Int
}
