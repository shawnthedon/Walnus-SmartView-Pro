package com.example.smartview.data.repository

import com.example.smartview.domain.model.Project
import kotlinx.coroutines.flow.Flow

/**
 * Data abstraction for construction project records.
 */
interface ProjectRepository {
    fun getProjects(): Flow<List<Project>>
    suspend fun getProjectById(id: String): Project?
    suspend fun createProject(project: Project): Project
    suspend fun updateProject(project: Project): Boolean
    suspend fun deleteProject(id: String): Boolean
}
