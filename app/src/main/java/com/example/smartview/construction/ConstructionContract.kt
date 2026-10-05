package com.example.smartview.construction

/**
 * ARCHITECTURAL BOUNDARY: Construction Calculation Subsystem
 *
 * Target Implementation: Future SmartView Task
 *
 * Responsibilities:
 * - SmartCoat™ paint and primer coverage algorithms
 * - Screeding, plastering, and leveling volume estimation
 * - Substrate preparation loss factor calculations
 * - Multi-coat material consumption modeling
 */
data class MaterialCoverageEstimate(
    val productFamily: String = "SmartCoat™",
    val materialCode: String,
    val surfaceAreaSqMeters: Double,
    val coatsRequired: Int,
    val requiredVolumeLiters: Double,
    val recommendedContainers: Int
)

interface ConstructionEstimator {
    fun estimatePaintCoverage(
        surfaceAreaSqMeters: Double,
        substrateType: String,
        productCode: String
    ): MaterialCoverageEstimate
}
