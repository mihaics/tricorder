package com.sysop.tricorder.feature.detail.aviation

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.sysop.tricorder.core.ui.theme.AviationSkyBlue
import com.sysop.tricorder.core.ui.theme.DarkSurface
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AircraftTrackerScreen(
    onBack: () -> Unit = {},
    viewModel: AircraftTrackerViewModel = hiltViewModel(),
) {
    val aircraft by viewModel.aircraft.collectAsState()
    val isTableMode by viewModel.isTableMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aircraft Tracker", color = AviationSkyBlue) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.toggleMode() }) {
                        Text(
                            if (isTableMode) "Map View" else "Table View",
                            color = AviationSkyBlue,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Text(
                "${aircraft.size} aircraft detected",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )

            if (isTableMode) {
                TableView(aircraft)
            } else {
                AircraftMapView(viewModel)
            }
        }
    }
}

@Composable
private fun TableView(aircraft: List<com.sysop.tricorder.core.model.SensorReading>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        items(aircraft) { reading ->
            val callsign = reading.labels["callsign"] ?: "N/A"
            val country = reading.labels["origin_country"] ?: ""
            val altitude = reading.values["altitude_m"]?.let { "%.0f m".format(it) } ?: "N/A"
            val speed = reading.values["velocity_ms"]?.let { "%.0f m/s".format(it) } ?: ""
            val heading = reading.values["heading_deg"]?.let { "%.0f\u00B0".format(it) } ?: ""

            ListItem(
                headlineContent = { Text(callsign, style = MaterialTheme.typography.labelLarge) },
                supportingContent = { Text("$country  $heading  $speed") },
                trailingContent = {
                    Text(
                        altitude,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AviationSkyBlue,
                    )
                },
            )
            HorizontalDivider()
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun AircraftMapView(viewModel: AircraftTrackerViewModel) {
    val arAircraft by viewModel.arAircraft.collectAsState()
    val context = LocalContext.current

    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val mapReady = remember { mutableStateOf(false) }

    // Update markers when aircraft data changes
    LaunchedEffect(arAircraft, mapReady.value) {
        val mapView = mapViewRef.value ?: return@LaunchedEffect
        if (!mapReady.value) return@LaunchedEffect

        mapView.getMapAsync { map ->
            map.markers.forEach { map.removeMarker(it) }

            val iconFactory = IconFactory.getInstance(context)

            arAircraft.forEach { ac ->
                val lat = ac.latitude
                val lon = ac.longitude
                if (lat == 0.0 && lon == 0.0) return@forEach

                val callsign = ac.callsign.ifEmpty { "---" }
                val label = "$callsign  %.0fm".format(ac.altitudeM)

                val markerBitmap = createAircraftMarkerBitmap(label, ac.headingDeg.toFloat())
                val icon = iconFactory.fromBitmap(markerBitmap)

                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(lat, lon))
                        .icon(icon)
                        .title(callsign)
                        .snippet("Alt: %.0fm  Speed: %.0f m/s  Dist: %.1f km".format(
                            ac.altitudeM, ac.velocityMs, ac.distanceKm,
                        ))
                )
            }

            // Fit bounds to show all aircraft
            if (arAircraft.isNotEmpty()) {
                val points = arAircraft
                    .filter { it.latitude != 0.0 || it.longitude != 0.0 }
                    .map { LatLng(it.latitude, it.longitude) }
                if (points.size >= 2) {
                    val boundsBuilder = LatLngBounds.Builder()
                    points.forEach { boundsBuilder.include(it) }
                    try {
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 60),
                            500,
                        )
                    } catch (_: Exception) {}
                }
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).also { mv ->
                mapViewRef.value = mv
                mv.getMapAsync { map ->
                    map.setStyle("https://tiles.openfreemap.org/styles/liberty") {
                        // Enable user location
                        try {
                            val locationComponent = map.locationComponent
                            val activationOptions = org.maplibre.android.location.LocationComponentActivationOptions
                                .builder(ctx, it)
                                .build()
                            locationComponent.activateLocationComponent(activationOptions)
                            locationComponent.isLocationComponentEnabled = true
                            locationComponent.cameraMode = org.maplibre.android.location.modes.CameraMode.TRACKING
                            locationComponent.renderMode = org.maplibre.android.location.modes.RenderMode.COMPASS
                        } catch (_: Exception) {}

                        map.moveCamera(CameraUpdateFactory.zoomTo(8.0))
                        mapReady.value = true
                    }
                    map.uiSettings.apply {
                        isCompassEnabled = true
                        isRotateGesturesEnabled = true
                        isTiltGesturesEnabled = true
                        isZoomGesturesEnabled = true
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
    )
}

private fun createAircraftMarkerBitmap(label: String, headingDeg: Float): Bitmap {
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 28f
        typeface = Typeface.DEFAULT_BOLD
    }
    val textWidth = textPaint.measureText(label).toInt()
    val iconSize = 24
    val padding = 8
    val width = textWidth + padding * 2
    val totalHeight = iconSize + 4 + 28 + padding * 2

    val bitmap = Bitmap.createBitmap(width.coerceAtLeast(iconSize + padding * 2), totalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(180, 0, 0, 0)
    }
    canvas.drawRoundRect(
        0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(),
        8f, 8f, bgPaint,
    )

    // Aircraft triangle icon rotated by heading
    val cx = bitmap.width / 2f
    val cy = padding.toFloat() + iconSize / 2f
    val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(3, 169, 244) // AviationSkyBlue
        style = Paint.Style.FILL
    }
    canvas.save()
    canvas.rotate(headingDeg, cx, cy)
    val path = android.graphics.Path().apply {
        moveTo(cx, cy - iconSize / 2f)
        lineTo(cx - iconSize / 3f, cy + iconSize / 2f)
        lineTo(cx + iconSize / 3f, cy + iconSize / 2f)
        close()
    }
    canvas.drawPath(path, iconPaint)
    canvas.restore()

    // Label text below icon
    val textX = (bitmap.width - textWidth) / 2f
    val textY = padding + iconSize + 4f + 22f
    canvas.drawText(label, textX, textY, textPaint)

    return bitmap
}
