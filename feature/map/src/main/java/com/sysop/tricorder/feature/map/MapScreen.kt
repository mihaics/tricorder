package com.sysop.tricorder.feature.map

import android.annotation.SuppressLint
import android.view.Gravity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.sysop.tricorder.core.model.SensorCategory
import com.sysop.tricorder.core.ui.theme.AccentGreen
import com.sysop.tricorder.core.ui.theme.DarkSurface
import com.sysop.tricorder.core.ui.theme.DarkSurfaceVariant
import com.sysop.tricorder.core.ui.theme.color
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
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

    // Panel weight: 0.0 = panel collapsed, 1.0 = panel takes all space
    // Default ~0.35 means panel gets ~35% of space
    var panelWeight by remember { mutableFloatStateOf(0.35f) }
    val density = LocalDensity.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    "TRICORDER",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen,
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Menu, contentDescription = "Settings", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = onNavigateToSessions) {
                    Icon(Icons.Default.List, contentDescription = "Sessions", tint = Color.White)
                }
                IconButton(onClick = { viewModel.toggleRecording() }) {
                    Icon(
                        Icons.Default.FiberManualRecord,
                        contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                        tint = if (isRecording) Color.Red else Color.White,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkSurface,
            ),
        )

        // Content area with resizable split
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val totalHeightPx = constraints.maxHeight.toFloat()

            Column(modifier = Modifier.fillMaxSize()) {
                // Map section
                Box(modifier = Modifier.fillMaxWidth().weight(1f - panelWeight)) {
                    @SuppressLint("MissingPermission")
                    AndroidView(
                        factory = { ctx ->
                            MapView(ctx).apply {
                                getMapAsync { map ->
                                    map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
                                        val locationComponent = map.locationComponent
                                        locationComponent.activateLocationComponent(
                                            LocationComponentActivationOptions.builder(ctx, style)
                                                .build()
                                        )
                                        locationComponent.isLocationComponentEnabled = true
                                        locationComponent.cameraMode = CameraMode.TRACKING
                                        locationComponent.renderMode = RenderMode.COMPASS

                                        locationComponent.lastKnownLocation?.let { loc ->
                                            map.moveCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(loc.latitude, loc.longitude), 15.0
                                                )
                                            )
                                        }
                                    }
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
                }

                // Drag handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .pointerInput(totalHeightPx) {
                            detectVerticalDragGestures { _, dragAmount ->
                                val delta = dragAmount / totalHeightPx
                                panelWeight = (panelWeight - delta).coerceIn(0.15f, 0.75f)
                            }
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(2.dp),
                            )
                    )
                }

                // Bottom panel: categories + readings
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(panelWeight)
                        .background(DarkSurface)
                ) {
                    // Category selector row
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(SensorCategory.entries) { category ->
                            val isActive = category in activeCategories
                            val color = category.color()
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable {
                                        if (category == SensorCategory.CAMERA) {
                                            onNavigateToDetail(category)
                                        } else {
                                            viewModel.toggleCategory(category)
                                        }
                                    }
                                    .background(
                                        if (isActive) color.copy(alpha = 0.2f)
                                        else Color.Transparent,
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            color = if (isActive) color.copy(alpha = 0.3f)
                                            else DarkSurfaceVariant,
                                            shape = CircleShape,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = categoryIcon(category),
                                        contentDescription = category.displayName,
                                        tint = if (isActive) color
                                        else Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = category.displayName.take(6),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isActive) color
                                    else Color.White.copy(alpha = 0.5f),
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = DarkSurfaceVariant, thickness = 1.dp)

                    // Readings list
                    val readingEntries = readings.flatMap { (_, reading) ->
                        reading.values.map { (key, value) ->
                            ReadingRow(
                                label = "${reading.providerId} / $key",
                                value = formatValue(value),
                                unit = "",
                                color = reading.category.color(),
                                category = reading.category,
                            )
                        }
                    }

                    if (readingEntries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (activeCategories.isEmpty()) "Tap a sensor category to start"
                                else "Waiting for readings...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.4f),
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                        ) {
                            items(readingEntries) { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToDetail(row.category) }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = row.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = row.color.copy(alpha = 0.8f),
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        text = row.value,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }
                                HorizontalDivider(color = DarkSurfaceVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class ReadingRow(
    val label: String,
    val value: String,
    val unit: String,
    val color: Color,
    val category: SensorCategory,
)

private fun formatValue(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        "%.2f".format(value)
    }
}

private fun categoryIcon(category: SensorCategory): ImageVector = when (category) {
    SensorCategory.MOTION -> Icons.Default.DirectionsRun
    SensorCategory.ENVIRONMENT -> Icons.Default.Thermostat
    SensorCategory.LOCATION -> Icons.Default.SatelliteAlt
    SensorCategory.RF -> Icons.Default.CellTower
    SensorCategory.AUDIO -> Icons.Default.Mic
    SensorCategory.CAMERA -> Icons.Default.CameraAlt
    SensorCategory.WEATHER -> Icons.Default.Cloud
    SensorCategory.AIR_QUALITY -> Icons.Default.Air
    SensorCategory.AVIATION -> Icons.Default.Flight
    SensorCategory.SEISMIC -> Icons.Default.Tsunami
    SensorCategory.RADIATION -> Icons.Default.Warning
    SensorCategory.SPACE -> Icons.Default.Star
    SensorCategory.TIDES -> Icons.Default.Water
}
