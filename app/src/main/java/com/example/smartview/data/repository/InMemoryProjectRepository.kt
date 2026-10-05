package com.example.smartview.data.repository

import com.example.smartview.core.log.SmartViewLogger
import com.example.smartview.domain.model.Project
import com.example.smartview.domain.model.ProjectStatus
import com.example.smartview.domain.model.Survey
import com.example.smartview.domain.model.SurveyStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Clean foundational in-memory repository for SV-001.
 * Acts as an architectural precursor to local database persistence in future tasks.
 */
class InMemoryProjectRepository : ProjectRepository, SurveyRepository {

    private val mutex = Mutex()

    private val _projects = MutableStateFlow<List<Project>>(
        listOf(
            Project(
                id = "proj-001",
                name = "Walnus HQ Innovation Center",
                clientName = "Walnus Global Ventures",
                siteAddress = "Tower 4, Enterprise Blvd, Tech District",
                status = ProjectStatus.ACTIVE,
                createdAtEpochMs = System.currentTimeMillis() - 86400000L * 3,
                surveyCount = 2
            ),
            Project(
                id = "proj-002",
                name = "Apex Industrial Facility Sector 7",
                clientName = "Apex Logistics International",
                siteAddress = "Harbor Logistics Corridor, Berth 12",
                status = ProjectStatus.SCHEDULED,
                createdAtEpochMs = System.currentTimeMillis() - 86400000L * 7,
                surveyCount = 1
            ),
            Project(
                id = "proj-003",
                name = "Metropolitan Retail Atrium",
                clientName = "Horizon Commercial Properties",
                siteAddress = "400 Grand Avenue, Central Core",
                status = ProjectStatus.ACTIVE,
                createdAtEpochMs = System.currentTimeMillis() - 86400000L * 1,
                surveyCount = 0
            )
        )
    )

    private val _surveys = MutableStateFlow<List<Survey>>(
        listOf(
            Survey(
                id = "srv-101",
                projectId = "proj-001",
                surveyTitle = "Exterior Facade & Substrate Assessment",
                surveyCode = "SV-2026-081",
                status = SurveyStatus.IN_PROGRESS,
                timestampEpochMs = System.currentTimeMillis() - 3600000L * 4,
                notes = "High-velocity wind exposure on North facade. SmartCoat™ primer test recommended."
            ),
            Survey(
                id = "srv-102",
                projectId = "proj-001",
                surveyTitle = "Ground Floor Entrance Vestibule",
                surveyCode = "SV-2026-082",
                status = SurveyStatus.COMPLETED,
                timestampEpochMs = System.currentTimeMillis() - 86400000L,
                notes = "Epoxy screed evaluation. Leveling needed prior to topcoat."
            ),
            Survey(
                id = "srv-103",
                projectId = "proj-002",
                surveyTitle = "High-Bay Warehouse Slab Survey",
                surveyCode = "SV-2026-090",
                status = SurveyStatus.DRAFT,
                timestampEpochMs = System.currentTimeMillis() - 7200000L,
                notes = "Pre-survey inspection. Awaiting site clearance."
            )
        )
    )

    override fun getProjects(): Flow<List<Project>> = _projects.asStateFlow()

    override suspend fun getProjectById(id: String): Project? = mutex.withLock {
        _projects.value.find { it.id == id }
    }

    override suspend fun createProject(project: Project): Project = mutex.withLock {
        SmartViewLogger.i("REPOSITORY", "Creating project: ${project.name}")
        val updated = _projects.value + project
        _projects.value = updated
        project
    }

    override suspend fun updateProject(project: Project): Boolean = mutex.withLock {
        val current = _projects.value
        val index = current.indexOfFirst { it.id == project.id }
        if (index != -1) {
            val updated = current.toMutableList().apply { set(index, project) }
            _projects.value = updated
            true
        } else {
            false
        }
    }

    override suspend fun deleteProject(id: String): Boolean = mutex.withLock {
        val current = _projects.value
        val filtered = current.filterNot { it.id == id }
        if (filtered.size != current.size) {
            _projects.value = filtered
            true
        } else {
            false
        }
    }

    override fun getSurveysForProject(projectId: String): Flow<List<Survey>> {
        return _surveys.map { list -> list.filter { it.projectId == projectId } }
    }

    override suspend fun getSurveyById(id: String): Survey? = mutex.withLock {
        _surveys.value.find { it.id == id }
    }

    override suspend fun createSurvey(survey: Survey): Survey = mutex.withLock {
        SmartViewLogger.i("REPOSITORY", "Creating survey: ${survey.surveyTitle} (${survey.surveyCode})")
        val updatedSurveys = _surveys.value + survey
        _surveys.value = updatedSurveys

        // Increment project survey count
        val currentProjects = _projects.value
        val pIndex = currentProjects.indexOfFirst { it.id == survey.projectId }
        if (pIndex != -1) {
            val p = currentProjects[pIndex]
            val updatedProj = p.copy(surveyCount = p.surveyCount + 1)
            val updatedList = currentProjects.toMutableList().apply { set(pIndex, updatedProj) }
            _projects.value = updatedList
        }
        survey
    }

    override suspend fun updateSurvey(survey: Survey): Boolean = mutex.withLock {
        val current = _surveys.value
        val index = current.indexOfFirst { it.id == survey.id }
        if (index != -1) {
            val updated = current.toMutableList().apply { set(index, survey) }
            _surveys.value = updated
            true
        } else {
            false
        }
    }

    override suspend fun deleteSurvey(id: String): Boolean = mutex.withLock {
        val current = _surveys.value
        val filtered = current.filterNot { it.id == id }
        if (filtered.size != current.size) {
            _surveys.value = filtered
            true
        } else {
            false
        }
    }
}
