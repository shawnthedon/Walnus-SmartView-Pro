package com.example.smartview.ui.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.smartview.camera.CameraUiState
import com.example.smartview.ui.components.BadgeStyle
import com.example.smartview.ui.components.TechnicalStatusBadge
import com.example.smartview.ui.theme.ConstructionAmber
import com.example.smartview.ui.theme.DarkSurface
import com.example.smartview.ui.theme.DarkSurfaceVariant
import com.example.smartview.ui.theme.PrecisionSky
import com.example.smartview.ui.theme.StatusGreen
import com.example.smartview.ui.theme.StatusRed
import java.io.File

/**
 * Dedicated Camera Acquisition Screen for SmartView Pro™ (Milestone SV-003).
 * Integrates Android CameraX with lifecycle-safe preview and local photo capture.
 */
@Composable
fun CameraCaptureScreen(
    viewModel: CameraViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onPermissionResult(hasPermission)
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            viewModel.unbindCamera()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("camera_capture_screen")
            .background(Color.Black)
    ) {
        // Camera Preview Layer or Fallback/Permission states
        when {
            uiState.isInvalidSurvey -> {
                InvalidSurveyNotice(
                    errorMessage = uiState.errorMessage ?: "Invalid survey context",
                    onNavigateBack = onNavigateBack
                )
            }
            !uiState.isPermissionGranted -> {
                PermissionRequiredView(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onNavigateBack = onNavigateBack
                )
            }
            uiState.cameraState is CameraUiState.CameraUnavailable -> {
                CameraUnavailableView(
                    reason = (uiState.cameraState as CameraUiState.CameraUnavailable).reason,
                    onNavigateBack = onNavigateBack
                )
            }
            else -> {
                // Live CameraX Preview Viewport
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            this.scaleType = PreviewView.ScaleType.FILL_CENTER
                            viewModel.bindCamera(lifecycleOwner, this)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("camerax_preview_view")
                )

                // Technical Reticle & Optical Overlay
                ViewfinderCrosshairOverlay()
            }
        }

        // Top Navigation & Survey Metadata Bar
        CameraTopBar(
            surveyCode = uiState.surveyCode,
            surveyTitle = uiState.surveyTitle,
            cameraState = uiState.cameraState,
            onNavigateBack = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        // Bottom Capture Controls
        if (uiState.isPermissionGranted && !uiState.isInvalidSurvey) {
            CameraBottomBar(
                cameraState = uiState.cameraState,
                onCaptureClick = { viewModel.capturePhoto() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }

        // Capture Success Review Sheet
        AnimatedVisibility(
            visible = uiState.isReviewVisible && uiState.lastCapture != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            uiState.lastCapture?.let { capture ->
                CaptureReviewCard(
                    capture = capture,
                    onDismiss = { viewModel.dismissReview() },
                    onFinish = {
                        viewModel.dismissReview()
                        onNavigateBack()
                    }
                )
            }
        }
    }
}

@Composable
private fun CameraTopBar(
    surveyCode: String,
    surveyTitle: String,
    cameraState: CameraUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.65f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("camera_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Return from camera",
                    tint = Color.White
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (surveyCode.isNotBlank()) surveyCode else "SMARTVIEW CAMERA",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrecisionSky,
                    fontFamily = FontFamily.Monospace
                )
                if (surveyTitle.isNotBlank()) {
                    Text(
                        text = surveyTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray,
                        maxLines = 1
                    )
                }
            }

            val (badgeText, badgeStyle) = when (cameraState) {
                is CameraUiState.Ready -> "ACTIVE" to BadgeStyle.READY
                is CameraUiState.Capturing -> "SAVING" to BadgeStyle.WARNING
                is CameraUiState.CaptureSuccess -> "SAVED" to BadgeStyle.READY
                is CameraUiState.Initializing -> "INIT" to BadgeStyle.NEUTRAL
                is CameraUiState.CaptureError -> "ERROR" to BadgeStyle.ERROR
                else -> "OFFLINE" to BadgeStyle.NEUTRAL
            }

            TechnicalStatusBadge(
                text = badgeText,
                style = badgeStyle
            )
        }
    }
}

@Composable
private fun CameraBottomBar(
    cameraState: CameraUiState,
    onCaptureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.65f),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp, top = 16.dp)
        ) {
            val isCapturing = cameraState is CameraUiState.Capturing

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(84.dp)
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(3.dp, Color.White, CircleShape)
                )

                // Shutter button inside ring
                IconButton(
                    onClick = onCaptureClick,
                    enabled = !isCapturing && cameraState !is CameraUiState.CameraUnavailable,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(if (isCapturing) ConstructionAmber else PrecisionSky)
                        .testTag("camera_shutter_button")
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture Photograph",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "WALNUS FIELD ACQUISITION PIPELINE",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun ViewfinderCrosshairOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val reticleLength = 24.dp.toPx()
        val reticleGap = 12.dp.toPx()
        val color = Color.White.copy(alpha = 0.5f)
        val strokeWidth = 1.5.dp.toPx()

        // Horizontal reticle marks
        drawLine(
            color = color,
            start = Offset(centerX - reticleLength - reticleGap, centerY),
            end = Offset(centerX - reticleGap, centerY),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(centerX + reticleGap, centerY),
            end = Offset(centerX + reticleLength + reticleGap, centerY),
            strokeWidth = strokeWidth
        )

        // Vertical reticle marks
        drawLine(
            color = color,
            start = Offset(centerX, centerY - reticleLength - reticleGap),
            end = Offset(centerX, centerY - reticleGap),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(centerX, centerY + reticleGap),
            end = Offset(centerX, centerY + reticleLength + reticleGap),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
private fun CaptureReviewCard(
    capture: com.example.smartview.domain.model.SurveyCapture,
    onDismiss: () -> Unit,
    onFinish: () -> Unit
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("capture_review_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Image Captured & Persisted",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                TechnicalStatusBadge(
                    text = "LOCAL ONLY",
                    style = BadgeStyle.READY
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Thumbnail and Metadata Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                val imageFile = remember(capture.localPath) { File(capture.localPath) }

                AsyncImage(
                    model = imageFile,
                    contentDescription = "Captured preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ID: ${capture.id}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = PrecisionSky
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Dimensions: ${capture.width ?: "—"} x ${capture.height ?: "—"} px",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "File: ${imageFile.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("take_another_button")
                ) {
                    Text("Take Another")
                }

                Button(
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(containerColor = PrecisionSky),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("done_capture_button")
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun PermissionRequiredView(
    onRequestPermission: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag("permission_required_view")
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = PrecisionSky,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "SmartView Pro™ requires camera access to capture site survey photographs, surface conditions, and geometric references for the active project.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = PrecisionSky),
            modifier = Modifier.testTag("grant_camera_permission_button")
        ) {
            Text("Grant Camera Access")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("cancel_permission_button")
        ) {
            Text("Return to Survey")
        }
    }
}

@Composable
private fun CameraUnavailableView(
    reason: String,
    onNavigateBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag("camera_unavailable_view")
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = ConstructionAmber,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Camera Hardware Unavailable",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = reason,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateBack,
            colors = ButtonDefaults.buttonColors(containerColor = PrecisionSky)
        ) {
            Text("Back to Survey")
        }
    }
}

@Composable
private fun InvalidSurveyNotice(
    errorMessage: String,
    onNavigateBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag("invalid_survey_view")
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = StatusRed,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Survey Context Required",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateBack,
            colors = ButtonDefaults.buttonColors(containerColor = PrecisionSky)
        ) {
            Text("Select Active Survey")
        }
    }
}
