package com.example.smartview.camera

import com.example.smartview.domain.model.SurveyCapture

/**
 * Coherent state representation for the SmartView camera subsystem.
 * Avoids contradictory boolean flags.
 */
sealed interface CameraUiState {
    object Idle : CameraUiState
    object Initializing : CameraUiState
    object Ready : CameraUiState
    object Capturing : CameraUiState
    data class CaptureSuccess(val capture: SurveyCapture) : CameraUiState
    data class CaptureError(val message: String) : CameraUiState
    object PermissionRequired : CameraUiState
    data class CameraUnavailable(val reason: String) : CameraUiState
}
