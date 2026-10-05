package com.example.smartview.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Top-level navigation destinations for SmartView Pro.
 */
sealed class SmartViewDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    data object Home : SmartViewDestination(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_item_home"
    )

    data object Projects : SmartViewDestination(
        route = "projects",
        title = "Projects",
        selectedIcon = Icons.Filled.Business,
        unselectedIcon = Icons.Outlined.Business,
        testTag = "nav_item_projects"
    )

    data object Survey : SmartViewDestination(
        route = "survey",
        title = "Survey",
        selectedIcon = Icons.Filled.CameraAlt,
        unselectedIcon = Icons.Outlined.CameraAlt,
        testTag = "nav_item_survey"
    )

    data object Settings : SmartViewDestination(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_item_settings"
    )

    companion object {
        val topLevelDestinations: List<SmartViewDestination> = listOf(Home, Projects, Survey, Settings)

        fun fromRoute(route: String?): SmartViewDestination {
            return topLevelDestinations.find { it.route == route } ?: Home
        }
    }
}
