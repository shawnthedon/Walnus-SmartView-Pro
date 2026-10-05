package com.example.smartview.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.smartview.data.local.dao.MeasurementDao
import com.example.smartview.data.local.dao.ProjectDao
import com.example.smartview.data.local.dao.SurveyDao
import com.example.smartview.data.local.entity.MeasurementEntity
import com.example.smartview.data.local.entity.ProjectEntity
import com.example.smartview.data.local.entity.SurveyEntity
import com.example.smartview.domain.model.MeasurementProvenance
import com.example.smartview.domain.model.MeasurementType
import com.example.smartview.domain.model.ProjectStatus
import com.example.smartview.domain.model.SurveyStatus
import kotlinx.coroutines.flow.first
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
class RoomDatabaseTest {

    private lateinit var database: SmartViewDatabase
    private lateinit var projectDao: ProjectDao
    private lateinit var surveyDao: SurveyDao
    private lateinit var measurementDao: MeasurementDao
    private lateinit var surveyCaptureDao: com.example.smartview.data.local.dao.SurveyCaptureDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SmartViewDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        projectDao = database.projectDao()
        surveyDao = database.surveyDao()
        measurementDao = database.measurementDao()
        surveyCaptureDao = database.surveyCaptureDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun emptyDatabase_returnsEmptyLists() = runTest {
        val projects = projectDao.getProjects().first()
        val surveys = surveyDao.getAllSurveys().first()
        val count = projectDao.getProjectCount()

        assertTrue(projects.isEmpty())
        assertTrue(surveys.isEmpty())
        assertEquals(0, count)
    }

    @Test
    fun projectDao_insertAndRetrieveById() = runTest {
        val project = ProjectEntity(
            id = "proj-test-1",
            name = "Marine Terminal Survey",
            clientName = "Port Authority",
            siteAddress = "Berth 18",
            status = ProjectStatus.ACTIVE,
            createdAtEpochMs = 1700000000000L,
            surveyCount = 0
        )

        projectDao.insertProject(project)

        val retrieved = projectDao.getProjectById("proj-test-1")
        assertNotNull(retrieved)
        assertEquals("Marine Terminal Survey", retrieved?.name)
        assertEquals("Port Authority", retrieved?.clientName)
        assertEquals(ProjectStatus.ACTIVE, retrieved?.status)
    }

    @Test
    fun projectDao_updateProject() = runTest {
        val project = ProjectEntity(
            id = "proj-test-2",
            name = "Initial Name",
            clientName = "Client A",
            siteAddress = "Address 1",
            status = ProjectStatus.ACTIVE,
            createdAtEpochMs = 1700000000000L,
            surveyCount = 0
        )
        projectDao.insertProject(project)

        val updated = project.copy(
            name = "Updated Name",
            status = ProjectStatus.COMPLETED
        )
        val rows = projectDao.updateProject(updated)

        assertEquals(1, rows)
        val result = projectDao.getProjectById("proj-test-2")
        assertEquals("Updated Name", result?.name)
        assertEquals(ProjectStatus.COMPLETED, result?.status)
    }

    @Test
    fun projectDao_deleteProjectById() = runTest {
        val project = ProjectEntity(
            id = "proj-test-3",
            name = "To Delete",
            clientName = "Client B",
            siteAddress = "Address 2",
            status = ProjectStatus.ACTIVE,
            createdAtEpochMs = 1700000000000L,
            surveyCount = 0
        )
        projectDao.insertProject(project)

        val rows = projectDao.deleteProjectById("proj-test-3")
        assertEquals(1, rows)

        val result = projectDao.getProjectById("proj-test-3")
        assertNull(result)
    }

