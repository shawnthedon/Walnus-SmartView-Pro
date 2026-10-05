package com.example.smartview.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.smartview.core.dispatchers.DispatcherProvider
import com.example.smartview.data.local.SmartViewDatabase
import com.example.smartview.domain.model.Measurement
import com.example.smartview.domain.model.MeasurementProvenance
import com.example.smartview.domain.model.MeasurementType
import com.example.smartview.domain.model.Project
import com.example.smartview.domain.model.ProjectStatus
import com.example.smartview.domain.model.Survey
import com.example.smartview.domain.model.SurveyStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomProjectRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testDispatcherProvider = object : DispatcherProvider {
        override val main: CoroutineDispatcher get() = Dispatchers.Main
        override val io: CoroutineDispatcher get() = testDispatcher
        override val default: CoroutineDispatcher get() = testDispatcher
        override val unconfined: CoroutineDispatcher get() = testDispatcher
    }

    private lateinit var database: SmartViewDatabase
    private lateinit var repository: RoomProjectRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SmartViewDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomProjectRepository(database, testDispatcherProvider)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun repository_emptyDatabase_returnsEmptyList() = runTest(testDispatcher) {
        val projects = repository.getProjects().first()
        assertTrue(projects.isEmpty())
    }

    @Test
    fun repository_createProject_andRetrieveAcrossReload() = runTest(testDispatcher) {
        val project = Project(
            id = "proj-reload-1",
            name = "Commercial Tower Coating",
            clientName = "Walnus Property Group",
            siteAddress = "Tower 1",
            status = ProjectStatus.ACTIVE,
            createdAtEpochMs = 1700000000000L
        )

        repository.createProject(project)

        // Simulate repository recreation (reloading from same underlying database)
        val secondRepository = RoomProjectRepository(database, testDispatcherProvider)
        val retrieved = secondRepository.getProjectById("proj-reload-1")

        assertNotNull("Project must survive repository reload", retrieved)
        assertEquals("Commercial Tower Coating", retrieved?.name)
        assertEquals("Walnus Property Group", retrieved?.clientName)
        assertEquals(ProjectStatus.ACTIVE, retrieved?.status)
    }

    @Test
    fun repository_createSurvey_andRetrieveAcrossReload() = runTest(testDispatcher) {
        val project = Project(
            id = "proj-srv-test",
            name = "Facade Renovation",
            clientName = "Metro Council",
            siteAddress = "Main St",
            status = ProjectStatus.ACTIVE
        )
        repository.createProject(project)

        val survey = Survey(
            id = "srv-test-1",
            projectId = "proj-srv-test",
            surveyTitle = "South Wall Elevation",
            surveyCode = "SV-S1",
            status = SurveyStatus.IN_PROGRESS,
            notes = "Surface requires SmartCoat primer"
        )
        repository.createSurvey(survey)

        // Verify project survey count was incremented
        val updatedProject = repository.getProjectById("proj-srv-test")
        assertEquals(1, updatedProject?.surveyCount)

        // Simulate reload
        val newRepo = RoomProjectRepository(database, testDispatcherProvider)
        val retrievedSurvey = newRepo.getSurveyById("srv-test-1")
        assertNotNull("Survey must survive reload", retrievedSurvey)
        assertEquals("South Wall Elevation", retrievedSurvey?.surveyTitle)
        assertEquals("SV-S1", retrievedSurvey?.surveyCode)

        val surveysList = newRepo.getSurveysForProject("proj-srv-test").first()
        assertEquals(1, surveysList.size)
    }

    @Test
    fun repository_recordMeasurement_preservesProvenanceAcrossReload() = runTest(testDispatcher) {
        val project = Project(id = "p1", name = "P1", clientName = "C1", siteAddress = "A1")
        repository.createProject(project)

        val survey = Survey(id = "s1", projectId = "p1", surveyTitle = "S1", surveyCode = "SC1")
        repository.createSurvey(survey)

        val measurement = Measurement(
            id = "m1",
            surveyId = "s1",
            label = "West Wall Area",
            type = MeasurementType.SURFACE_AREA,
            value = 150.75,
            unit = "m²",
            provenance = MeasurementProvenance.AI_SUGGESTED
        )
        repository.recordMeasurement(measurement)

        val newRepo = RoomProjectRepository(database, testDispatcherProvider)
        val retrieved = newRepo.getMeasurementById("m1")

        assertNotNull("Measurement must survive reload", retrieved)
        assertEquals("West Wall Area", retrieved?.label)
        assertEquals(150.75, retrieved?.value ?: 0.0, 0.001)
        assertEquals("m²", retrieved?.unit)
        assertEquals(MeasurementProvenance.AI_SUGGESTED, retrieved?.provenance)

        val measurements = newRepo.getMeasurementsForSurvey("s1").first()
        assertEquals(1, measurements.size)
    }

    @Test
    fun repository_updateAndDeleteOperations() = runTest(testDispatcher) {
        val project = Project(id = "p-crud", name = "Original", clientName = "C", siteAddress = "A")
        repository.createProject(project)

        val updated = project.copy(name = "Updated Title", status = ProjectStatus.COMPLETED)
        val updateResult = repository.updateProject(updated)
        assertTrue(updateResult)

        val reloaded = repository.getProjectById("p-crud")
        assertEquals("Updated Title", reloaded?.name)
        assertEquals(ProjectStatus.COMPLETED, reloaded?.status)

        val deleteResult = repository.deleteProject("p-crud")
        assertTrue(deleteResult)

        val deleted = repository.getProjectById("p-crud")
        assertNull(deleted)
    }

    @Test
    fun repository_nonExistentEntity_returnsNullGracefully() = runTest(testDispatcher) {
        val project = repository.getProjectById("non-existent-id")
        val survey = repository.getSurveyById("non-existent-id")
        val measurement = repository.getMeasurementById("non-existent-id")

        assertNull(project)
        assertNull(survey)
        assertNull(measurement)
    }

    @Test
    fun repository_surveyCaptures_saveAndRetrieveAcrossReload_andFileCleanup() = runTest(testDispatcher) {
        val project = Project(id = "p-cap", name = "Capture Proj", clientName = "Client", siteAddress = "Site")
        repository.createProject(project)

        val survey = Survey(id = "s-cap", projectId = "p-cap", surveyTitle = "Survey Photos", surveyCode = "SV-PH1")
        repository.createSurvey(survey)

        // Create a temporary file to test real file cleanup
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tempDir = java.io.File(context.filesDir, "test_captures").apply { mkdirs() }
        val testImageFile = java.io.File(tempDir, "sample_capture.jpg").apply {
            writeText("dummy image data for test")
        }
        assertTrue("Test image file must exist before capture save", testImageFile.exists())

        val capture = com.example.smartview.domain.model.SurveyCapture(
            id = "cap-repo-1",
            surveyId = "s-cap",
            localPath = testImageFile.absolutePath,
            captureType = com.example.smartview.domain.model.CaptureType.SURVEY_STILL,
            width = 3840,
            height = 2160
        )
        repository.saveCapture(capture)

        // Verify across reload
        val newRepo = RoomProjectRepository(database, testDispatcherProvider)
        val retrieved = newRepo.getCaptureById("cap-repo-1")
        assertNotNull("Capture must survive repository reload", retrieved)
        assertEquals("cap-repo-1", retrieved?.id)
        assertEquals(3840, retrieved?.width)
        assertEquals(2160, retrieved?.height)

        val surveyCaptures = newRepo.getCapturesForSurvey("s-cap").first()
        assertEquals(1, surveyCaptures.size)

        // Delete capture -> must delete Room record AND local physical file!
        val deleteSuccess = newRepo.deleteCapture("cap-repo-1")
        assertTrue(deleteSuccess)
        assertNull(newRepo.getCaptureById("cap-repo-1"))
        org.junit.Assert.assertFalse("Physical image file must be deleted upon capture delete", testImageFile.exists())
    }
}
