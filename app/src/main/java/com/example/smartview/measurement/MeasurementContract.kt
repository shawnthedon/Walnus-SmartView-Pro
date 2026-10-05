package com.example.smartview.measurement

import com.example.smartview.domain.model.Measurement
import com.example.smartview.domain.model.MeasurementType

/**
 * ARCHITECTURAL BOUNDARY: Measurement Subsystem
 *
 * Target Implementation: Future SmartView Task
 *
 * Responsibilities:
 * - Real-time metric & imperial distance calculations
 * - Multi-point polyline and polygon surface area computation
 * - Volumetric calculation with confidence ratings
 * - Measurement tolerance and error bounds estimation
 */
interface MeasurementEngine {
    fun recordMeasurement(
        surveyId: String,
        type: MeasurementType,
        value: Double,
        unit: String
    ): Measurement

    fun convertUnits(value: Double, fromUnit: String, toUnit: String): Double
}
