package com.example.smartview.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartview.core.dispatchers.DispatcherProvider
import com.example.smartview.core.log.SmartViewLogger
import com.example.smartview.data.repository.ProjectRepository
import com.example.smartview.domain.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val activeProjects: List<Project> = emptyList(),
    val totalProjectCount: Int = 0,
    val architectureVersion: String = "SV-001 Greenfield Foundation",
    val errorMessage: String? = null
)

class HomeViewModel(
    private val projectRepository: ProjectRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch(dispatchers.io) {
            SmartViewLogger.d("HOME", "Loading dashboard overview data")
            projectRepository.getProjects()
                .catch { ex ->
                    SmartViewLogger.e("HOME", "Error loading projects for home dashboard", ex)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = ex.message ?: "Failed to load project records"
                    )
                }
                .collect { projects ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        activeProjects = projects,
                        totalProjectCount = projects.size,
                        errorMessage = null
                    )
                }
        }
    }
}
