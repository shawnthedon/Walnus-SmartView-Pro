package com.example.smartview.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartview.core.log.SmartViewLogger
import com.example.smartview.data.local.converter.SmartViewTypeConverters
import com.example.smartview.data.local.dao.MeasurementDao
import com.example.smartview.data.local.dao.ProjectDao
import com.example.smartview.data.local.dao.SurveyCaptureDao
import com.example.smartview.data.local.dao.SurveyDao
import com.example.smartview.data.local.entity.MeasurementEntity
import com.example.smartview.data.local.entity.ProjectEntity
import com.example.smartview.data.local.entity.SurveyCaptureEntity
import com.example.smartview.data.local.entity.SurveyEntity
import com.example.smartview.domain.model.MeasurementProvenance
import com.example.smartview.domain.model.MeasurementType
import com.example.smartview.domain.model.ProjectStatus
import com.example.smartview.domain.model.SurveyStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Primary persistent Room Database for SmartView Pro™.
 *
 * Database Version: 2 (SV-003 Camera Capture & Local Media Pipeline)
 * Tables: projects, surveys, measurements, survey_captures
 */
@Database(
    entities = [
        ProjectEntity::class,
        SurveyEntity::class,
        MeasurementEntity::class,
        SurveyCaptureEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(SmartViewTypeConverters::class)
abstract class SmartViewDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun surveyDao(): SurveyDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun surveyCaptureDao(): SurveyCaptureDao

    companion object {
        const val DATABASE_NAME = "smartview_pro.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                SmartViewLogger.i("DATABASE", "Executing migration 1 -> 2: creating survey_captures table and index")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `survey_captures` (
                        `id` TEXT NOT NULL,
                        `survey_id` TEXT NOT NULL,
                        `local_path` TEXT NOT NULL,
                        `capture_type` TEXT NOT NULL,
                        `width` INTEGER,
                        `height` INTEGER,
                        `created_at_epoch_ms` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`survey_id`) REFERENCES `surveys`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_survey_captures_survey_id` ON `survey_captures` (`survey_id`)"
                )
            }
        }

        @Volatile
        private var INSTANCE: SmartViewDatabase? = null

        fun getInstance(context: Context, shouldPrepopulate: Boolean = true): SmartViewDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext, shouldPrepopulate).also {
                    INSTANCE = it
                }
            }
        }

        private fun buildDatabase(appContext: Context, shouldPrepopulate: Boolean): SmartViewDatabase {
            SmartViewLogger.i("DATABASE", "Building persistent SmartViewDatabase instance ($DATABASE_NAME)")
            val builder = Room.databaseBuilder(
                appContext,
                SmartViewDatabase::class.java,
                DATABASE_NAME
            ).addMigrations(MIGRATION_1_2)

            if (shouldPrepopulate) {
                builder.addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        SmartViewLogger.i("DATABASE", "SmartViewDatabase created on disk for first time. Pre-populating default site projects.")
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val database = getInstance(appContext, shouldPrepopulate = false)
                                prepopulateDatabase(database)
                            } catch (e: Exception) {
                                SmartViewLogger.e("DATABASE", "Failed to prepopulate database", e)
                            }
                        }
                    }
                })
            }

            return builder.build()
        }

        suspend fun prepopulateDatabase(database: SmartViewDatabase) {
            val projectDao = database.projectDao()
            val surveyDao = database.surveyDao()
            val measurementDao = database.measurementDao()

            if (projectDao.getProjectCount() == 0) {
                val p1 = ProjectEntity(
                    id = "proj-001",
                    name = "Walnus HQ Innovation Center",
                    clientName = "Walnus Global Ventures",
                    siteAddress = "Tower 4, Enterprise Blvd, Tech District",
                    status = ProjectStatus.ACTIVE,
                    createdAtEpochMs = System.currentTimeMillis() - 86400000L * 3,
                    surveyCount = 2
                )
                val p2 = ProjectEntity(
                    id = "proj-002",
                    name = "Apex Industrial Facility Sector 7",
                    clientName = "Apex Logistics International",
                    siteAddress = "Harbor Logistics Corridor, Berth 12",
                    status = ProjectStatus.SCHEDULED,
                    createdAtEpochMs = System.currentTimeMillis() - 86400000L * 7,
                    surveyCount = 1
                )
                val p3 = ProjectEntity(
                    id = "proj-003",
                    name = "Metropolitan Retail Atrium",
                    clientName = "Horizon Commercial Properties",
                    siteAddress = "400 Grand Avenue, Central Core",
                    status = ProjectStatus.ACTIVE,
                    createdAtEpochMs = System.currentTimeMillis() - 86400000L * 1,
                    surveyCount = 0
                )
                projectDao.insertProjects(listOf(p1, p2, p3))

                val s1 = SurveyEntity(
                    id = "srv-101",
                    projectId = "proj-001",
                    surveyTitle = "Exterior Facade & Substrate Assessment",
                    surveyCode = "SV-2026-081",
                    status = SurveyStatus.IN_PROGRESS,
                    timestampEpochMs = System.currentTimeMillis() - 3600000L * 4,
                    notes = "High-velocity wind exposure on North facade. SmartCoat™ primer test recommended."
                )
                val s2 = SurveyEntity(
                    id = "srv-102",
                    projectId = "proj-001",
                    surveyTitle = "Ground Floor Entrance Vestibule",
                    surveyCode = "SV-2026-082",
                    status = SurveyStatus.COMPLETED,
                    timestampEpochMs = System.currentTimeMillis() - 86400000L,
                    notes = "Epoxy screed evaluation. Leveling needed prior to topcoat."
                )
                val s3 = SurveyEntity(
                    id = "srv-103",
                    projectId = "proj-002",
                    surveyTitle = "High-Bay Warehouse Slab Survey",
                    surveyCode = "SV-2026-090",
                    status = SurveyStatus.DRAFT,
                    timestampEpochMs = System.currentTimeMillis() - 7200000L,
                    notes = "Pre-survey inspection. Awaiting site clearance."
                )
                surveyDao.insertSurveys(listOf(s1, s2, s3))

                val m1 = MeasurementEntity(
                    id = "meas-001",
                    surveyId = "srv-101",
                    label = "North Elevation Wall Height",
                    type = MeasurementType.LINEAR_DISTANCE,
                    value = 14.50,
                    unit = "m",
                    provenance = MeasurementProvenance.MEASURED,
                    timestampEpochMs = System.currentTimeMillis() - 3600000L * 3
                )
                val m2 = MeasurementEntity(
                    id = "meas-002",
                    surveyId = "srv-101",
                    label = "North Facade Surface Area",
                    type = MeasurementType.SURFACE_AREA,
                    value = 420.50,
                    unit = "m²",
                    provenance = MeasurementProvenance.ESTIMATED,
                    timestampEpochMs = System.currentTimeMillis() - 3600000L * 2
                )
                measurementDao.insertMeasurements(listOf(m1, m2))
            }
        }
    }
}
