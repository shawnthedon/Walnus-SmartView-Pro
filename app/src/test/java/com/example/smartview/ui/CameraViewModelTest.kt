package com.example.smartview.ui

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.example.smartview.camera.CameraCaptureService
import com.example.smartview.camera.CameraUiState
import com.example.smartview.core.dispatchers.DispatcherProvider
import com.example.smartview.data.repository.InMemoryProjectRepository
import com.example.smartview.data.repository.SurveyCaptureRepository
import com.example.smartview.domain.model.CaptureType
import com.example.smartview.domain.model.Survey
import com.example.smartview.domain.model.SurveyCapture
import com.example.smartview.ui.screens.camera.CameraViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatcherProvider = object : DispatcherProvider {
        override val main: CoroutineDispatcher get() = testDispatcher
        override val io: CoroutineDispatcher get() = testDispatcher
        override val default: CoroutineDispatcher get() = testDispatcher
        override val unconfined: CoroutineDispatcher get() = testDispatcher
    }

    private class FakeCameraCaptureService : CameraCaptureService {
        private val _state = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
        override val cameraState: StateFlow<CameraUiState> = _state.asStateFlow()

        var shouldFail: Boolean = false
        var captureToReturn: SurveyCapture? = null

        override fun setPermissionGranted(isGranted: Boolean) {
            _state.value = if (isGranted) CameraUiState.Ready else CameraUiState.PermissionRequired
        }

        override fun bindToLifecycle(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
            _state.value = CameraUiState.Ready
        }

        override fun unbind() {
            _state.value = CameraUiState.Idle
        }

        override suspend fun takePhoto(surveyId: String, captureType: CaptureType): Result<SurveyCapture> {
            return if (shouldFail) {
                Result.failure(RuntimeException("Simulated camera hardware error"))
            } else {
                val cap = captureToReturn ?: SurveyCapture(
                    id = "cap-fake-1",
                    surveyId = surveyId,
                    localPath = "/mock/path/photo.jpg",
                    width = 1920,
                    height = 1080
                )
                Result.success(cap)
            }
        }
    }

    private class FakeSurveyCaptureRepository : SurveyCaptureRepository {
        val captures = mutableListOf<SurveyCapture>()

        override fun getCapturesForSurvey(surveyId: String): Flow<List<SurveyCapture>> {
            return flowOf(captures.filter { it.surveyId == surveyId })
        }

        override suspend fun getCaptureById(id: String): SurveyCapture? {
            return captures.find { it.id == id }
        }

        override suspend fun saveCapture(capture: SurveyCapture): SurveyCapture {
            captures.add(capture)
            return capture
        }

        override suspend fun deleteCapture(id: String): Boolean {
            return captures.removeIf { it.id == id }
        }

        override suspend fun deleteCapturesForSurvey(surveyId: String): Int {
            val count = captures.count { it.surveyId == surveyId }
            captures.removeIf { it.surveyId == surveyId }
            return count
        }
    }

    private lateinit var surveyRepository: InMemoryProjectRepository
    private lateinit var captureRepository: FakeSurveyCaptureRepository
    private lateinit var cameraService: FakeCameraCaptureService

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        surveyRepository = InMemoryProjectRepository()
        captureRepository = FakeSurveyCaptureRepository()
        cameraService = FakeCameraCaptureService()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cameraViewModel_withValidSurvey_initializesAndLoadsSurveyContext() = runTest {
        val survey = Survey(id = "srv-test-cam", projectId = "proj-1", surveyTitle = "Facade Inspection", surveyCode = "SV-FC1")
        surveyRepository.createSurvey(survey)

        val viewModel = CameraViewModel(
            surveyId = "srv-test-cam",
            cameraService = cameraService,
            surveyRepository = surveyRepository,
            surveyCaptureRepository = captureRepository,
            dispatchers = testDispatcherProvider
        )

        val state = viewModel.uiState.value
        assertFalse(state.isInvalidSurvey)
        assertEquals("SV-FC1", state.surveyCode)
        assertEquals("Facade Inspection", state.surveyTitle)
    }

    @Test
    fun cameraViewModel_withEmptySurvey_marksInvalidContext() = runTest {
        val viewModel = CameraViewModel(
            surveyId = "",
            cameraService = cameraService,
            surveyRepository = surveyRepository,
            surveyCaptureRepository = captureRepository,
            dispatchers = testDispatcherProvider
        )

        val state = viewModel.uiState.value
        assertTrue(state.isInvalidSurvey)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun cameraViewModel_permissionHandling_updatesState() = runTest {
        val viewModel = CameraViewModel(
            surveyId = "srv-test-cam",
            cameraService = cameraService,
            surveyRepository = surveyRepository,
            surveyCaptureRepository = captureRepository,
            dispatchers = testDispatcherProvider
        )

        viewModel.onPermissionResult(false)
        assertFalse(viewModel.uiState.value.isPermissionGranted)
        assertEquals(CameraUiState.PermissionRequired, viewModel.uiState.value.cameraState)

        viewModel.onPermissionResult(true)
        assertTrue(viewModel.uiState.value.isPermissionGranted)
        assertEquals(CameraUiState.Ready, viewModel.uiState.value.cameraState)
    }

    @Test
    fun cameraViewModel_capturePhoto_persistsToRepositoryAndOpensReview() = runTest {
        val survey = Survey(id = "srv-capture-ok", projectId = "proj-1", surveyTitle = "Review Test", surveyCode = "SV-REV")
        surveyRepository.createSurvey(survey)

        val viewModel = CameraViewModel(
            surveyId = "srv-capture-ok",
            cameraService = cameraService,
            surveyRepository = surveyRepository,
            surveyCaptureRepository = captureRepository,
            dispatchers = testDispatcherProvider
        )

        viewModel.capturePhoto()

        val state = viewModel.uiState.value
        assertTrue(state.isReviewVisible)
        assertNotNull(state.lastCapture)
        assertEquals("srv-capture-ok", state.lastCapture?.surveyId)
        assertEquals(1, captureRepository.captures.size)
    }
}
