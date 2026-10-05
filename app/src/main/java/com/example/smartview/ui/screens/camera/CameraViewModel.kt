package com.example.smartview.ui.screens.camera

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartview.camera.CameraCaptureService
import com.example.smartview.camera.CameraUiState
import com.example.smartview.core.dispatchers.DispatcherProvider
import com.example.smartview.core.log.SmartViewLogger
import com.example.smartview.data.repository.SurveyCaptureRepository
import com.example.smartview.data.repository.SurveyRepository
import com.example.smartview.domain.model.CaptureType
import com.example.smartview.domain.model.Survey
import com.example.smartview.domain.model.SurveyCapture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CameraScreenUiState(
    val surveyId: String = "",
    val surveyCode: String = "",
    val surveyTitle: String = "",
    val cameraState: CameraUiState = CameraUiState.Idle,
    val lastCapture: SurveyCapture? = null,
    val isReviewVisible: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val isInvalidSurvey: Boolean = false,
    val errorMessage: String? = null
)

class CameraViewModel(
    val surveyId: String,
    private val cameraService: CameraCaptureService,
    private val surveyRepository: SurveyRepository,
    private val surveyCaptureRepository: SurveyCaptureRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CameraScreenUiState(
            surveyId = surveyId,
            isInvalidSurvey = surveyId.isBlank()
        )
    )
    val uiState: StateFlow<CameraScreenUiState> = _uiState.asStateFlow()

    init {
        if (surveyId.isBlank()) {
            SmartViewLogger.e("CAMERA", "Camera launched with invalid/empty survey ID!")
            _uiState.value = _uiState.value.copy(
                isInvalidSurvey = true,
                errorMessage = "Cannot launch camera without an active site survey."
            )
        } else {
            loadSurveyContext()
            observeCameraServiceState()
        }
    }

    private fun loadSurveyContext() {
        viewModelScope.launch(dispatchers.io) {
            val survey = surveyRepository.getSurveyById(surveyId)
            if (survey != null) {
                _uiState.value = _uiState.value.copy(
                    surveyCode = survey.surveyCode,
                    surveyTitle = survey.surveyTitle
                )
            } else {
                SmartViewLogger.e("CAMERA", "Active survey $surveyId not found in repository")
                _uiState.value = _uiState.value.copy(
                    isInvalidSurvey = true,
                    errorMessage = "Survey session not found. Please select an active survey."
                )
            }
        }
    }

    private fun observeCameraServiceState() {
        viewModelScope.launch(dispatchers.main) {
            cameraService.cameraState.collect { state ->
                _uiState.value = _uiState.value.copy(cameraState = state)
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        SmartViewLogger.i("CAMERA", "Camera permission result: isGranted=$isGranted")
        _uiState.value = _uiState.value.copy(isPermissionGranted = isGranted)
        cameraService.setPermissionGranted(isGranted)
    }

    fun bindCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        if (_uiState.value.isInvalidSurvey) return
        if (_uiState.value.isPermissionGranted) {
            cameraService.bindToLifecycle(lifecycleOwner, previewView)
        } else {
            cameraService.setPermissionGranted(false)
        }
    }

    fun unbindCamera() {
        cameraService.unbind()
    }

    fun capturePhoto(captureType: CaptureType = CaptureType.SURVEY_STILL) {
        if (_uiState.value.isInvalidSurvey) {
            _uiState.value = _uiState.value.copy(errorMessage = "Cannot capture photo: invalid survey context.")
            return
        }

        viewModelScope.launch(dispatchers.main) {
            val result = cameraService.takePhoto(surveyId, captureType)
            result.onSuccess { capture ->
                viewModelScope.launch(dispatchers.io) {
                    surveyCaptureRepository.saveCapture(capture)
                    _uiState.value = _uiState.value.copy(
                        lastCapture = capture,
                        isReviewVisible = true
                    )
                }
            }.onFailure { ex ->
                SmartViewLogger.e("CAMERA", "Photo capture failed", ex)
                _uiState.value = _uiState.value.copy(
                    errorMessage = ex.localizedMessage ?: "Failed to capture image"
                )
            }
        }
    }

    fun dismissReview() {
        _uiState.value = _uiState.value.copy(isReviewVisible = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        cameraService.unbind()
    }
}
