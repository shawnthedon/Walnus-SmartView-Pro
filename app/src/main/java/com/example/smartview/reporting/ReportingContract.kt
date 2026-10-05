package com.example.smartview.reporting

import com.example.smartview.domain.model.Survey
import java.io.File

/**
 * ARCHITECTURAL BOUNDARY: Reporting & Quotation Subsystem
 *
 * Target Implementation: Future SmartView Task
 *
 * Responsibilities:
 * - PDF Site Survey report generation
 * - Walnus SmartCoat™ quotation and material specification exports
 * - CAD/BIM compatible export formats (DXF, CSV, JSON)
 */
interface ReportGenerator {
    suspend fun generateSurveyPdf(survey: Survey, destinationFile: File): Boolean
    suspend fun exportSurveySummary(survey: Survey): String
}
