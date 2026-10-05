package com.example.smartview.ai

/**
 * ARCHITECTURAL BOUNDARY: AI Intelligence Subsystem
 *
 * Target Implementation: Future SmartView Task
 *
 * Responsibilities:
 * - Substrate defect and crack classification
 * - Wall finish and material identification
 * - Construction anomaly detection
 * - Automated coating and application recommendations
 */
data class DefectClassification(
    val defectType: String,
    val confidence: Float,
    val suggestedTreatment: String
)

interface ProjectIntelligenceService {
    suspend fun analyzeSurfaceImage(imageBytes: ByteArray): List<DefectClassification>
}
