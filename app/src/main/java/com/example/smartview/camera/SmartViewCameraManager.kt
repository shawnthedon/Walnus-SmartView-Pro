package com.example.smartview.camera

import android.content.Context
import android.graphics.BitmapFactory
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.smartview.core.log.SmartViewLogger
import com.example.smartview.domain.model.CaptureType
import com.example.smartview.domain.model.SurveyCapture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume

/**
 * Camera service abstraction isolating CameraX implementation details from ViewModels and UI.
 */
interface CameraCaptureService {
    val cameraState: StateFlow<CameraUiState>
    fun setPermissionGranted(isGranted: Boolean)
    fun bindToLifecycle(lifecycleOwner: LifecycleOwner, previewView: PreviewView)
    fun unbind()
    suspend fun takePhoto(surveyId: String, captureType: CaptureType = CaptureType.SURVEY_STILL): Result<SurveyCapture>
}

/**
 * Production implementation of [CameraCaptureService] using Android CameraX.
 * Adheres to offline-first principles, storing all images inside application-private storage.
 */
class CameraXCaptureService(
    private val context: Context
) : CameraCaptureService {

    private val _cameraState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    override val cameraState: StateFlow<CameraUiState> = _cameraState.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var currentLifecycleOwner: LifecycleOwner? = null

    override fun setPermissionGranted(isGranted: Boolean) {
        if (!isGranted) {
            _cameraState.value = CameraUiState.PermissionRequired
        } else if (_cameraState.value == CameraUiState.PermissionRequired) {
            _cameraState.value = CameraUiState.Idle
        }
    }

    override fun bindToLifecycle(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        currentLifecycleOwner = lifecycleOwner
        _cameraState.value = CameraUiState.Initializing

        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )

                    SmartViewLogger.i("CAMERA", "CameraX successfully bound to lifecycle")
                    _cameraState.value = CameraUiState.Ready
                } catch (e: Exception) {
                    SmartViewLogger.e("CAMERA", "Failed to bind CameraX use cases", e)
                    _cameraState.value = CameraUiState.CameraUnavailable(
                        e.localizedMessage ?: "Camera hardware unavailable"
                    )
                }
            }, ContextCompat.getMainExecutor(context))
        } catch (e: Exception) {
            SmartViewLogger.e("CAMERA", "Failed to get ProcessCameraProvider instance", e)
            _cameraState.value = CameraUiState.CameraUnavailable(
                e.localizedMessage ?: "Failed to initialize Camera provider"
            )
        }
    }

    override fun unbind() {
        try {
            cameraProvider?.unbindAll()
            _cameraState.value = CameraUiState.Idle
            SmartViewLogger.i("CAMERA", "CameraX unbind completed")
        } catch (e: Exception) {
            SmartViewLogger.e("CAMERA", "Error unbinding CameraX", e)
        }
    }

    override suspend fun takePhoto(
        surveyId: String,
        captureType: CaptureType
    ): Result<SurveyCapture> = withContext(Dispatchers.IO) {
        val activeImageCapture = imageCapture
        if (activeImageCapture == null) {
            SmartViewLogger.e("CAMERA", "Cannot capture photo: ImageCapture is null")
            _cameraState.value = CameraUiState.CaptureError("Camera not ready for capture")
            return@withContext Result.failure(IllegalStateException("Camera capture use case is not bound"))
        }

        _cameraState.value = CameraUiState.Capturing

        val capturesDir = File(context.filesDir, "survey_captures").apply {
            if (!exists()) mkdirs()
        }

        val captureId = "cap-${UUID.randomUUID().toString().take(8)}"
        val photoFile = File(capturesDir, "survey_${surveyId}_${captureId}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        suspendCancellableCoroutine { continuation ->
            activeImageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        try {
                            val options = BitmapFactory.Options().apply {
                                inJustDecodeBounds = true
                            }
                            BitmapFactory.decodeFile(photoFile.absolutePath, options)
                            val width = if (options.outWidth > 0) options.outWidth else null
                            val height = if (options.outHeight > 0) options.outHeight else null

                            val capture = SurveyCapture(
                                id = captureId,
                                surveyId = surveyId,
                                localPath = photoFile.absolutePath,
                                captureType = captureType,
                                width = width,
                                height = height,
                                createdAtEpochMs = System.currentTimeMillis()
                            )

                            SmartViewLogger.i("CAMERA", "Image successfully captured and saved: ${photoFile.name}")
                            _cameraState.value = CameraUiState.CaptureSuccess(capture)
                            continuation.resume(Result.success(capture))
                        } catch (ex: Exception) {
                            SmartViewLogger.e("CAMERA", "Error decoding capture dimensions", ex)
                            _cameraState.value = CameraUiState.CaptureError("Error processing captured image: ${ex.message}")
                            continuation.resume(Result.failure(ex))
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        SmartViewLogger.e("CAMERA", "CameraX image capture failed: ${exception.message}", exception)
                        _cameraState.value = CameraUiState.CaptureError(exception.localizedMessage ?: "Capture failed")
                        continuation.resume(Result.failure(exception))
                    }
                }
            )
        }
    }
}
