package com.example.smartview.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartview.data.local.entity.MeasurementEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Measurement entities.
 */
@Dao
interface MeasurementDao {

    @Query("SELECT * FROM measurements WHERE survey_id = :surveyId ORDER BY timestamp_epoch_ms ASC")
    fun getMeasurementsForSurvey(surveyId: String): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE id = :id LIMIT 1")
    suspend fun getMeasurementById(id: String): MeasurementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: MeasurementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurements(measurements: List<MeasurementEntity>)

    @Update
    suspend fun updateMeasurement(measurement: MeasurementEntity): Int

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteMeasurementById(id: String): Int

    @Query("DELETE FROM measurements WHERE survey_id = :surveyId")
    suspend fun deleteMeasurementsForSurvey(surveyId: String): Int

    @Query("SELECT COUNT(*) FROM measurements WHERE survey_id = :surveyId")
    suspend fun countMeasurementsForSurvey(surveyId: String): Int
}
