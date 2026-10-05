package com.example.smartview.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.smartview.core.di.DefaultSmartViewContainer
import com.example.smartview.ui.theme.SmartViewTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SmartViewRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun smartViewApp_launchesAndDisplaysShellWithPrimaryActions() {
        val container = DefaultSmartViewContainer()

        composeTestRule.setContent {
            SmartViewTheme {
                SmartViewApp(container = container)
            }
        }

        composeTestRule.waitForIdle()

        // Verify root container and header are visible
        composeTestRule.onNodeWithTag("smartview_app_root").assertIsDisplayed()
        composeTestRule.onNodeWithTag("header_product_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("header_company_subtitle").assertIsDisplayed()

        // Verify primary Start Survey action
        composeTestRule.onNodeWithTag("start_survey_button").assertIsDisplayed()

        // Verify bottom navigation bar exists
        composeTestRule.onNodeWithTag("bottom_navigation_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_item_home").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_item_projects").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_item_survey").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_item_settings").assertIsDisplayed()
    }

    @Test
    fun smartViewApp_navigation_projectsAndSurveyAndSettings() {
        val container = DefaultSmartViewContainer()

        composeTestRule.setContent {
            SmartViewTheme {
                SmartViewApp(container = container)
            }
        }

        composeTestRule.waitForIdle()

        // 1. Navigate to Projects
        composeTestRule.onNodeWithTag("nav_item_projects").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("projects_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("new_project_button").assertIsDisplayed()

        // 2. Navigate to Survey (verifying explicit SV-002 boundary notice)
        composeTestRule.onNodeWithTag("nav_item_survey").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("survey_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("survey_back_to_home_button").assertExists()

        // 3. Navigate to Settings
        composeTestRule.onNodeWithTag("nav_item_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("unit_metric_chip").assertExists()

        // 4. Return to Home and test "Start Survey" button
        composeTestRule.onNodeWithTag("nav_item_home").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("start_survey_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("survey_screen").assertIsDisplayed()
    }

    @Test
    fun smartViewApp_projectsScreen_displaysPersistentProjects() {
        kotlinx.coroutines.runBlocking {
            val container = DefaultSmartViewContainer()
            val newProj = com.example.smartview.domain.model.Project(
                id = "proj-persisted-ui",
                name = "Skyline Logistics Center",
                clientName = "Apex Cargo",
                siteAddress = "Terminal 4",
                status = com.example.smartview.domain.model.ProjectStatus.ACTIVE
            )
            container.projectRepository.createProject(newProj)

            composeTestRule.setContent {
                SmartViewTheme {
                    SmartViewApp(container = container)
                }
            }

            composeTestRule.waitForIdle()

            // 1. Navigate to Projects
            composeTestRule.onNodeWithTag("nav_item_projects").performClick()
            composeTestRule.waitForIdle()

            // 2. Verify created persistent project is rendered in list
            composeTestRule.onNodeWithText("Skyline Logistics Center").assertExists()
        }
    }

    @Test
    fun smartViewApp_surveyScreen_navigatesToCameraAndBack() {
        val container = DefaultSmartViewContainer()

        composeTestRule.setContent {
            SmartViewTheme {
                SmartViewApp(container = container)
            }
        }

        composeTestRule.waitForIdle()

        // 1. Navigate to Survey screen
        composeTestRule.onNodeWithTag("nav_item_survey").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("survey_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("launch_camera_button").assertExists()

        // 2. Launch Camera
        composeTestRule.onNodeWithTag("launch_camera_button").performClick()
        composeTestRule.waitForIdle()

        // 3. Verify Camera screen is displayed
        composeTestRule.onNodeWithTag("camera_capture_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("camera_back_button").assertIsDisplayed()

        // 4. Return to Survey screen
        composeTestRule.onNodeWithTag("camera_back_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("survey_screen").assertIsDisplayed()
    }
}
