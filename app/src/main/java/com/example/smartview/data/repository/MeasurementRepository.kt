package com.example.smartview.data.repository

import com.example.smartview.domain.model.Measurement
import kotlinx.coroutines.flow.Flow

/**
 * Data abstraction for construction measurements and geometric dimensions.
 */
interface MeasurementRepository {
    fun getMeasurementsForSurvey(surveyId: String): Flow<List<Measurement>>
    suspend fun getMeasurementById(id: String): Measurement?
    suspend fun recordMeasurement(measurement: Measurement): Measurement
    suspend fun updateMeasurement(measurement: Measurement): Boolean
    suspend fun deleteMeasurement(id: String): Boolean
}
