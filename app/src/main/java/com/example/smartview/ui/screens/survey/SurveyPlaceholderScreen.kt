package com.example.smartview.ui.screens.survey

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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.smartview.domain.model.SurveyCapture
import com.example.smartview.ui.components.BadgeStyle
import com.example.smartview.ui.components.ProfessionalCard
import com.example.smartview.ui.components.SmartViewHeader
import com.example.smartview.ui.components.TechnicalStatusBadge
import com.example.smartview.ui.theme.ConstructionAmber
import com.example.smartview.ui.theme.DarkSurfaceVariant
import com.example.smartview.ui.theme.PrecisionEmerald
import com.example.smartview.ui.theme.PrecisionSky
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SurveyPlaceholderScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onLaunchCamera: (surveyId: String) -> Unit = {},
    viewModel: SurveyViewModel? = null,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel?.uiState?.collectAsState()?.value ?: SurveyScreenUiState()
    val activeSurvey = uiState.activeSurvey
    val surveyId = activeSurvey?.id ?: "srv-101"

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("survey_screen")
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
            // Active Survey Context Card
            ProfessionalCard(
                shape = RoundedCornerShape(12.dp),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                borderColor = PrecisionSky.copy(alpha = 0.5f),
                borderWidth = 1.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        PrecisionSky.copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = PrecisionSky,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = activeSurvey?.surveyCode ?: "SV-2026-081",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = activeSurvey?.surveyTitle ?: "Exterior Facade & Substrate Assessment",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                        TechnicalStatusBadge(
                            text = "ACTIVE",
                            style = BadgeStyle.READY
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Site Notes: ${activeSurvey?.notes ?: "High-velocity wind exposure on North facade. SmartCoat™ primer test recommended."}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Launch Camera Action Button
                    Button(
                        onClick = { onLaunchCamera(surveyId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("launch_camera_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrecisionSky),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Capture Site Photo (CameraX)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Survey Photographic Captures Gallery Section
            Text(
                text = "Site Photographic Records (${uiState.captures.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.captures.isEmpty()) {
                ProfessionalCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "No site photographs captured yet",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap 'Capture Site Photo' to record substrate conditions.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.testTag("survey_captures_section")
                ) {
                    uiState.captures.forEach { capture ->
                        SurveyCaptureItem(
                            capture = capture,
                            onDelete = { viewModel?.deleteCapture(capture.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Architectural Roadmap & Task Boundaries
            Text(
                text = "Architectural Implementation Plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            ProfessionalCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RoadmapStepItem(
                        stepCode = "SV-001",
                        title = "Foundation & App Shell",
                        description = "Compose shell, navigation graph, domain models, testing infrastructure",
                        statusText = "COMPLETED",
                        isCompleted = true,
                        isCurrentOrNext = false,
                        icon = Icons.Default.CheckCircle
                    )

                    RoadmapStepItem(
                        stepCode = "SV-002",
                        title = "Local Room Persistence",
                        description = "Room SQLite database, DAOs, entity mappers, offline-first data layer",
                        statusText = "COMPLETED",
                        isCompleted = true,
                        isCurrentOrNext = false,
                        icon = Icons.Default.CheckCircle
                    )

                    RoadmapStepItem(
                        stepCode = "SV-003",
                        title = "Camera Capture Subsystem",
                        description = "CameraX preview, photo capture, local image file storage & Room metadata",
                        statusText = "COMPLETED",
                        isCompleted = true,
                        isCurrentOrNext = false,
                        icon = Icons.Default.CheckCircle
                    )

                    RoadmapStepItem(
                        stepCode = "SV-004",
                        title = "ARCore Spatial Computing",
                        description = "Visual Inertial Odometry, horizontal & vertical plane detection, anchors",
                        statusText = "NEXT TASK",
                        isCompleted = false,
                        isCurrentOrNext = true,
                        icon = Icons.Default.ViewInAr
                    )

                    RoadmapStepItem(
                        stepCode = "SV-005+",
                        title = "Geometry & Material Engine",
                        description = "Surface area measurement, SmartCoat™ coverage calculations & AI reasoning",
                        statusText = "ROADMAP",
                        isCompleted = false,
                        isCurrentOrNext = false,
                        icon = Icons.Default.Tune
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToHome,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("survey_back_to_home_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrecisionSky),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Dashboard")
                }

                OutlinedButton(
                    onClick = onNavigateToProjects,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("survey_view_projects_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Browse Projects")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SurveyCaptureItem(
    capture: SurveyCapture,
    onDelete: () -> Unit
) {
    val file = remember(capture.localPath) { File(capture.localPath) }
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    ProfessionalCard(
        shape = RoundedCornerShape(10.dp),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = file,
                contentDescription = "Capture ${capture.id}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = capture.id,
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = PrecisionSky
                )
                Text(
                    text = "${capture.captureType.name} • ${capture.width ?: "—"}x${capture.height ?: "—"} px",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateFormatter.format(Date(capture.createdAtEpochMs)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_capture_${capture.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete capture",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun RoadmapStepItem(
    stepCode: String,
    title: String,
    description: String,
    statusText: String,
    isCompleted: Boolean,
    isCurrentOrNext: Boolean,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (tintColor, badgeStyle) = when {
            isCompleted -> PrecisionEmerald to BadgeStyle.READY
            isCurrentOrNext -> ConstructionAmber to BadgeStyle.PENDING_TASK
            else -> MaterialTheme.colorScheme.onSurfaceVariant to BadgeStyle.INFO
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(tintColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "[$stepCode] ",
                    style = MaterialTheme.typography.labelSmall,
                    color = tintColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TechnicalStatusBadge(
            text = statusText,
            style = badgeStyle
        )
    }
}
