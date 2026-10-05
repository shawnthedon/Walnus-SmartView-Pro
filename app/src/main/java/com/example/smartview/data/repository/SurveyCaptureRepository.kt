package com.example.smartview.data.repository

import com.example.smartview.domain.model.SurveyCapture
import kotlinx.coroutines.flow.Flow

/**
 * Data abstraction for survey photographic captures and media records.
 */
interface SurveyCaptureRepository {
    fun getCapturesForSurvey(surveyId: String): Flow<List<SurveyCapture>>
    suspend fun getCaptureById(id: String): SurveyCapture?
    suspend fun saveCapture(capture: SurveyCapture): SurveyCapture
    suspend fun deleteCapture(id: String): Boolean
    suspend fun deleteCapturesForSurvey(surveyId: String): Int
}
