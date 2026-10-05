package com.example.smartview.ui.screens.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartview.core.dispatchers.DispatcherProvider
import com.example.smartview.core.log.SmartViewLogger
import com.example.smartview.data.repository.SurveyCaptureRepository
import com.example.smartview.data.repository.SurveyRepository
import com.example.smartview.domain.model.Survey
import com.example.smartview.domain.model.SurveyCapture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class SurveyScreenUiState(
    val isLoading: Boolean = true,
    val activeSurvey: Survey? = null,
    val captures: List<SurveyCapture> = emptyList(),
    val errorMessage: String? = null
)

class SurveyViewModel(
    private val surveyRepository: SurveyRepository,
    private val captureRepository: SurveyCaptureRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(SurveyScreenUiState())
    val uiState: StateFlow<SurveyScreenUiState> = _uiState.asStateFlow()

    init {
        loadDefaultSurvey()
    }

    fun loadDefaultSurvey() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Load primary survey session (e.g. srv-101 from default seed)
            val survey = surveyRepository.getSurveyById("srv-101")
            if (survey != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activeSurvey = survey
                )
                observeCapturesForSurvey(survey.id)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No active survey session found"
                )
            }
        }
    }

    fun selectSurvey(surveyId: String) {
        viewModelScope.launch(dispatchers.io) {
            val survey = surveyRepository.getSurveyById(surveyId)
            if (survey != null) {
                _uiState.value = _uiState.value.copy(activeSurvey = survey)
                observeCapturesForSurvey(survey.id)
            }
        }
    }

    private fun observeCapturesForSurvey(surveyId: String) {
        viewModelScope.launch(dispatchers.io) {
            captureRepository.getCapturesForSurvey(surveyId)
                .catch { ex ->
                    SmartViewLogger.e("SURVEY", "Failed to retrieve survey captures", ex)
                }
                .collect { list ->
                    _uiState.value = _uiState.value.copy(captures = list)
                }
        }
    }

    fun deleteCapture(captureId: String) {
        viewModelScope.launch(dispatchers.io) {
            captureRepository.deleteCapture(captureId)
        }
    }
}