    @Test
    fun relationships_surveyBelongsToProject_andMeasurementsBelongToSurvey() = runTest {
        val project = ProjectEntity(
            id = "proj-rel-1",
            name = "Main Complex",
            clientName = "Walnus",
            siteAddress = "Site 1",
            status = ProjectStatus.ACTIVE,
            createdAtEpochMs = 1700000000000L
        )
        projectDao.insertProject(project)

        val survey = SurveyEntity(
            id = "srv-rel-1",
            projectId = "proj-rel-1",
            surveyTitle = "Roof Waterproofing Assessment",
            surveyCode = "SV-R1",
            status = SurveyStatus.IN_PROGRESS,
            timestampEpochMs = 1700000001000L,
            notes = "Test notes"
        )
        surveyDao.insertSurvey(survey)

        val measurement = MeasurementEntity(
            id = "meas-rel-1",
            surveyId = "srv-rel-1",
            label = "Roof Parapet Perimeter",
            type = MeasurementType.LINEAR_DISTANCE,
            value = 88.4,
            unit = "m",
            provenance = MeasurementProvenance.MEASURED,
            timestampEpochMs = 1700000002000L
        )
        measurementDao.insertMeasurement(measurement)

        val projectSurveys = surveyDao.getSurveysForProject("proj-rel-1").first()
        assertEquals(1, projectSurveys.size)
        assertEquals("srv-rel-1", projectSurveys.first().id)

        val surveyMeasurements = measurementDao.getMeasurementsForSurvey("srv-rel-1").first()
        assertEquals(1, surveyMeasurements.size)
        assertEquals("meas-rel-1", surveyMeasurements.first().id)
        assertEquals(MeasurementProvenance.MEASURED, surveyMeasurements.first().provenance)
    }

    @Test
    fun cascadeDeletion_deletingProjectRemovesSurveysAndMeasurements() = runTest {
        val project = ProjectEntity(
            id = "proj-cascade",
            name = "Demolition Zone",
            clientName = "Civil Works",
            siteAddress = "Block 9",
            status = ProjectStatus.ACTIVE,
            createdAtEpochMs = 1700000000000L
        )
        projectDao.insertProject(project)

        val survey = SurveyEntity(
            id = "srv-cascade",
            projectId = "proj-cascade",
            surveyTitle = "Slab Survey",
            surveyCode = "SV-C1",
            status = SurveyStatus.DRAFT,
            timestampEpochMs = 1700000001000L
        )
        surveyDao.insertSurvey(survey)

        val measurement = MeasurementEntity(
            id = "meas-cascade",
            surveyId = "srv-cascade",
            label = "Slab Thickness",
            type = MeasurementType.LINEAR_DISTANCE,
            value = 0.25,
            unit = "m",
            provenance = MeasurementProvenance.MEASURED,
            timestampEpochMs = 1700000002000L
        )
        measurementDao.insertMeasurement(measurement)

        // Delete parent project
        projectDao.deleteProjectById("proj-cascade")

        // Verify children were cascaded
        assertNull(projectDao.getProjectById("proj-cascade"))
        assertNull(surveyDao.getSurveyById("srv-cascade"))
        assertNull(measurementDao.getMeasurementById("meas-cascade"))
    }

    @Test
    fun provenance_allProvenanceValuesPersistAndRestoreAccurately() = runTest {
        val project = ProjectEntity(
            id = "proj-prov",
            name = "Provenance Test",
            clientName = "QA",
            siteAddress = "Lab",
            status = ProjectStatus.ACTIVE,
            createdAtEpochMs = 1700000000000L
        )
        projectDao.insertProject(project)

        val survey = SurveyEntity(
            id = "srv-prov",
            projectId = "proj-prov",
            surveyTitle = "Provenance Survey",
            surveyCode = "SV-P1",
            status = SurveyStatus.DRAFT,
            timestampEpochMs = 1700000001000L
        )
        surveyDao.insertSurvey(survey)

        MeasurementProvenance.entries.forEachIndexed { index, provenance ->
            val measurement = MeasurementEntity(
                id = "meas-prov-$index",
                surveyId = "srv-prov",
                label = "Dimension $index",
                type = MeasurementType.SURFACE_AREA,
                value = 100.0 + index,
                unit = "m²",
                provenance = provenance,
                timestampEpochMs = 1700000002000L + index
            )
            measurementDao.insertMeasurement(measurement)

            val retrieved = measurementDao.getMeasurementById("meas-prov-$index")
            assertNotNull(retrieved)
            assertEquals(provenance, retrieved?.provenance)
        }
    }

