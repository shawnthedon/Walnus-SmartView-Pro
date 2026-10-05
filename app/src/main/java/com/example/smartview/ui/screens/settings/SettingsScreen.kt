package com.example.smartview.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.smartview.ui.components.BadgeStyle
import com.example.smartview.ui.components.ProfessionalCard
import com.example.smartview.ui.components.SmartViewHeader
import com.example.smartview.ui.components.TechnicalStatusBadge

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartViewHeader(
            statusBadgeText = "CONFIG",
            statusBadgeStyle = BadgeStyle.INFO
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.title_configuration),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Field surveying preferences and system diagnostics",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Unit System Selection
            ProfessionalCard {
                Text(
                    text = stringResource(id = R.string.setting_unit_system),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Controls geometric measurements, surface areas, and volume readings",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = uiState.unitSystem == UnitSystem.METRIC,
                        onClick = { viewModel.setUnitSystem(UnitSystem.METRIC) },
                        label = { Text("Metric (m, m², m³)") },
                        modifier = Modifier.testTag("unit_metric_chip")
                    )
                    FilterChip(
                        selected = uiState.unitSystem == UnitSystem.IMPERIAL,
                        onClick = { viewModel.setUnitSystem(UnitSystem.IMPERIAL) },
                        label = { Text("Imperial (ft, sq ft, cu yd)") },
                        modifier = Modifier.testTag("unit_imperial_chip")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Field Mode & Diagnostic Toggles
            ProfessionalCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.setting_field_mode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(id = R.string.setting_field_mode_desc),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isFieldHighContrastEnabled,
                        onCheckedChange = { viewModel.toggleFieldHighContrast(it) },
                        modifier = Modifier.testTag("field_mode_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.setting_diagnostic_logging),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(id = R.string.setting_diagnostic_logging_desc),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isDiagnosticLoggingEnabled,
                        onCheckedChange = { viewModel.toggleDiagnosticLogging(it) },
                        modifier = Modifier.testTag("diagnostic_logging_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Product & Architecture Specifications
            ProfessionalCard {
                Text(
                    text = "System Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                SpecRow(label = "Application ID", value = uiState.applicationId)
                SpecRow(label = "Build Target", value = "Android 15 (API 36)")
                SpecRow(label = "Foundation Phase", value = "SV-001 Greenfield Foundation")
                SpecRow(label = "Product Ecosystem", value = uiState.productFamily)
                SpecRow(label = "Organization", value = uiState.company)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
