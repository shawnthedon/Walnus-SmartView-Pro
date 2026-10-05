package com.example.smartview.camera

import kotlinx.coroutines.flow.StateFlow

/**
 * ARCHITECTURAL BOUNDARY: Camera Subsystem
 *
 * Target Implementation: SV-002
 *
 * Responsibilities:
 * - Camera permission verification and lifecycle binding
 * - High-speed camera frame streaming for computer vision and AR
 * - High-resolution still capture for project documentation
 * - Hardware capability inspection (Flash, Depth, Ultra-wide)
 */
interface CameraController {
    val cameraState: StateFlow<CameraState>

    suspend fun startCapture()
    suspend fun stopCapture()
    suspend fun switchLens(lensFacing: CameraLensFacing)
}

enum class CameraState {
    UNINITIALIZED,
    INITIALIZING,
    READY,
    STREAMING,
    ERROR
}

enum class CameraLensFacing {
    BACK_WIDE,
    BACK_ULTRAWIDE,
    FRONT
}
