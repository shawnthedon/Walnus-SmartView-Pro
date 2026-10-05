package com.example.smartview.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.example.smartview.core.log.SmartViewLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class UnitSystem {
    METRIC,
    IMPERIAL
}

data class SettingsUiState(
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val isFieldHighContrastEnabled: Boolean = false,
    val isDiagnosticLoggingEnabled: Boolean = true,
    val buildVersion: String = "1.0.0-SV003",
    val applicationId: String = "com.example",
    val productFamily: String = "SmartCoat™ Professional Ecosystem",
    val company: String = "Walnus Global Ventures"
)

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setUnitSystem(unitSystem: UnitSystem) {
        SmartViewLogger.d("SETTINGS", "Unit system changed to: $unitSystem")
        _uiState.value = _uiState.value.copy(unitSystem = unitSystem)
    }

    fun toggleFieldHighContrast(enabled: Boolean) {
        SmartViewLogger.d("SETTINGS", "Field high contrast mode set to: $enabled")
        _uiState.value = _uiState.value.copy(isFieldHighContrastEnabled = enabled)
    }

    fun toggleDiagnosticLogging(enabled: Boolean) {
        SmartViewLogger.isDebugLoggingEnabled = enabled
        SmartViewLogger.i("SETTINGS", "Diagnostic logging set to: $enabled")
        _uiState.value = _uiState.value.copy(isDiagnosticLoggingEnabled = enabled)
    }
}
