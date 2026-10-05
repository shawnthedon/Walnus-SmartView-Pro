package com.example.smartview.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.smartview.domain.model.Project
import com.example.smartview.ui.components.BadgeStyle
import com.example.smartview.ui.components.ProfessionalCard
import com.example.smartview.ui.components.SmartViewHeader
import com.example.smartview.ui.components.TechnicalStatusBadge
import com.example.smartview.ui.theme.ConstructionAmber
import com.example.smartview.ui.theme.PrecisionEmerald
import com.example.smartview.ui.theme.PrecisionSky
import com.example.smartview.ui.theme.PrecisionSkyLight

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartSurveyClick: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartViewHeader(
            statusBadgeText = "SV-003 CAMERA",
            statusBadgeStyle = BadgeStyle.READY
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Hero Title & Primary Action Area
            ProfessionalCard(
                shape = RoundedCornerShape(12.dp),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                borderColor = PrecisionSky.copy(alpha = 0.5f),
                borderWidth = 1.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FIELD INTELLIGENCE SHELL",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrecisionSkyLight,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(id = R.string.product_title),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(id = R.string.company_full_name),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TechnicalStatusBadge(
                        text = "READY",
                        style = BadgeStyle.READY
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Intelligent construction-site surveying foundation. Structured architecture ready for camera capture, ARCore spatial computing, and SmartCoat™ material estimations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action Button (Start Survey)
                Button(
                    onClick = onStartSurveyClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_survey_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrecisionSky,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.action_start_survey),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Access Navigation Area: Projects & Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Projects Quick Card
                ProfessionalCard(
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToProjects
                ) {
                    Column(modifier = Modifier.testTag("quick_action_projects")) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = stringResource(id = R.string.cd_nav_projects),
                                tint = PrecisionSkyLight,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "${uiState.totalProjectCount}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(id = R.string.nav_projects),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Site directories",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Settings Quick Card
                ProfessionalCard(
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSettings
                ) {
                    Column(modifier = Modifier.testTag("quick_action_settings")) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(id = R.string.cd_nav_settings),
                                tint = ConstructionAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "v1.0",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(id = R.string.nav_settings),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Units & config",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Architecture Foundation Status Card (Verifiable System Readiness)
            Text(
                text = stringResource(id = R.string.title_system_architecture),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            ProfessionalCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ArchitectureLayerItem(
                        layerName = "Presentation Layer (UI)",
                        description = "Jetpack Compose, Material 3, Clean Navigation Shell",
                        statusText = "VERIFIED",
                        isReady = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ArchitectureLayerItem(
                        layerName = "Domain Layer",
                        description = "Project, Survey & Measurement entity models",
                        statusText = "VERIFIED",
                        isReady = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ArchitectureLayerItem(
                        layerName = "Data Layer (Room Persistence)",
                        description = "Room SQLite database, DAOs, Entity mappers, offline-first",
                        statusText = "VERIFIED",
                        isReady = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ArchitectureLayerItem(
                        layerName = "Camera Subsystem (CameraX)",
                        description = "CameraX preview, image capture, local media pipeline",
                        statusText = "VERIFIED",
                        isReady = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Active Site Projects Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.title_active_projects),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedButton(
                    onClick = onNavigateToProjects,
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            } else {
                uiState.activeProjects.take(2).forEach { project ->
                    ProjectSummaryCard(
                        project = project,
                        onClick = onNavigateToProjects
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ArchitectureLayerItem(
    layerName: String,
    description: String,
    statusText: String,
    isReady: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.Layers,
            contentDescription = null,
            tint = if (isReady) PrecisionEmerald else ConstructionAmber,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = layerName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TechnicalStatusBadge(
            text = statusText,
            style = if (isReady) BadgeStyle.READY else BadgeStyle.PENDING_TASK
        )
    }
}

@Composable
private fun ProjectSummaryCard(
    project: Project,
    onClick: () -> Unit
) {
    ProfessionalCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${project.clientName} • ${project.siteAddress}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TechnicalStatusBadge(
                text = "${project.surveyCount} SURVEYS",
                style = BadgeStyle.ACTIVE
            )
        }
    }
}