    @Test
    fun surveyCaptureDao_insertAndRetrieveAndCascadeDelete() = runTest {
        val project = ProjectEntity(
            id = "proj-cap-1",
            name = "Facade Project",
            clientName = "Walnus Client",
            siteAddress = "Site 10",
            status = ProjectStatus.ACTIVE,
            createdAtEpochMs = 1700000000000L
        )
        projectDao.insertProject(project)

        val survey = SurveyEntity(
            id = "srv-cap-1",
            projectId = "proj-cap-1",
            surveyTitle = "Exterior Photography",
            surveyCode = "SV-CAP1",
            status = SurveyStatus.IN_PROGRESS,
            timestampEpochMs = 1700000001000L
        )
        surveyDao.insertSurvey(survey)

        val capture = com.example.smartview.data.local.entity.SurveyCaptureEntity(
            id = "cap-test-1",
            surveyId = "srv-cap-1",
            localPath = "/data/user/0/com.example/files/survey_captures/test.jpg",
            captureType = com.example.smartview.domain.model.CaptureType.SURVEY_STILL,
            width = 1920,
            height = 1080,
            createdAtEpochMs = 1700000002000L
        )
        surveyCaptureDao.insertCapture(capture)

        val retrieved = surveyCaptureDao.getCaptureById("cap-test-1")
        assertNotNull(retrieved)
        assertEquals("cap-test-1", retrieved?.id)
        assertEquals(1920, retrieved?.width)
        assertEquals(1080, retrieved?.height)
        assertEquals(com.example.smartview.domain.model.CaptureType.SURVEY_STILL, retrieved?.captureType)

        val surveyCaptures = surveyCaptureDao.getCapturesForSurvey("srv-cap-1").first()
        assertEquals(1, surveyCaptures.size)

        // Delete parent survey -> capture metadata must be cascade deleted by SQLite
        surveyDao.deleteSurveyById("srv-cap-1")
        assertNull(surveyCaptureDao.getCaptureById("cap-test-1"))
    }

    @Test
    fun migration_1_to_2_createsSurveyCapturesTableAndExecutesSuccessfully() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbHelper = androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory()
            .create(
                androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                    .name("migration_test.db")
                    .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            db.execSQL(
                                """
                                CREATE TABLE IF NOT EXISTS `projects` (
                                    `id` TEXT NOT NULL,
                                    `name` TEXT NOT NULL,
                                    `client_name` TEXT NOT NULL,
                                    `site_address` TEXT NOT NULL,
                                    `status` TEXT NOT NULL,
                                    `created_at_epoch_ms` INTEGER NOT NULL,
                                    `survey_count` INTEGER NOT NULL,
                                    PRIMARY KEY(`id`)
                                )
                                """.trimIndent()
                            )
                            db.execSQL(
                                """
                                CREATE TABLE IF NOT EXISTS `surveys` (
                                    `id` TEXT NOT NULL,
                                    `project_id` TEXT NOT NULL,
                                    `survey_title` TEXT NOT NULL,
                                    `survey_code` TEXT NOT NULL,
                                    `status` TEXT NOT NULL,
                                    `timestamp_epoch_ms` INTEGER NOT NULL,
                                    `notes` TEXT NOT NULL,
                                    PRIMARY KEY(`id`)
                                )
                                """.trimIndent()
                            )
                        }

                        override fun onUpgrade(
                            db: androidx.sqlite.db.SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int
                        ) {}
                    })
                    .build()
            )

        val db = dbHelper.writableDatabase

        // Apply Room migration 1 -> 2
        SmartViewDatabase.MIGRATION_1_2.migrate(db)

        // Verify survey_captures table exists and allows insertion
        db.execSQL(
            """
            INSERT INTO `surveys` (`id`, `project_id`, `survey_title`, `survey_code`, `status`, `timestamp_epoch_ms`, `notes`)
            VALUES ('s-mig-1', 'p1', 'Title', 'Code', 'DRAFT', 1000, 'Notes')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `survey_captures` (`id`, `survey_id`, `local_path`, `capture_type`, `width`, `height`, `created_at_epoch_ms`)
            VALUES ('c-mig-1', 's-mig-1', '/path/img.jpg', 'SURVEY_STILL', 1920, 1080, 2000)
            """.trimIndent()
        )

        val cursor = db.query("SELECT COUNT(*) FROM `survey_captures` WHERE `id` = 'c-mig-1'")
        assertTrue(cursor.moveToFirst())
        assertEquals(1, cursor.getInt(0))
        cursor.close()
        db.close()
    }
}
