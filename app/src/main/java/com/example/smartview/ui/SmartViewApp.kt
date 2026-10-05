package com.example.smartview.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartview.SmartViewApplication
import com.example.smartview.core.di.DefaultSmartViewContainer
import com.example.smartview.core.di.SmartViewContainer
import com.example.smartview.core.log.SmartViewLogger
import com.example.smartview.ui.navigation.SmartViewDestination
import com.example.smartview.ui.screens.camera.CameraCaptureScreen
import com.example.smartview.ui.screens.home.HomeScreen
import com.example.smartview.ui.screens.home.HomeViewModel
import com.example.smartview.ui.screens.projects.ProjectsScreen
import com.example.smartview.ui.screens.projects.ProjectsViewModel
import com.example.smartview.ui.screens.settings.SettingsScreen
import com.example.smartview.ui.screens.settings.SettingsViewModel
import com.example.smartview.ui.screens.survey.SurveyPlaceholderScreen
import com.example.smartview.ui.screens.survey.SurveyViewModel
import com.example.smartview.ui.theme.PrecisionSky
import com.example.smartview.ui.theme.PrecisionSkyLight

@Composable
fun SmartViewApp(
    container: SmartViewContainer = remember {
        val context = null as android.content.Context?
        DefaultSmartViewContainer(context)
    },
    navController: NavHostController = rememberNavController()
) {
    val viewModelFactory = remember(container) { SmartViewViewModelFactory(container) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isCameraRoute = currentRoute?.startsWith("camera") == true

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("smartview_app_root"),
        bottomBar = {
            if (!isCameraRoute) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    SmartViewDestination.topLevelDestinations.forEach { destination ->
                        val isSelected = currentRoute == destination.route

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != destination.route) {
                                    SmartViewLogger.logNavigation(currentRoute, destination.route)
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.title
                                )
                            },
                            label = {
                                Text(
                                    text = destination.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrecisionSkyLight,
                                selectedTextColor = PrecisionSkyLight,
                                indicatorColor = PrecisionSky.copy(alpha = 0.2f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(destination.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SmartViewDestination.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(SmartViewDestination.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                HomeScreen(
                    viewModel = homeViewModel,
                    onStartSurveyClick = {
                        SmartViewLogger.logNavigation(currentRoute, SmartViewDestination.Survey.route)
                        navController.navigate(SmartViewDestination.Survey.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToProjects = {
                        SmartViewLogger.logNavigation(currentRoute, SmartViewDestination.Projects.route)
                        navController.navigate(SmartViewDestination.Projects.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSettings = {
                        SmartViewLogger.logNavigation(currentRoute, SmartViewDestination.Settings.route)
                        navController.navigate(SmartViewDestination.Settings.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(SmartViewDestination.Projects.route) {
                val projectsViewModel: ProjectsViewModel = viewModel(factory = viewModelFactory)
                ProjectsScreen(viewModel = projectsViewModel)
            }

            composable(SmartViewDestination.Survey.route) {
                val surveyViewModel: SurveyViewModel = viewModel(factory = viewModelFactory)
                SurveyPlaceholderScreen(
                    viewModel = surveyViewModel,
                    onNavigateToHome = {
                        navController.navigate(SmartViewDestination.Home.route) {
                            popUpTo(SmartViewDestination.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToProjects = {
                        navController.navigate(SmartViewDestination.Projects.route) {
                            launchSingleTop = true
                        }
                    },
                    onLaunchCamera = { targetSurveyId ->
                        SmartViewLogger.logNavigation(currentRoute, "camera/$targetSurveyId")
                        navController.navigate("camera/$targetSurveyId")
                    }
                )
            }

            composable(
                route = "camera/{surveyId}",
                arguments = listOf(
                    navArgument("surveyId") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val targetSurveyId = backStackEntry.arguments?.getString("surveyId").orEmpty()
                val cameraViewModel = remember(targetSurveyId) {
                    viewModelFactory.createCameraViewModel(targetSurveyId)
                }
                CameraCaptureScreen(
                    viewModel = cameraViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(SmartViewDestination.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
