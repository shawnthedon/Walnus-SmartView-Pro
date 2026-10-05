package com.example.smartview.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartview.camera.CameraCaptureService
import com.example.smartview.camera.CameraXCaptureService
import com.example.smartview.core.di.SmartViewContainer
import com.example.smartview.ui.screens.camera.CameraViewModel
import com.example.smartview.ui.screens.home.HomeViewModel
import com.example.smartview.ui.screens.projects.ProjectsViewModel
import com.example.smartview.ui.screens.settings.SettingsViewModel
import com.example.smartview.ui.screens.survey.SurveyViewModel

@Suppress("UNCHECKED_CAST")
class SmartViewViewModelFactory(
    private val container: SmartViewContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    projectRepository = container.projectRepository,
                    dispatchers = container.dispatchers
                ) as T
            }
            modelClass.isAssignableFrom(ProjectsViewModel::class.java) -> {
                ProjectsViewModel(
                    projectRepository = container.projectRepository,
                    dispatchers = container.dispatchers
                ) as T
            }
            modelClass.isAssignableFrom(SurveyViewModel::class.java) -> {
                SurveyViewModel(
                    surveyRepository = container.surveyRepository,
                    captureRepository = container.surveyCaptureRepository,
                    dispatchers = container.dispatchers
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    fun createCameraViewModel(
        surveyId: String,
        cameraService: CameraCaptureService = CameraXCaptureService(container.applicationContext)
    ): CameraViewModel {
        return CameraViewModel(
            surveyId = surveyId,
            cameraService = cameraService,
            surveyRepository = container.surveyRepository,
            surveyCaptureRepository = container.surveyCaptureRepository,
            dispatchers = container.dispatchers
        )
    }
}
