package com.example.smartview.data

import com.example.smartview.data.repository.InMemoryProjectRepository
import com.example.smartview.domain.model.Project
import com.example.smartview.domain.model.ProjectStatus
import com.example.smartview.domain.model.Survey
import com.example.smartview.domain.model.SurveyStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProjectRepositoryTest {

    private lateinit var repository: InMemoryProjectRepository

    @Before
    fun setUp() {
        repository = InMemoryProjectRepository()
    }

    @Test
    fun repository_initializesWithDefaultProjects() = runTest {
        val projects = repository.getProjects().first()
        assertTrue("Expected default projects to be populated", projects.isNotEmpty())
        assertTrue(projects.any { it.name.contains("Walnus HQ") })
    }

    @Test
    fun repository_createProject_persistsAndCanBeRetrieved() = runTest {
        val newProject = Project(
            id = "test-proj-01",
            name = "Harbor Bridge Recoating",
            clientName = "Civil Transport Authority",
            siteAddress = "Pier 9, Marine Way",
            status = ProjectStatus.ACTIVE
        )

        repository.createProject(newProject)

        val retrieved = repository.getProjectById("test-proj-01")
        assertNotNull("Project should be retrievable by ID", retrieved)
        assertEquals("Harbor Bridge Recoating", retrieved?.name)
        assertEquals("Civil Transport Authority", retrieved?.clientName)
    }

    @Test
    fun repository_createSurvey_associatesWithProjectAndIncrementsCount() = runTest {
        val initialProjects = repository.getProjects().first()
        val targetProject = initialProjects.first()
        val initialSurveyCount = targetProject.surveyCount

        val survey = Survey(
            id = "test-srv-01",
            projectId = targetProject.id,
            surveyTitle = "Exterior Deck Inspection",
            surveyCode = "SV-TEST-001",
            status = SurveyStatus.IN_PROGRESS
        )

        repository.createSurvey(survey)

        val projectSurveys = repository.getSurveysForProject(targetProject.id).first()
        assertTrue(projectSurveys.any { it.id == "test-srv-01" })

        val updatedProject = repository.getProjectById(targetProject.id)
        assertEquals(initialSurveyCount + 1, updatedProject?.surveyCount)
    }
}
