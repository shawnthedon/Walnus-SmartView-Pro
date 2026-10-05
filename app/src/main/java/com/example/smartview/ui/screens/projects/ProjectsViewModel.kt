package com.example.smartview.ui.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartview.core.dispatchers.DispatcherProvider
import com.example.smartview.core.log.SmartViewLogger
import com.example.smartview.data.repository.ProjectRepository
import com.example.smartview.domain.model.Project
import com.example.smartview.domain.model.ProjectStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

data class ProjectsUiState(
    val isLoading: Boolean = false,
    val projects: List<Project> = emptyList(),
    val isCreateDialogOpen: Boolean = false,
    val errorMessage: String? = null
)

class ProjectsViewModel(
    private val projectRepository: ProjectRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectsUiState(isLoading = true))
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch(dispatchers.io) {
            SmartViewLogger.d("PROJECTS", "Subscribing to project records flow")
            projectRepository.getProjects()
                .catch { ex ->
                    SmartViewLogger.e("PROJECTS", "Failed to retrieve projects", ex)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = ex.message
                    )
                }
                .collect { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        projects = list,
                        errorMessage = null
                    )
                }
        }
    }

    fun openCreateProjectDialog() {
        _uiState.value = _uiState.value.copy(isCreateDialogOpen = true)
    }

    fun closeCreateProjectDialog() {
        _uiState.value = _uiState.value.copy(isCreateDialogOpen = false)
    }

    fun createProject(name: String, clientName: String, siteAddress: String) {
        viewModelScope.launch(dispatchers.io) {
            val newProject = Project(
                id = "proj-${UUID.randomUUID().toString().take(8)}",
                name = name.ifBlank { "Untitled Project" },
                clientName = clientName.ifBlank { "General Client" },
                siteAddress = siteAddress.ifBlank { "Unassigned Site" },
                status = ProjectStatus.ACTIVE,
                createdAtEpochMs = System.currentTimeMillis(),
                surveyCount = 0
            )
            val created = projectRepository.createProject(newProject)
            _uiState.value = _uiState.value.copy(
                isCreateDialogOpen = false,
                projects = if (_uiState.value.projects.none { it.id == created.id }) {
                    listOf(created) + _uiState.value.projects
                } else {
                    _uiState.value.projects
                }
            )
        }
    }
}
