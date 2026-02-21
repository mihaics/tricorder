package com.sysop.tricorder.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sysop.tricorder.core.ui.theme.AccentGreen
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val openWeatherMapKey by viewModel.openWeatherMapKey.collectAsState()
    val waqiKey by viewModel.waqiKey.collectAsState()
    val n2yoKey by viewModel.n2yoKey.collectAsState()
    val openCellIdKey by viewModel.openCellIdKey.collectAsState()
    val defaultSampleRateMs by viewModel.defaultSampleRateMs.collectAsState()
    val sessionRetentionDays by viewModel.sessionRetentionDays.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = AccentGreen,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // --- API Keys Section ---
            item {
                SectionHeader("API Keys")
            }

            item {
                ApiKeyField(
                    label = "OpenWeatherMap",
                    value = openWeatherMapKey,
                    onValueChange = viewModel::setOpenWeatherMapKey,
                )
            }

            item {
                ApiKeyField(
                    label = "WAQI (Air Quality)",
                    value = waqiKey,
                    onValueChange = viewModel::setWaqiKey,
                )
            }

            item {
                ApiKeyField(
                    label = "N2YO (Satellite Tracking)",
                    value = n2yoKey,
                    onValueChange = viewModel::setN2yoKey,
                )
            }

            item {
                ApiKeyField(
                    label = "OpenCelliD (Cell Towers)",
                    value = openCellIdKey,
                    onValueChange = viewModel::setOpenCellIdKey,
                )
            }

            // --- Sensor Settings Section ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("Sensor Settings")
            }

            item {
                SampleRateSlider(
                    currentMs = defaultSampleRateMs,
                    onValueChange = viewModel::setDefaultSampleRateMs,
                )
            }

            // --- Session Settings Section ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("Session Settings")
            }

            item {
                RetentionDaysInput(
                    currentDays = sessionRetentionDays,
                    onValueChange = viewModel::setSessionRetentionDays,
                )
            }

            // --- About Section ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("About")
            }

            item {
                AboutCard()
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = AccentGreen,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun ApiKeyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    var text by remember(value) { mutableStateOf(value) }
    var revealed by remember { mutableStateOf(false) }
    val isConfigured = value.isNotBlank()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = if (isConfigured) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = if (isConfigured) "Configured" else "Not configured",
                    tint = if (isConfigured) AccentGreen else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API Key") },
                singleLine = true,
                visualTransformation = if (revealed) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    cursorColor = AccentGreen,
                    focusedLabelColor = AccentGreen,
                ),
                trailingIcon = {
                    Row {
                        TextButton(onClick = { revealed = !revealed }) {
                            Text(
                                if (revealed) "Hide" else "Show",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        if (text != value) {
                            TextButton(onClick = { onValueChange(text) }) {
                                Text("Save", color = AccentGreen, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun SampleRateSlider(
    currentMs: Long,
    onValueChange: (Long) -> Unit,
) {
    var sliderValue by remember(currentMs) { mutableStateOf(currentMs.toFloat()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Default Sample Rate",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${sliderValue.roundToLong()} ms",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AccentGreen,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onValueChange(sliderValue.roundToLong()) },
                valueRange = 100f..5000f,
                steps = 48, // 100ms increments: (5000-100)/100 - 1 = 48
                colors = SliderDefaults.colors(
                    thumbColor = AccentGreen,
                    activeTrackColor = AccentGreen,
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("100 ms", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text("5000 ms", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun RetentionDaysInput(
    currentDays: Int,
    onValueChange: (Int) -> Unit,
) {
    var text by remember(currentDays) { mutableStateOf(currentDays.toString()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Session Retention",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } && newValue.length <= 4) {
                        text = newValue
                        newValue.toIntOrNull()?.let { days ->
                            if (days in 1..9999) onValueChange(days)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Retention period (days)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    cursorColor = AccentGreen,
                    focusedLabelColor = AccentGreen,
                ),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Sessions older than this will be automatically deleted",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun AboutCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "App Version",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "0.1.0",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AccentGreen,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tricorder",
                style = MaterialTheme.typography.headlineMedium,
                color = AccentGreen,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "A multi-sensor aggregation tool for Android",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "github.com/sysop/tricorder",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
