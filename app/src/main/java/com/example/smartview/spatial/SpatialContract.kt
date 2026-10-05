package com.example.smartview.spatial

import kotlinx.coroutines.flow.StateFlow

/**
 * ARCHITECTURAL BOUNDARY: Spatial & AR Subsystem
 *
 * Target Implementation: SV-003+
 *
 * Responsibilities:
 * - ARCore session management & lifecycle
 * - Visual Inertial Odometry (VIO) tracking
 * - Real-world plane detection (floors, walls, ceilings)
 * - Spatial anchor placement and persistence
 */
interface SpatialSessionManager {
    val trackingState: StateFlow<SpatialTrackingState>

    suspend fun resumeSession()
    suspend fun pauseSession()
    suspend fun resetTracking()
}

enum class SpatialTrackingState {
    PAUSED,
    INITIALIZING,
    TRACKING_ADEQUATE,
    TRACKING_LIMITED_MOTION,
    TRACKING_LIMITED_LIGHTING,
    TRACKING_LOST
}
