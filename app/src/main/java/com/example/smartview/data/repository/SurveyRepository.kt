package com.example.smartview.data.repository

import com.example.smartview.domain.model.Survey
import kotlinx.coroutines.flow.Flow

/**
 * Data abstraction for site survey sessions.
 */
interface SurveyRepository {
    fun getSurveysForProject(projectId: String): Flow<List<Survey>>
    suspend fun getSurveyById(id: String): Survey?
    suspend fun createSurvey(survey: Survey): Survey
    suspend fun updateSurvey(survey: Survey): Boolean
    suspend fun deleteSurvey(id: String): Boolean
}
