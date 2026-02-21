package com.sysop.tricorder.feature.map

import android.view.Gravity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.sysop.tricorder.core.model.SensorCategory
import com.sysop.tricorder.core.ui.components.SensorReadoutChip
import com.sysop.tricorder.core.ui.theme.color
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToDetail: (SensorCategory) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSessions: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel(),
) {
    val activeCategories by viewModel.activeCategories.collectAsState()
    val readings by viewModel.readings.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        MapLibre.getInstance(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tricorder") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleRecording() }) {
                        Icon(
                            Icons.Default.FiberManualRecord,
                            contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                            tint = if (isRecording) Color.Red else Color.White,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                ),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // MapLibre map
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        getMapAsync { map ->
                            map.setStyle("https://tiles.openfreemap.org/styles/liberty")
                            map.uiSettings.apply {
                                isCompassEnabled = true
                                setCompassGravity(Gravity.TOP or Gravity.END)
                                isRotateGesturesEnabled = true
                                isTiltGesturesEnabled = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )

            // Quick readout bar
            if (readings.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 72.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(readings.entries.toList()) { (providerId, reading) ->
                        val firstValue = reading.values.entries.firstOrNull()
                        if (firstValue != null) {
                            SensorReadoutChip(
                                label = providerId,
                                value = "%.1f".format(firstValue.value),
                                unit = firstValue.key,
                                accentColor = reading.category.color(),
                            )
                        }
                    }
                }
            }

            // Category tabs
            LazyRow(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(SensorCategory.entries) { category ->
                    val isActive = category in activeCategories
                    FilterChip(
                        selected = isActive,
                        onClick = { viewModel.toggleCategory(category) },
                        label = { Text(category.displayName, style = MaterialTheme.typography.bodyMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = category.color().copy(alpha = 0.3f),
                            selectedLabelColor = category.color(),
                        ),
                    )
                }
            }
        }
    }
}
